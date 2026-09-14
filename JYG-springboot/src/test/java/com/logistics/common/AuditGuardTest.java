package com.logistics.common;

import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuditGuard 自审校验单元测试
 */
class AuditGuardTest {

    private SysUserRepository sysUserRepository;
    private AuditGuard auditGuard;

    @BeforeEach
    void setUp() {
        sysUserRepository = mock(SysUserRepository.class);
        auditGuard = new AuditGuard(sysUserRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * 模拟已登录用户：向 SecurityContext 写入 username 认证，并按需 stub 用户查询
     */
    private void loginAs(String username, Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList()));
        when(sysUserRepository.findByUsername(username))
                .thenReturn(userId == null ? Optional.empty() : Optional.of(buildUser(userId)));
    }

    private SysUser buildUser(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        return user;
    }

    @Test
    void 未登录时不做拦截() {
        assertDoesNotThrow(() -> auditGuard.assertNotSelfAudit(1L));
    }

    @Test
    void 申请人为空时跳过校验() {
        loginAs("zhangsan", 100L);
        assertDoesNotThrow(() -> auditGuard.assertNotSelfAudit(null));
    }

    @Test
    void 审批人与申请人相同时抛出业务异常() {
        loginAs("zhangsan", 100L);
        assertThrows(BusinessException.class, () -> auditGuard.assertNotSelfAudit(100L));
    }

    @Test
    void 审批人与申请人不同时不抛异常() {
        loginAs("zhangsan", 100L);
        assertDoesNotThrow(() -> auditGuard.assertNotSelfAudit(200L));
    }

    @Test
    void 匿名用户不做拦截() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList()));
        assertDoesNotThrow(() -> auditGuard.assertNotSelfAudit(1L));
    }

    @Test
    void 用户名未匹配到系统用户时不拦截() {
        loginAs("ghost", null); // findByUsername 返回 empty
        assertDoesNotThrow(() -> auditGuard.assertNotSelfAudit(1L));
    }
}
