package com.exam.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String path = request instanceof ServletServerHttpRequest
                ? ((ServletServerHttpRequest) request).getServletRequest().getRequestURI()
                : null;

        if (body instanceof ApiResponse) {
            ApiResponse<?> apiResponse = (ApiResponse<?>) body;
            if (apiResponse.getPath() == null) {
                apiResponse.setPath(path);
            }
            return body;
        }

        if (body instanceof ResponseEntity) {
            ResponseEntity<?> re = (ResponseEntity<?>) body;
            Object innerBody = re.getBody();

            if (innerBody instanceof byte[]) {
                return body;
            }

            if (innerBody instanceof ApiResponse) {
                return body;
            }

            if (innerBody == null) {
                ApiResponse<Void> apiResponse = ApiResponse.<Void>success(null, path);
                return ResponseEntity.status(re.getStatusCode()).headers(re.getHeaders()).body(apiResponse);
            }

            if (innerBody instanceof String) {
                return body;
            }

            ApiResponse<Object> wrapped = ApiResponse.success(innerBody, path);
            return ResponseEntity.status(re.getStatusCode()).headers(re.getHeaders()).body(wrapped);
        }

        if (body instanceof byte[]) {
            return body;
        }

        if (body instanceof String) {
            return body;
        }

        if (body == null) {
            return ApiResponse.<Void>success(null, path);
        }

        return ApiResponse.success(body, path);
    }
}
