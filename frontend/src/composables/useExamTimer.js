import { ref, onUnmounted } from 'vue';
import { notification } from 'ant-design-vue';

export function useExamTimer(onTimeout) {
  const timeLeft = ref(0);
  const timer = ref(null);
  const reminderSent = ref(false);
  let totalSeconds = 0;

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' + s : s}`;
  };

  const startTimer = (durationMinutes) => {
    stopTimer();
    totalSeconds = durationMinutes * 60;
    timeLeft.value = totalSeconds;
    reminderSent.value = false;

    timer.value = setInterval(() => {
      if (timeLeft.value > 0) {
        timeLeft.value--;

        if (timeLeft.value <= 300 && !reminderSent.value) {
          notification.warning({
            message: '考试提醒',
            description: '距离考试结束还有不到 5 分钟，请妥善安排答题进度并及时交卷。',
            duration: 10,
          });
          reminderSent.value = true;
        }
      } else {
        stopTimer();
        if (onTimeout) {
          onTimeout();
        }
      }
    }, 1000);
  };

  const stopTimer = () => {
    if (timer.value) {
      clearInterval(timer.value);
      timer.value = null;
    }
  };

  const progressPercent = () => {
    if (totalSeconds === 0) return 0;
    return (timeLeft.value / totalSeconds) * 100;
  };

  const isUrgent = (urgentThreshold = 0.1) => {
    if (totalSeconds === 0) return false;
    return timeLeft.value < totalSeconds * urgentThreshold;
  };

  onUnmounted(() => {
    stopTimer();
  });

  return {
    timeLeft,
    reminderSent,
    formatTime,
    startTimer,
    stopTimer,
    progressPercent,
    isUrgent,
  };
}
