package com.logistics.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.annotation.Log;
import com.logistics.entity.SysOperationLog;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.regex.Pattern;

/**
 * 操作日志 AOP 切面：拦截 @Log 标注的方法，自动记录操作日志到 sys_operation_log
 */
@Aspect
@Component
public class LogAspect {

    /** 敏感字段（密码类）脱敏：字段名不区分大小写 */
    private static final Pattern SENSITIVE_FIELD_PATTERN = Pattern.compile(
            "(\"(?:password|pwd|oldPassword|newPassword|confirmPassword)\"\\s*:\\s*)\"[^\"]*\"",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AsyncLogWriter asyncLogWriter;

    @Around("@annotation(com.logistics.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取当前用户
        Long userId = null;
        String username = "anonymous";
        String realName = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(auth.getName())) {
                username = auth.getName();
                SysUser user = sysUserRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    userId = user.getId();
                    realName = user.getRealName();
                }
            }
        }

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        String clientIp = request != null ? request.getRemoteAddr() : null;
        String requestUrl = request != null ? request.getRequestURI() : null;
        String requestMethod = request != null ? request.getMethod() : null;

        // 获取注解参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);
        String module = logAnnotation.module();
        String operationDesc = logAnnotation.operation();
        String operationType = logAnnotation.type();

        // 获取请求参数（限制长度，防止超长）
        String requestParams = "";
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                String json = objectMapper.writeValueAsString(args);
                requestParams = json.length() > 2000 ? json.substring(0, 2000) + "..." : json;
            }
        } catch (Exception e) {
            requestParams = "参数序列化失败";
        }
        // 敏感信息脱敏（密码等）
        requestParams = maskSensitive(requestParams);

        Object result = null;
        String exceptionMsg = null;
        int responseCode = 200;

        try {
            result = joinPoint.proceed();
            // 如果返回 Result，提取 code
            if (result != null && result.getClass().getSimpleName().equals("Result")) {
                try {
                    responseCode = (int) result.getClass().getMethod("getCode").invoke(result);
                } catch (Exception ignored) {
                }
            }
            return result;
        } catch (Throwable e) {
            exceptionMsg = e.getMessage();
            responseCode = 500;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;

            SysOperationLog log = new SysOperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setRealName(realName);
            log.setModule(module);
            log.setOperationType(operationType);
            log.setOperationDesc(operationDesc);
            log.setRequestUrl(requestUrl);
            log.setRequestMethod(requestMethod);
            log.setRequestParams(requestParams);
            log.setResponseCode(responseCode);
            log.setCostTime((int) costTime);
            log.setClientIp(clientIp);
            log.setExceptionMsg(exceptionMsg);
            log.setCreateTime(OffsetDateTime.now());

            // 异步落库，避免影响主业务响应时间
            asyncLogWriter.save(log);
        }
    }

    /**
     * 敏感字段脱敏：将密码类字段值替换为 ***
     */
    private String maskSensitive(String json) {
        if (!StringUtils.hasText(json)) {
            return json;
        }
        return SENSITIVE_FIELD_PATTERN.matcher(json).replaceAll("$1\"***\"");
    }
}
