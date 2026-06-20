import { ref, computed } from 'vue';

export function useExamSubmission(options = {}) {
  const {
    getExamApi,
    getExamQuestionsApi,
    submitExamApi,
    getSubmissionApi,
    onSubmitSuccess,
    onSubmitError,
    onLoadError,
  } = options;

  const exam = ref(null);
  const questions = ref([]);
  const submissionData = ref(null);
  const loading = ref(true);

  const isAnalysis = computed(() => !!submissionData.value);

  const currentIndex = ref(0);

  const currentQuestion = computed(() => questions.value[currentIndex.value]);

  const totalScore = computed(() =>
    questions.value.reduce((sum, q) => sum + (q.score || 0), 0)
  );

  const jumpTo = (index) => {
    currentIndex.value = index;
  };

  const nextQuestion = () => {
    if (currentIndex.value < questions.value.length - 1) {
      currentIndex.value++;
    }
  };

  const prevQuestion = () => {
    if (currentIndex.value > 0) {
      currentIndex.value--;
    }
  };

  const loadAnalysisMode = async (submissionId) => {
    try {
      const subRes = await getSubmissionApi(submissionId);
      submissionData.value = subRes.data;
      exam.value = subRes.data.exam;

      const qRes = await getExamQuestionsApi(subRes.data.exam.id);
      questions.value = qRes.data;

      const restoredAnswers = {};
      if (submissionData.value.answers) {
        submissionData.value.answers.forEach((sa) => {
          const qId = sa.question.id || sa.question;
          restoredAnswers[qId] = sa.studentAnswer;
        });
      }
      return { restoredAnswers };
    } catch (e) {
      console.error(e);
      if (onLoadError) onLoadError(e);
      throw e;
    }
  };

  const loadExamMode = async (examId) => {
    try {
      const [examRes, qRes] = await Promise.all([
        getExamApi(examId),
        getExamQuestionsApi(examId),
      ]);
      exam.value = examRes.data;
      questions.value = qRes.data;
      return { duration: exam.value.duration, enableCamera: exam.value.enableCamera };
    } catch (e) {
      console.error(e);
      if (onLoadError) onLoadError(e);
      throw e;
    }
  };

  const formatAnswersForSubmit = (answersMap) => {
    const finalAnswers = {};
    Object.keys(answersMap).forEach((qId) => {
      const val = answersMap[qId];
      finalAnswers[qId] = Array.isArray(val) ? val.sort().join(',') : val;
    });
    return finalAnswers;
  };

  const doSubmit = async (examId, answersMap) => {
    try {
      const finalAnswers = formatAnswersForSubmit(answersMap);
      const res = await submitExamApi(examId, finalAnswers);
      if (onSubmitSuccess) {
        onSubmitSuccess(res.data);
      }
      return res.data;
    } catch (e) {
      if (onSubmitError) {
        onSubmitError(e);
      }
      throw e;
    }
  };

  const getQuestionById = (qId) => {
    return questions.value.find((i) => i.question.id === qId);
  };

  const getCorrectAnswer = (qId) => {
    if (!isAnalysis.value) return null;
    const q = getQuestionById(qId);
    if (exam.value && !exam.value.allowViewAnalysis) return '***';
    return q ? q.question.answer : '';
  };

  const getAnalysis = (qId) => {
    if (!isAnalysis.value) return null;
    const q = getQuestionById(qId);
    if (exam.value && !exam.value.allowViewAnalysis) return '教师设置了隐藏解析';
    return q ? q.question.analysis : '';
  };

  const getTeacherComment = (qId) => {
    if (!isAnalysis.value || !submissionData.value.answers) return null;
    const sa = submissionData.value.answers.find(
      (a) => (a.question.id || a.question) === qId
    );
    if (exam.value && !exam.value.allowViewAnalysis) return null;
    return sa ? sa.teacherComment : null;
  };

  const getQuestionScore = (qId) => {
    if (!isAnalysis.value || !submissionData.value.answers) return 0;
    const sa = submissionData.value.answers.find(
      (a) => (a.question.id || a.question) === qId
    );
    return sa ? sa.score : 0;
  };

  const isCorrect = (qId, studentAnswer) => {
    if (!isAnalysis.value) return false;
    const correctAns = getCorrectAnswer(qId);
    return studentAnswer === correctAns;
  };

  const answerProgressPercent = (answersMap) => {
    if (questions.value.length === 0) return 0;
    const answered = Object.keys(answersMap).filter((k) => answersMap[k] !== '').length;
    return Math.round((answered / questions.value.length) * 100);
  };

  const setLoading = (val) => {
    loading.value = val;
  };

  return {
    exam,
    questions,
    submissionData,
    loading,
    isAnalysis,
    currentIndex,
    currentQuestion,
    totalScore,
    jumpTo,
    nextQuestion,
    prevQuestion,
    loadAnalysisMode,
    loadExamMode,
    formatAnswersForSubmit,
    doSubmit,
    getCorrectAnswer,
    getAnalysis,
    getTeacherComment,
    getQuestionScore,
    isCorrect,
    answerProgressPercent,
    setLoading,
  };
}
