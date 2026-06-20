package com.exam.service;

import com.exam.dto.ExamStatistics;
import com.exam.dto.ExamStatistics.QuestionStat;
import com.exam.dto.ExamStatistics.StudentRanking;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExamServiceStatisticsTest {

    @Autowired
    private ExamService examService;

    @Test
    @DisplayName("5题满分100: 及格/不及格/缺考 → averageScore=60, passRate=50%, absentCount=1, 排名降序, 题目正确率一致")
    @Sql({"classpath:sql/cleanup.sql", "classpath:sql/seed-statistics.sql"})
    void testGetExamStatistics_MainFixture() {
        ExamStatistics stats = examService.getExamStatistics(1L);

        assertEquals(1L, stats.getExamId());
        assertEquals("统计测试考试", stats.getTitle());

        assertEquals(3, stats.getTotalParticipants());
        assertEquals(2, stats.getTotalSubmissions());
        assertEquals(1, stats.getAbsentCount());

        assertEquals(60.0, stats.getAverageScore(), 0.01);
        assertEquals(75, stats.getMaxScore());
        assertEquals(45, stats.getMinScore());

        assertEquals(50.0, stats.getPassRate(), 0.01);

        Map<String, Integer> dist = stats.getScoreDistribution();
        assertNotNull(dist);
        assertEquals(5, dist.size());
        assertEquals(1, dist.get("0-59"));
        assertEquals(0, dist.get("60-69"));
        assertEquals(1, dist.get("70-79"));
        assertEquals(0, dist.get("80-89"));
        assertEquals(0, dist.get("90-100"));

        List<StudentRanking> rankings = stats.getRankings();
        assertNotNull(rankings);
        assertEquals(2, rankings.size());
        assertEquals(1, rankings.get(0).getRank());
        assertEquals("student_pass", rankings.get(0).getUsername());
        assertEquals(75, rankings.get(0).getScore());
        assertEquals(2, rankings.get(1).getRank());
        assertEquals("student_fail", rankings.get(1).getUsername());
        assertEquals(45, rankings.get(1).getScore());
        assertTrue(rankings.get(0).getScore() >= rankings.get(1).getScore());

        List<QuestionStat> qStats = stats.getQuestionAnalysis();
        assertNotNull(qStats);
        assertEquals(5, qStats.size());

        assertQuestionStat(qStats.get(0), 1L, 20.0, 100.0);
        assertQuestionStat(qStats.get(1), 2L, 15.0, 50.0);
        assertQuestionStat(qStats.get(2), 3L, 7.5, 0.0);
        assertQuestionStat(qStats.get(3), 4L, 17.5, 50.0);
        assertQuestionStat(qStats.get(4), 5L, 0.0, 0.0);
    }

    @Test
    @DisplayName("零提交考试: absentCount=总人数, 其余字段为默认值")
    @Sql({"classpath:sql/cleanup.sql", "classpath:sql/seed-zero-submission.sql"})
    void testGetExamStatistics_ZeroSubmissions() {
        ExamStatistics stats = examService.getExamStatistics(1L);

        assertEquals(0, stats.getTotalSubmissions());
        assertEquals(3, stats.getTotalParticipants());
        assertEquals(3, stats.getAbsentCount());

        assertEquals(0.0, stats.getAverageScore(), 0.01);
        assertEquals(0, stats.getMaxScore());
        assertEquals(0, stats.getMinScore());
        assertEquals(0.0, stats.getPassRate(), 0.01);

        assertNull(stats.getScoreDistribution());
        assertNull(stats.getRankings());
        assertNull(stats.getQuestionAnalysis());
    }

    @Test
    @DisplayName("BUG回归: 满分50时绝对分分段错误 — 期望百分比分段行为")
    @Sql({"classpath:sql/cleanup.sql", "classpath:sql/seed-percentage-bug.sql"})
    void testGetExamStatistics_PercentageDistributionBug() {
        ExamStatistics stats = examService.getExamStatistics(2L);

        assertEquals(2, stats.getTotalSubmissions());
        assertEquals(2, stats.getTotalParticipants());
        assertEquals(0, stats.getAbsentCount());

        assertEquals(35.0, stats.getAverageScore(), 0.01);
        assertEquals(40, stats.getMaxScore());
        assertEquals(30, stats.getMinScore());

        assertEquals(100.0, stats.getPassRate(), 0.01);

        Map<String, Integer> dist = stats.getScoreDistribution();
        assertNotNull(dist);

        // BUG REGRESSION: getExamStatistics uses absolute score thresholds
        // (0-59, 60-69, 70-79, 80-89, 90-100) instead of percentage-based
        // thresholds. For an exam with totalPossibleScore=50:
        //   - Student score 40/50 = 80% → should be in "80-89" bucket
        //   - Student score 30/50 = 60% → should be in "60-69" bucket
        // Current buggy code places both in "0-59" because 40 < 60 and 30 < 60.
        // After fixing to use percentage-based distribution, these assertions pass.
        assertEquals(0, dist.get("0-59"), "30/50=60% and 40/50=80% should NOT be in 0-59");
        assertEquals(1, dist.get("60-69"), "30/50=60% should be in 60-69");
        assertEquals(0, dist.get("70-79"));
        assertEquals(1, dist.get("80-89"), "40/50=80% should be in 80-89");
        assertEquals(0, dist.get("90-100"));

        List<StudentRanking> rankings = stats.getRankings();
        assertNotNull(rankings);
        assertEquals(2, rankings.size());
        assertEquals("student_80pct", rankings.get(0).getUsername());
        assertEquals(40, rankings.get(0).getScore());
        assertEquals("student_60pct", rankings.get(1).getUsername());
        assertEquals(30, rankings.get(1).getScore());
    }

    private void assertQuestionStat(QuestionStat qs, long expectedQuestionId,
                                    double expectedAvgScore, double expectedCorrectRate) {
        assertEquals(expectedQuestionId, qs.getQuestionId());
        assertEquals(expectedAvgScore, qs.getAverageScore(), 0.01,
                "Q" + expectedQuestionId + " averageScore mismatch");
        assertEquals(expectedCorrectRate, qs.getCorrectRate(), 0.01,
                "Q" + expectedQuestionId + " correctRate mismatch");
    }
}
