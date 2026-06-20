package com.exam.controller;

import com.exam.common.BusinessException;
import com.exam.common.ErrorCode;
import com.exam.entity.User;
import com.exam.repository.UserRepository;
import com.exam.service.SystemConfigService;
import com.exam.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "认证管理", description = "登录、注册、个人资料修改等接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final com.exam.service.UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SystemConfigService systemConfigService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          com.exam.service.UserService userService, PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils, SystemConfigService systemConfigService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.systemConfigService = systemConfigService;
    }

    @Operation(summary = "修改个人资料", description = "修改当前登录用户的姓名和密码")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, String> request,
                                          org.springframework.security.core.Authentication authentication) {
        String username = authentication.getName();
        User user = userService.updateProfile(username, request.get("fullName"), request.get("password"));

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("fullName", user.getFullName());
        response.put("clazz", user.getClazz());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "用户登录", description = "根据用户名和密码登录，返回 JWT Token 及用户信息（敏感接口，包含 requestId 日志追踪）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功，返回 token 和用户信息"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "账号或密码错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "登录失败")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String requestId = MDC.get("requestId");
        log.info("[requestId={}] Login attempt for user: {}", requestId, username);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.get("username"), loginRequest.get("password")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(loginRequest.get("username"));

            User user = userRepository.findByUsername(loginRequest.get("username")).orElseThrow();

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            response.put("clazz", user.getClazz());
            response.put("createdAt", user.getCreatedAt());

            log.info("[requestId={}] Login successful for user: {}", requestId, username);
            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            log.warn("[requestId={}] Login failed for user: {} - invalid credentials", requestId, username);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (Exception e) {
            log.error("[requestId={}] Login error for user: {}", requestId, username, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "登录失败：" + e.getMessage());
        }
    }

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "用户名已存在或密码长度不足")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        int minLength = systemConfigService.getConfig().getPasswordMinLength() != null ?
                        systemConfigService.getConfig().getPasswordMinLength() : 6;
        if (signUpRequest.getPassword() == null || signUpRequest.getPassword().length() < minLength) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT, "密码长度不能少于 " + minLength + " 位");
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(signUpRequest.getRole());
        user.setFullName(signUpRequest.getFullName());
        user.setClazz(signUpRequest.getClazz());

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}
