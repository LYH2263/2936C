package com.exam.controller;

import com.exam.dto.StudentStatsDTO;
import com.exam.dto.SubmissionGradeDTO;
import com.exam.dto.TeacherStatsDTO;
import com.exam.entity.Submission;
import com.exam.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Tag(name = "提交管理", description = "学生交卷、成绩查询、教师批改、统计数据等接口")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private static final Logger log = LoggerFactory.getLogger(SubmissionController.class);

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Operation(summary = "学生交卷", description = "学生提交一场考试的答案，系统对客观题自动判分并保存提交记录（敏感接口，包含 requestId 日志追踪）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提交成功，返回提交记录及得分"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "考试不存在或参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "考试已提交")
    })
    @PostMapping("/{examId}")
    public Submission submitExam(
            @Parameter(description = "考试 ID", required = true, example = "1")
            @PathVariable Long examId,
            @RequestBody Map<Long, String> answers,
            Principal principal) {
        String username = principal.getName();
        String requestId = MDC.get("requestId");
        log.info("[requestId={}] Submission attempt: user={}, examId={}, answerCount={}",
                requestId, username, examId, answers != null ? answers.size() : 0);
        try {
            Submission submission = submissionService.submitExam(examId, answers, username);
            log.info("[requestId={}] Submission successful: user={}, examId={}, submissionId={}, score={}",
                    requestId, username, examId, submission.getId(), submission.getScore());
            return submission;
        } catch (Exception e) {
            log.error("[requestId={}] Submission failed: user={}, examId={}",
                    requestId, username, examId, e);
            throw e;
        }
    }

    @Operation(summary = "查询我的提交记录", description = "当前登录学生查询自己的全部提交记录，含排名和总分")
    @GetMapping("/my")
    public List<Submission> getMySubmissions(Principal principal) {
        return submissionService.getStudentSubmissions(principal.getName());
    }

    @Operation(summary = "查询考试的全部提交记录", description = "查询指定考试下所有学生的提交记录（教师/管理员可见）")
    @GetMapping("/exam/{examId}")
    public List<Submission> getExamSubmissions(@PathVariable Long examId) {
        return submissionService.getExamSubmissions(examId);
    }

    @Operation(summary = "查询单条提交详情", description = "根据提交 ID 查询详细信息，包含各题答案、得分、排名")
    @GetMapping("/{id}")
    public Submission getSubmission(
            @Parameter(description = "提交记录 ID", required = true, example = "1")
            @PathVariable Long id) {
        return submissionService.getSubmissionById(id);
    }

    @Operation(summary = "教师批改主观题", description = "教师对主观题进行评分并填写评语，更新提交总分")
    @PostMapping("/{id}/grade")
    public void gradeSubmission(@PathVariable Long id, @RequestBody Map<Long, SubmissionGradeDTO> grades) {
        submissionService.gradeSubmission(id, grades);
    }

    @Operation(summary = "获取学生个人统计", description = "当前登录学生获取自己的考试统计：参加考试数、平均分、及格率、待考数、最近提交")
    @GetMapping("/stats")
    public StudentStatsDTO getMyStats(Principal principal) {
        return submissionService.getStudentStats(principal.getName());
    }

    @Operation(summary = "获取教师全局统计", description = "获取全局数据：总考试数、总提交数、活跃考试数、学生总数、最近提交")
    @GetMapping("/teacher-stats")
    public TeacherStatsDTO getTeacherStats() {
        return submissionService.getTeacherStats();
    }
}
