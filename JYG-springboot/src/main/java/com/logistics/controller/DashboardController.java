package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.dto.dashboard.DashboardStatisticsVO;
import com.logistics.dto.dashboard.MessageVO;
import com.logistics.dto.dashboard.TodoVO;
import com.logistics.dto.dashboard.TrendVO;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页数据聚合
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SysUserRepository sysUserRepository;

    @GetMapping("/statistics")
    public Result<DashboardStatisticsVO> getStatistics() {
        Long userId = getCurrentUserId();
        return Result.success(dashboardService.getStatistics(userId));
    }

    @GetMapping("/todos")
    public Result<List<TodoVO>> getTodos() {
        Long userId = getCurrentUserId();
        return Result.success(dashboardService.getTodos(userId));
    }

    @GetMapping("/messages")
    public Result<List<MessageVO>> getMessages() {
        Long userId = getCurrentUserId();
        return Result.success(dashboardService.getMessages(userId));
    }

    @GetMapping("/trend")
    public Result<TrendVO> getTrend() {
        return Result.success(dashboardService.getTrend());
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户 ID，未匹配时兜底默认用户
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                SysUser user = sysUserRepository.findByUsername(authentication.getName()).orElse(null);
                if (user != null) {
                    return user.getId();
                }
            }
        }
        return DEFAULT_USER_ID;
    }
}
