package com.exam.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "统一响应结构")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "业务状态码，0 表示成功", example = "0")
    private int code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应时间戳 (ISO 8601)", example = "2024-01-01T00:00:00Z")
    private Instant timestamp;

    @Schema(description = "请求路径", example = "/api/exams")
    private String path;

    @Schema(description = "响应数据")
    private T data;

    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    public ApiResponse(int code, String message, T data, String path) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.path = path;
        this.timestamp = Instant.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, null);
    }

    public static <T> ApiResponse<T> success(T data, String path) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data, path);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), message, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String path) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null, path);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage, String path) {
        return new ApiResponse<>(errorCode.getCode(), customMessage, null, path);
    }

    public static <T> ApiResponse<T> error(int code, String message, String path) {
        return new ApiResponse<>(code, message, null, path);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
