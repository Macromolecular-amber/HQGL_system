package com.logistics.common;

import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 审批自审校验：审批人不能是申请人本人（不相容职务分离）
 */
@Component
@RequiredArgsConstructor
public class AuditGuard {

    private final SysUserRepository sysUserRepository;

    /**
     * 校验当前登录用户不能审批自己提交的单据
     *
     * @param applicantId 申请人ID（申请人字段为 null 时跳过校验）
     */
    public void assertNotSelfAudit(Long applicantId) {
        if (applicantId == null) {
            return;
        }
        Long currentUserId = resolveCurrentUserId();
        if (currentUserId != null && currentUserId.equals(applicantId)) {
            throw new BusinessException("不能审批自己提交的申请");
        }
    }

    /**
     * 从 SecurityContext 解析当前登录用户 ID，未登录/未匹配时返回 null
     */
    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                return sysUserRepository.findByUsername(authentication.getName())
                        .map(SysUser::getId)
                        .orElse(null);
            }
        }
        return null;
    }
}
