# 错误码表

统一响应结构：

```json
{
  "code": 0,
  "message": "操作成功",
  "timestamp": "2024-01-01T00:00:00Z",
  "path": "/api/exams",
  "data": { }
}
```

| 错误码 (code) | HTTP 状态 | 含义 | 说明 |
|--------------|-----------|------|------|
| 0 | 200 OK | 操作成功 | 业务处理成功，data 字段为返回数据 |
| 40000 | 400 Bad Request | 请求参数错误 | 通用请求参数错误 |
| 40001 | 400 Bad Request | 参数校验失败 | @Valid 等校验注解不通过，message 包含具体字段错误 |
| 40002 | 400 Bad Request | 账号或密码错误 | 登录时用户名或密码不正确 |
| 40003 | 400 Bad Request | 用户名已存在 | 注册时用户名重复 |
| 40004 | 400 Bad Request | 密码长度不足 | 注册或修改密码时密码长度低于配置最小值 |
| 40100 | 401 Unauthorized | 未登录或登录已过期 | 通用未认证错误 |
| 40101 | 401 Unauthorized | Token 无效 | JWT Token 格式错误或签名校验失败 |
| 40102 | 401 Unauthorized | Token 已过期 | JWT Token 超过有效期 |
| 40300 | 403 Forbidden | 权限不足 | 通用无权限错误 |
| 40301 | 403 Forbidden | 角色权限不足 | 当前用户角色不允许调用该接口 |
| 40400 | 404 Not Found | 资源不存在 | 通用资源未找到 |
| 40401 | 404 Not Found | 考试不存在 | 通过 ID 查询考试时未找到 |
| 40402 | 404 Not Found | 题目不存在 | 通过 ID 查询题目时未找到 |
| 40403 | 404 Not Found | 提交记录不存在 | 通过 ID 查询提交时未找到 |
| 40404 | 404 Not Found | 用户不存在 | 通过用户名或 ID 查询用户时未找到 |
| 40900 | 409 Conflict | 资源冲突 | 通用资源冲突 |
| 40901 | 409 Conflict | 考试已提交 | 重复提交同一场考试 |
| 40902 | 409 Conflict | 考试未发布 | 学生尝试参加未发布的考试 |
| 50000 | 500 Internal Server Error | 服务器内部错误 | 未捕获的异常，堆栈已记录到日志 |
| 50001 | 500 Internal Server Error | 文件上传失败 | Multipart 文件上传异常 |
| 50002 | 500 Internal Server Error | 导出失败 | CSV / Excel 等文件导出异常 |

## 编码规范

- `0` 表示成功，非 `0` 表示失败
- `4xxxxx` 表示客户端错误（4xx HTTP 状态码）
- `5xxxxx` 表示服务端错误（5xx HTTP 状态码）
- 细分错误码使用第 3-5 位：例如 40401 = 404(NOT_FOUND) + 01(EXAM)

## Swagger 注解示例（≥5 个）

### 1. 考试列表接口（ExamController.getExams）

```java
@Operation(summary = "获取考试列表", description = "根据当前用户角色返回可见的考试列表")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回考试列表"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "权限不足")
})
@GetMapping
public List<Exam> getExams(Principal principal) { ... }
```

### 2. 考试详情接口（ExamController.getExam）

```java
@Operation(summary = "获取考试详情", description = "根据 ID 查询单个考试的完整信息")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功返回考试详情"),
        @ApiResponse(responseCode = "404", description = "考试不存在")
})
@GetMapping("/{id}")
public Exam getExam(@Parameter(description = "考试 ID", required = true, example = "1")
                    @PathVariable Long id) { ... }
```

### 3. 创建考试接口（ExamController.createExam）

```java
@Operation(summary = "创建考试", description = "创建一个新的考试（草稿状态）。仅 TEACHER 和 ADMIN 角色可调用")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "创建成功"),
        @ApiResponse(responseCode = "400", description = "参数校验失败"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "403", description = "需要 TEACHER 或 ADMIN 角色")
})
@PostMapping
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public Exam createExam(@RequestBody Exam exam, Principal principal) { ... }
```

### 4. 学生交卷接口（SubmissionController.submitExam）

```java
@Operation(summary = "学生交卷", description = "学生提交一场考试的答案，系统对客观题自动判分（敏感接口，含 requestId 日志追踪）")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "提交成功，返回提交记录及得分"),
        @ApiResponse(responseCode = "400", description = "考试不存在或参数错误"),
        @ApiResponse(responseCode = "401", description = "未登录"),
        @ApiResponse(responseCode = "409", description = "考试已提交")
})
@PostMapping("/{examId}")
public Submission submitExam(@Parameter(description = "考试 ID", required = true, example = "1")
                             @PathVariable Long examId,
                             @RequestBody Map<Long, String> answers,
                             Principal principal) { ... }
```

### 5. 用户登录接口（AuthController.authenticateUser）

```java
@Operation(summary = "用户登录", description = "根据用户名和密码登录，返回 JWT Token 及用户信息（含 requestId 日志追踪）")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功，返回 token 和用户信息"),
        @ApiResponse(responseCode = "400", description = "账号或密码错误"),
        @ApiResponse(responseCode = "500", description = "登录失败")
})
@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) { ... }
```

### 6. 考试统计接口（ExamController.getExamStatistics）

```java
@Operation(summary = "获取考试统计", description = "返回考试的各项统计数据：平均分、最高分、及格率、排名、题目分析")
@GetMapping("/{id}/statistics")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public ResponseEntity<ExamStatistics> getExamStatistics(@PathVariable Long id) { ... }
```

## 日志 MDC（requestId）

所有请求经过 `RequestIdFilter` 时会自动生成或复用 `X-Request-Id`，写入 SLF4J MDC，日志模式：

```
[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%thread] [%X{requestId:-}] %-5level %logger{36} - %msg%n
```

敏感接口（登录、交卷）在 Controller 层额外输出包含 requestId 的结构化日志，便于链路追踪。
