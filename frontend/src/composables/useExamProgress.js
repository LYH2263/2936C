import { ref, watch, onUnmounted } from 'vue';

export function useExamProgress(examId, userId, isExamMode) {
  const answers = ref({});
  const flagged = ref(new Set());

  const storageKey = () => {
    const uid = typeof userId === 'function' ? userId() : (userId?.value ?? userId ?? '');
    const eid = typeof examId === 'function' ? examId() : (examId?.value ?? examId ?? '');
    return `exam_progress_${eid}_${uid}`;
  };

  const saveProgress = () => {
    if (!isExamMode()) return;
    try {
      localStorage.setItem(storageKey(), JSON.stringify({
        savedAnswers: answers.value,
        savedFlagged: Array.from(flagged.value),
      }));
    } catch (e) {
      console.error('Failed to save progress', e);
    }
  };

  const restoreProgress = () => {
    if (!isExamMode()) return;
    try {
      const saved = localStorage.getItem(storageKey());
      if (saved) {
        const { savedAnswers, savedFlagged } = JSON.parse(saved);
        answers.value = savedAnswers || {};
        flagged.value = new Set(savedFlagged || []);
      }
    } catch (e) {
      console.error('Failed to restore progress', e);
    }
  };

  const clearProgress = () => {
    try {
      localStorage.removeItem(storageKey());
    } catch (e) {
      console.error('Failed to clear progress', e);
    }
  };

  const toggleFlag = (questionId) => {
    if (flagged.value.has(questionId)) {
      flagged.value.delete(questionId);
    } else {
      flagged.value.add(questionId);
    }
    saveProgress();
  };

  const isFlagged = (questionId) => {
    return flagged.value.has(questionId);
  };

  const setAnswer = (questionId, value) => {
    answers.value[questionId] = value;
  };

  const getAnswer = (questionId) => {
    return answers.value[questionId];
  };

  const isAnswered = (questionId) => {
    return answers.value[questionId] !== undefined && answers.value[questionId] !== '';
  };

  const answeredCount = () => {
    return Object.keys(answers.value).filter((k) => answers.value[k] !== '').length;
  };

  let stopWatch = null;

  const startAutoSave = () => {
    if (stopWatch) return;
    stopWatch = watch(
      answers,
      () => {
        saveProgress();
      },
      { deep: true }
    );
  };

  onUnmounted(() => {
    if (stopWatch) {
      stopWatch();
      stopWatch = null;
    }
  });

  return {
    answers,
    flagged,
    saveProgress,
    restoreProgress,
    clearProgress,
    toggleFlag,
    isFlagged,
    setAnswer,
    getAnswer,
    isAnswered,
    answeredCount,
    startAutoSave,
  };
}
