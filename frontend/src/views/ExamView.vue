<script setup>
import { ref, onMounted, nextTick, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getExam, getExamQuestions, submitExam, getSubmission, recordCheating } from '@/api';
import { useAuthStore } from '@/stores/auth';
import { useConfigStore } from '@/stores/config';
import { message, Modal } from 'ant-design-vue';
import {
  ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined,
  LeftOutlined, RightOutlined, FlagOutlined, FlagFilled,
  SendOutlined, AppstoreOutlined, UserOutlined, EyeOutlined,
  InfoCircleOutlined, SafetyOutlined, CheckCircleFilled, CloseCircleFilled
} from '@ant-design/icons-vue';

import { useExamTimer } from '@/composables/useExamTimer';
import { useExamProgress } from '@/composables/useExamProgress';
import { useExamLockdown } from '@/composables/useExamLockdown';
import { useExamSubmission } from '@/composables/useExamSubmission';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const configStore = useConfigStore();
const examId = route.params.id;
const submissionId = route.query.submissionId;

const submission = useExamSubmission({
  getExamApi: getExam,
  getExamQuestionsApi: getExamQuestions,
  submitExamApi: submitExam,
  getSubmissionApi: getSubmission,
  onLoadError: () => {
    message.error('加载失败');
    router.push('/dashboard');
  },
  onSubmitError: () => {
    message.error('提交失败');
  },
  onSubmitSuccess: (data) => {
    Modal.success({
      title: '交卷成功',
      content: `您的考试得分：${data.score} 分。点击确定后将返回成绩单。`,
      onOk: () => {
        router.push({ path: '/dashboard', query: { tab: 'scores' } });
      },
    });
  },
});

const timer = useExamTimer(() => {
  handleSubmit(true);
});

const progress = useExamProgress(
  () => examId,
  () => authStore.user?.id,
  () => !submission.isAnalysis.value
);

const lockdown = useExamLockdown({
  examId: () => examId,
  isExamMode: () => !submission.isAnalysis.value,
  getExamConfig: () => submission.exam.value,
  recordCheatingApi: recordCheating,
  onForceSubmit: () => handleSubmit(true),
});

const videoRef = lockdown.videoRef;

const handleSubmit = async (auto = false) => {
  if (!auto) {
    Modal.confirm({
      title: '确认交卷',
      content: '确定要提交试卷吗？提交前请检查是否还有遗漏。',
      okText: '确认交卷',
      cancelText: '继续检查',
      onOk: async () => {
        await performSubmit();
      },
    });
  } else {
    message.info('考试时间到，自动交卷');
    await performSubmit();
  }
};

const performSubmit = async () => {
  timer.stopTimer();
  lockdown.cleanupAll();
  progress.clearProgress();
  await submission.doSubmit(examId, progress.answers.value);
};

const handleToggleFlag = () => {
  const qId = submission.currentQuestion.value.question.id;
  progress.toggleFlag(qId);
};

const handleCurrentFlag = computed(() => {
  if (!submission.currentQuestion.value) return false;
  return progress.isFlagged(submission.currentQuestion.value.question.id);
});

const fetchData = async () => {
  try {
    if (submissionId) {
      const { restoredAnswers } = await submission.loadAnalysisMode(submissionId);
      Object.assign(progress.answers.value, restoredAnswers);
    } else {
      const { duration, enableCamera } = await submission.loadExamMode(examId);
      progress.restoreProgress();
      progress.startAutoSave();
      timer.startTimer(duration);

      await nextTick();
      lockdown.initAll(enableCamera);
    }
  } catch (e) {
    console.error(e);
  } finally {
    submission.setLoading(false);
  }
};

onMounted(() => {
  fetchData();
});

const isCorrect = (qId) => submission.isCorrect(qId, progress.answers.value[qId]);
</script>

<template>
  <div class="exam-page-container" v-if="submission.exam.value">
    <a-layout class="full-height-layout">
      <!-- Fixed Sticky Header -->
      <a-layout-header class="prof-exam-header">
         <div class="header-left">
           <div class="brand-logo" v-if="configStore.logoUrl">
             <img :src="configStore.logoUrl" alt="logo" />
           </div>
           <div class="exam-meta-info">
             <h2 class="exam-title-text">{{ submission.exam.value.title }}</h2>
             <span class="course-tag">{{ submission.exam.value.course }}</span>
           </div>
         </div>
         
         <div class="header-center">
            <div class="timer-display" :class="{ 'urgent': timer.isUrgent() }" v-if="!submission.isAnalysis.value">
               <span class="time-label">剩余时间</span>
               <span class="time-val">{{ timer.formatTime(timer.timeLeft.value) }}</span>
               <div class="progress-under">
                 <div class="progress-bar" :style="{ width: timer.progressPercent() + '%' }"></div>
               </div>
            </div>
            <div v-else class="analysis-badge">
               <EyeOutlined /> 试卷解析预览模式
            </div>
         </div>
         
         <div class="header-right">
            <div class="user-id-badge">
               <UserOutlined /> {{ authStore.user?.fullName }} ({{ authStore.user?.username }})
            </div>
            <a-button type="primary" danger shape="round" @click="handleSubmit(false)" v-if="!submission.isAnalysis.value" class="finish-exam-btn">
               提交试卷
            </a-button>
            <a-button shape="round" @click="router.push('/dashboard')" v-else>
               退出解析
            </a-button>
         </div>
      </a-layout-header>
      
      <a-layout class="exam-main-layout">
         <!-- Left Sider: Answer Sheet & Monitor -->
         <a-layout-sider width="320" theme="light" class="prof-exam-sider" :trigger="null">
            <div class="sider-inner">
               <div class="navigator-card">
                  <div class="nav-header">
                     <AppstoreOutlined /> 答题卡导航
                  </div>
                  <div class="nav-indicators">
                     <div class="ind-item"><span class="dot current"></span> 当前</div>
                     <div class="ind-item"><span class="dot done"></span> 已答</div>
                     <div class="ind-item"><span class="dot flag"></span> 标记</div>
                     <div class="ind-item"><span class="dot"></span> 未答</div>
                  </div>
                  <div class="nav-grid">
                     <div 
                        v-for="(q, idx) in submission.questions.value" 
                        :key="q.id" 
                        class="nav-cell"
                        :class="{ 
                           'is-active': submission.currentIndex.value === idx,
                           'is-done': progress.isAnswered(q.question.id),
                           'is-flagged': progress.isFlagged(q.question.id)
                        }"
                        @click="submission.jumpTo(idx)"
                     >
                        {{ idx + 1 }}
                     </div>
                  </div>
               </div>
               
               <div v-if="!submission.isAnalysis.value && submission.exam.value.enableCamera" class="monitor-card">
                  <div class="monitor-head">智能监考系统</div>
                  <div class="video-container">
                    <video ref="videoRef" autoplay playsinline muted></video>
                    <div class="live-status">
                       <span class="status-pulse"></span> REC 实时监控中
                    </div>
                  </div>
               </div>

               <div class="exam-rules-mini">
                  <p><InfoCircleOutlined /> 系统检测到切屏将立即计入次数</p>
                  <p v-if="!submission.exam.value.allowTabSwitch"><SafetyOutlined /> 切屏限制: {{ lockdown.tabSwitchCount.value }} / {{ submission.exam.value.tabSwitchLimit }}</p>
               </div>
            </div>
         </a-layout-sider>

         <!-- Right Content: Question Content -->
         <a-layout-content class="prof-exam-content">
            <div class="question-canvas" v-if="submission.currentQuestion.value">
               <div class="q-scope-header">
                  <div class="q-type-label">
                     <a-tag color="blue">
                        {{ { 'SINGLE': '单选题', 'MULTI': '多选题', 'JUDGE': '判断题', 'SHORT': '简答题' }[submission.currentQuestion.value.question.type] }}
                     </a-tag>
                     <span class="q-index-info">第 {{ submission.currentIndex.value + 1 }} 题 / 共 {{ submission.questions.value.length }} 题</span>
                  </div>
                  <div class="q-actions-right">
                    <span class="q-score-tag">分值: {{ submission.currentQuestion.value.score }}分</span>
                    <a-button 
                      v-if="!submission.isAnalysis.value"
                      type="text" 
                      class="flag-btn" 
                      @click="handleToggleFlag"
                      :class="{ 'flagged-active': handleCurrentFlag }"
                    >
                      <template #icon>
                        <FlagFilled v-if="handleCurrentFlag" />
                        <FlagOutlined v-else />
                      </template>
                      标记此题
                    </a-button>
                  </div>
               </div>

               <div class="q-content-body">
                  <div class="question-text" v-html="submission.currentQuestion.value.question.content"></div>
                  
                  <div class="answer-interaction-area">
                     <!-- SINGLE -->
                     <a-radio-group v-if="submission.currentQuestion.value.question.type === 'SINGLE'" v-model:value="progress.answers.value[submission.currentQuestion.value.question.id]" :disabled="submission.isAnalysis.value" class="choice-group">
                        <a-radio class="choice-item" v-for="opt in JSON.parse(submission.currentQuestion.value.question.options)" :key="opt.label" :value="opt.label">
                           <span class="choice-key">{{ opt.label }}</span>
                           <span class="choice-text">{{ opt.text }}</span>
                        </a-radio>
                     </a-radio-group>
                     
                     <!-- MULTI -->
                     <a-checkbox-group v-if="submission.currentQuestion.value.question.type === 'MULTI'" v-model:value="progress.answers.value[submission.currentQuestion.value.question.id]" :disabled="submission.isAnalysis.value" class="choice-group">
                        <div v-for="opt in JSON.parse(submission.currentQuestion.value.question.options)" :key="opt.label" class="choice-item multi-wrap">
                           <a-checkbox :value="opt.label">
                              <span class="choice-key">{{ opt.label }}</span>
                              <span class="choice-text">{{ opt.text }}</span>
                           </a-checkbox>
                        </div>
                     </a-checkbox-group>

                     <!-- JUDGE -->
                     <a-radio-group v-if="submission.currentQuestion.value.question.type === 'JUDGE'" v-model:value="progress.answers.value[submission.currentQuestion.value.question.id]" :disabled="submission.isAnalysis.value" class="judge-group">
                        <a-radio-button value="TRUE" class="judge-btn">正确 (TRUE)</a-radio-button>
                        <a-radio-button value="FALSE" class="judge-btn">错误 (FALSE)</a-radio-button>
                     </a-radio-group>
                     
                     <!-- SHORT -->
                     <a-textarea 
                        v-if="submission.currentQuestion.value.question.type === 'SHORT'" 
                        v-model:value="progress.answers.value[submission.currentQuestion.value.question.id]" 
                        placeholder="在此输入您的回答..."
                        :rows="12" 
                        :disabled="submission.isAnalysis.value" 
                        class="essay-editor"
                     />
                  </div>
               </div>

               <!-- Analysis View (Visible only during review) -->
               <div v-if="submission.isAnalysis.value" class="prof-analysis-section">
                  <div class="analysis-header-row">
                     <div class="judge-banner" :class="isCorrect(submission.currentQuestion.value.question.id) ? 'correct' : 'wrong'">
                        <CheckCircleFilled v-if="isCorrect(submission.currentQuestion.value.question.id)" />
                        <CloseCircleFilled v-else />
                        <span>{{ isCorrect(submission.currentQuestion.value.question.id) ? '回答正确' : '回答错误' }}</span>
                     </div>
                     <div class="analysis-stat-info">
                        本题得分: <span class="score-num">{{ submission.getQuestionScore(submission.currentQuestion.value.question.id) }}</span> / {{ submission.currentQuestion.value.score }}
                     </div>
                  </div>
                  
                  <div class="analysis-details-grid">
                     <div class="detail-col">
                        <div class="detail-label">我的作答</div>
                        <div class="detail-val" :class="{ 'error-text': !isCorrect(submission.currentQuestion.value.question.id) }">
                           {{ progress.answers.value[submission.currentQuestion.value.question.id] || '(未答)' }}
                        </div>
                     </div>
                     <div class="detail-col">
                        <div class="detail-label">正确答案</div>
                        <div class="detail-val correct-text">{{ submission.getCorrectAnswer(submission.currentQuestion.value.question.id) }}</div>
                     </div>
                  </div>

                  <div class="teacher-comment-box" v-if="submission.getTeacherComment(submission.currentQuestion.value.question.id)">
                     <div class="comment-label">教师评语</div>
                     <div class="comment-content">{{ submission.getTeacherComment(submission.currentQuestion.value.question.id) }}</div>
                  </div>

                  <div class="explanation-box">
                     <div class="exp-label">解析说明</div>
                     <div class="exp-content">{{ submission.getAnalysis(submission.currentQuestion.value.question.id) || '暂无解析数据' }}</div>
                  </div>
               </div>
            </div>

            <!-- Enhanced Sticky Pager -->
            <div class="prof-exam-pager">
               <div class="pager-left">
                  <a-button @click="submission.prevQuestion" :disabled="submission.currentIndex.value === 0" size="large" ghost type="primary" class="nav-btn">
                     <LeftOutlined /> 上一题
                  </a-button>
               </div>
               <div class="pager-center">
                  进度: <a-progress :percent="submission.answerProgressPercent(progress.answers.value)" size="small" style="width: 200px" />
               </div>
               <div class="pager-right">
                  <a-button type="primary" @click="submission.nextQuestion" :disabled="submission.currentIndex.value === submission.questions.value.length - 1" size="large" class="nav-btn next">
                     下一题 <RightOutlined />
                  </a-button>
               </div>
            </div>
         </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
  <div v-else class="prof-exam-loading">
    <a-spin size="large" tip="系统正在加载试卷，请稍候..." />
  </div>
</template>

<style scoped>
.exam-page-container {
  height: 100vh;
  background-color: #f7f9fc;
}
.full-height-layout {
  height: 100%;
}

/* Header Styles */
.prof-exam-header {
  height: 72px;
  background: white;
  border-bottom: 2px solid #e1e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32px;
  line-height: normal;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.brand-logo img {
  height: 36px;
}
.exam-title-text {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #1a1a1a;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.course-tag {
  font-size: 12px;
  color: #8c8c8c;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}
.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 240px;
}
.time-label {
  font-size: 11px;
  color: #999;
  text-transform: uppercase;
  margin-bottom: 2px;
}
.time-val {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  font-family: 'JetBrains Mono', 'Monaco', monospace;
  line-height: 1;
}
.timer-display.urgent .time-val {
  color: #f5222d;
  animation: timer-pulse 1s infinite;
}
.progress-under {
  width: 100%;
  height: 4px;
  background: #e1e8f0;
  border-radius: 2px;
  margin-top: 6px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background: #1890ff;
  transition: width 0.3s;
}
.timer-display.urgent .progress-bar {
  background: #f5222d;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}
.user-id-badge {
  color: #595959;
  font-size: 14px;
}
.finish-exam-btn {
  font-weight: 600;
  padding: 0 28px;
  height: 40px;
}

/* Sider Styles */
.prof-exam-sider {
  border-right: 1px solid #e1e8f0;
  overflow-y: auto;
}
.sider-inner {
  padding: 24px;
}
.navigator-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.nav-header {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #262626;
}
.nav-indicators {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 24px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}
.ind-item {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 1px solid #d9d9d9;
}
.dot.current { background: #1890ff; border-color: #1890ff; }
.dot.done { background: #e6f7ff; border-color: #91d5ff; }
.dot.flag { background: #faad14; border-color: #faad14; }

.nav-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}
.nav-cell {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e1e8f0;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  font-size: 14px;
}
.nav-cell:hover {
  border-color: #1890ff;
  color: #1890ff;
}
.nav-cell.is-done {
  background: #e6f7ff;
  border-color: #91d5ff;
}
.nav-cell.is-flagged {
  background: #fffbe6;
  border-color: #faad14;
  position: relative;
}
.nav-cell.is-active {
  background: #1890ff !important;
  border-color: #1890ff !important;
  color: white !important;
  box-shadow: 0 4px 8px rgba(24, 144, 255, 0.35);
}

.monitor-card {
  margin-top: 24px;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}
.monitor-head {
  padding: 8px 12px;
  font-size: 10px;
  color: rgba(255,255,255,0.7);
  background: rgba(40,40,40,0.8);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1;
}
.video-container {
  width: 100%;
  aspect-ratio: 16/10;
}
.video-container video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.live-status {
  position: absolute;
  bottom: 8px;
  right: 8px;
  font-size: 9px;
  color: #52c41a;
  background: rgba(0,0,0,0.6);
  padding: 2px 8px;
  border-radius: 10px;
}
.status-pulse {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  margin-right: 4px;
  animation: status-pulse 1.5s infinite;
}

.exam-rules-mini {
  margin-top: 24px;
  padding: 16px;
  background: #fff1f0;
  border-radius: 8px;
  font-size: 12px;
  color: #cf1322;
}
.exam-rules-mini p {
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Content Area Styles */
.prof-exam-content {
  padding: 40px 60px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}
.question-canvas {
  background: white;
  min-height: 600px;
  border-radius: 16px;
  padding: 48px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
  margin-bottom: 40px;
  flex: 1;
}

.q-scope-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}
.q-index-info {
  margin-left: 12px;
  font-size: 14px;
  color: #8c8c8c;
}
.q-score-tag {
  color: #595959;
  font-size: 14px;
  margin-right: 20px;
}
.flag-btn {
  color: #8c8c8c;
}
.flagged-active {
  color: #faad14 !important;
}

.question-text {
  font-size: 20px;
  line-height: 1.8;
  color: #1a1a1a;
  margin-bottom: 40px;
}

.answer-interaction-area {
  margin-top: 24px;
}
.choice-group {
  width: 100%;
}
.choice-item {
  width: 100%;
  margin-bottom: 16px !important;
  display: flex !important;
  align-items: center;
  padding: 16px 24px;
  border: 1px solid #e1e8f0;
  border-radius: 12px;
  transition: all 0.2s;
  font-size: 16px;
}
.choice-item:hover {
  border-color: #1890ff;
  background: #f0f7ff;
}
.choice-item.ant-radio-wrapper-checked, 
.choice-item.ant-checkbox-wrapper-checked {
  border-color: #1890ff;
  background: #f0f7ff;
}
.choice-key {
  font-weight: 700;
  margin-right: 12px;
  color: #1890ff;
}
.choice-text {
  color: #262626;
}

.judge-group {
  display: flex;
  gap: 16px;
}
.judge-btn {
  height: 50px;
  min-width: 160px;
  text-align: center;
  line-height: 48px;
}

.essay-editor {
  border-radius: 12px;
  padding: 24px;
  font-size: 17px;
  line-height: 1.8;
  background: #f8fafc;
}

/* Sticky Pager */
.prof-exam-pager {
  position: sticky;
  bottom: 0px;
  background: white;
  padding: 20px 40px;
  border-radius: 12px;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 99;
}
.nav-btn {
  min-width: 140px;
  height: 46px;
  font-weight: 600;
  border-radius: 23px;
}

/* Analysis Styles */
.prof-analysis-section {
  margin-top: 48px;
  background: #f8fafc;
  padding: 32px;
  border-radius: 16px;
  border-left: 6px solid #1890ff;
}
.analysis-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.judge-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
}
.judge-banner.correct { color: #52c41a; }
.judge-banner.wrong { color: #f5222d; }

.score-num {
  font-size: 24px;
  color: #1890ff;
  font-weight: 800;
}
.analysis-details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}
.detail-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}
.detail-val {
  font-size: 18px;
  font-weight: 600;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  border: 1px dashed #d9d9d9;
}
.correct-text { color: #52c41a; border-color: #b7eb8f; background: #f6ffed; }
.error-text { color: #f5222d; border-color: #ffa39e; background: #fff1f0; }

.teacher-comment-box, .explanation-box {
  margin-bottom: 24px;
}
.comment-label, .exp-label {
  font-weight: 600;
  color: #262626;
  margin-bottom: 10px;
}
.comment-content {
  background: #e6f7ff;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #91d5ff;
  color: #0050b3;
}
.exp-content {
  line-height: 1.8;
  color: #595959;
}

.prof-exam-loading {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
}

@keyframes timer-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}
@keyframes status-pulse {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

/* Custom Checkbox/Radio overrides for premium feel */
:deep(.ant-radio-wrapper::after), :deep(.ant-checkbox-wrapper::after) {
  display: none !important;
}
:deep(.ant-radio-inner), :deep(.ant-checkbox-inner) {
  width: 20px;
  height: 20px;
}
:deep(.choice-text) {
  white-space: normal;
}
</style>
