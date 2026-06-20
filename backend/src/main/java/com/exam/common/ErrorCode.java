package com.exam.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    SUCCESS(0, "操作成功", HttpStatus.OK),

    BAD_REQUEST(40000, "请求参数错误", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(40001, "参数校验失败", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(40002, "账号或密码错误", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(40003, "用户名已存在", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(40004, "密码长度不足", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(40100, "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(40101, "Token 无效", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(40102, "Token 已过期", HttpStatus.UNAUTHORIZED),

    FORBIDDEN(40300, "权限不足", HttpStatus.FORBIDDEN),
    ROLE_FORBIDDEN(40301, "角色权限不足", HttpStatus.FORBIDDEN),

    NOT_FOUND(40400, "资源不存在", HttpStatus.NOT_FOUND),
    EXAM_NOT_FOUND(40401, "考试不存在", HttpStatus.NOT_FOUND),
    QUESTION_NOT_FOUND(40402, "题目不存在", HttpStatus.NOT_FOUND),
    SUBMISSION_NOT_FOUND(40403, "提交记录不存在", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(40404, "用户不存在", HttpStatus.NOT_FOUND),

    CONFLICT(40900, "资源冲突", HttpStatus.CONFLICT),
    EXAM_ALREADY_SUBMITTED(40901, "考试已提交", HttpStatus.CONFLICT),
    EXAM_NOT_PUBLISHED(40902, "考试未发布", HttpStatus.CONFLICT),

    INTERNAL_ERROR(50000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_ERROR(50001, "文件上传失败", HttpStatus.INTERNAL_SERVER_ERROR),
    EXPORT_ERROR(50002, "导出失败", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
