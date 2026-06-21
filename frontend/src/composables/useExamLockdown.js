import { ref, onUnmounted } from 'vue';
import { message, Modal } from 'ant-design-vue';

const SNAPSHOT_INTERVAL = 5 * 60 * 1000;

export function useExamLockdown(options = {}) {
  const {
    examId,
    isExamMode,
    getExamConfig,
    recordCheatingApi,
    onForceSubmit,
    canvasSelector = '.question-canvas',
  } = options;

  const tabSwitchCount = ref(0);
  const videoRef = ref(null);
  const stream = ref(null);
  const snapshotInterval = ref(null);
  let contextMenuHandler = null;
  const copyPasteHandlers = [];

  const cheatingQueue = [];

  const recordCheatingEvent = async (type, detail) => {
    try {
      const eid = typeof examId === 'function' ? examId() : examId;
      if (recordCheatingApi) {
        await recordCheatingApi(eid, { type, detail });
      }
    } catch (e) {
      cheatingQueue.push({ type, detail, timestamp: Date.now() });
      console.error('Failed to record cheating event', e);
    }
  };

  const initCamera = async () => {
    try {
      if (typeof navigator === 'undefined' || !navigator.mediaDevices) return;
      stream.value = await navigator.mediaDevices.getUserMedia({ video: true });
      if (videoRef.value) {
        videoRef.value.srcObject = stream.value;
      }
    } catch (e) {
      message.warning('无法访问摄像头，无法进行智能监考');
    }
  };

  const stopCamera = () => {
    if (stream.value) {
      stream.value.getTracks().forEach((track) => track.stop());
      stream.value = null;
    }
  };

  const takeSnapshot = () => {
    if (!videoRef.value || !stream.value) return;

    try {
      const canvas = document.createElement('canvas');
      canvas.width = 320;
      canvas.height = 240;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(videoRef.value, 0, 0, 320, 240);
      recordCheatingEvent('SNAPSHOT', 'Periodic camera snapshot captured');
    } catch (e) {
      console.error('Snapshot failed', e);
    }
  };

  const startSnapshotInterval = () => {
    stopSnapshotInterval();
    snapshotInterval.value = setInterval(takeSnapshot, SNAPSHOT_INTERVAL);
  };

  const stopSnapshotInterval = () => {
    if (snapshotInterval.value) {
      clearInterval(snapshotInterval.value);
      snapshotInterval.value = null;
    }
  };

  const handleVisibilityChange = () => {
    const examMode = typeof isExamMode === 'function' ? isExamMode() : isExamMode;
    if (!examMode) return;

    const config = typeof getExamConfig === 'function' ? getExamConfig() : {};
    if (!config) return;

    if (document.hidden) {
      recordCheatingEvent('TAB_SWITCH', 'Student switched tab/minimized window');

      if (config.allowTabSwitch === false) {
        tabSwitchCount.value++;
        const limit = config.tabSwitchLimit || 3;

        if (tabSwitchCount.value >= limit) {
          Modal.error({
            title: '考试由于严重违规已强制结束',
            content: `由于您切屏次数达到上限 (${limit} 次)，系统已自动交卷。`,
            onOk: () => {
              if (onForceSubmit) onForceSubmit();
            },
          });
        } else {
          Modal.warning({
            title: '警告：检测到切屏行为',
            content: `您已切出考试界面 ${tabSwitchCount.value} 次！限制次数为 ${limit} 次，超出后将自动交卷。`,
            centered: true,
          });
        }
      }
    }
  };

  const handleCopyPaste = (e) => {
    e.preventDefault();
    recordCheatingEvent('LOCKDOWN_VIOLATION', `Attempted ${e.type} operation`);
    message.warning('为了考试公平，系统已禁用复制、粘贴和剪切功能');
  };

  const initLockdown = () => {
    document.addEventListener('visibilitychange', handleVisibilityChange);

    const canvas = document.querySelector(canvasSelector);
    if (canvas) {
      const events = ['copy', 'paste', 'cut'];
      events.forEach((evt) => {
        const handler = handleCopyPaste.bind(null);
        canvas.addEventListener(evt, handler);
        copyPasteHandlers.push({ event: evt, handler, element: canvas });
      });
      contextMenuHandler = (e) => e.preventDefault();
      canvas.addEventListener('contextmenu', contextMenuHandler);
    }
  };

  const cleanupLockdown = () => {
    document.removeEventListener('visibilitychange', handleVisibilityChange);

    copyPasteHandlers.forEach(({ event, handler, element }) => {
      element.removeEventListener(event, handler);
    });
    copyPasteHandlers.length = 0;

    if (contextMenuHandler) {
      const canvas = document.querySelector(canvasSelector);
      if (canvas) {
        canvas.removeEventListener('contextmenu', contextMenuHandler);
      }
      contextMenuHandler = null;
    }
  };

  const initAll = (enableCamera = false) => {
    initLockdown();
    if (enableCamera) {
      initCamera().then(() => {
        startSnapshotInterval();
      });
    }
  };

  const cleanupAll = () => {
    stopSnapshotInterval();
    stopCamera();
    cleanupLockdown();
  };

  onUnmounted(() => {
    cleanupAll();
  });

  return {
    tabSwitchCount,
    videoRef,
    stream,
    recordCheatingEvent,
    initCamera,
    stopCamera,
    takeSnapshot,
    startSnapshotInterval,
    stopSnapshotInterval,
    handleVisibilityChange,
    initLockdown,
    cleanupLockdown,
    initAll,
    cleanupAll,
  };
}
