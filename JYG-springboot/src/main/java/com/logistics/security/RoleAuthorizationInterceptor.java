package com.logistics.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限拦截器：校验 Handler 上的 @RequiresRoles 注解
 */
@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresRoles methodAnnotation = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        if (methodAnnotation == null) {
            // 尝试类级别注解
            methodAnnotation = handlerMethod.getBeanType().getAnnotation(RequiresRoles.class);
        }

        if (methodAnnotation == null) {
            return true; // 没有权限要求，放行
        }

        String[] requiredRoles = methodAnnotation.value();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        List<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 超级管理员拥有所有权限
        if (userRoles.contains("ADMIN")) {
            return true;
        }

        boolean hasRole = Arrays.stream(requiredRoles).anyMatch(userRoles::contains);
        if (!hasRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
