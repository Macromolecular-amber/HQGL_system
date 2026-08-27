package com.logistics.controller.message;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.message.MessageQuery;
import com.logistics.dto.message.MessageSendRequest;
import com.logistics.dto.message.MessageVO;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import com.logistics.security.RequiresRoles;
import com.logistics.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 消息中心
 */
@RestController
@RequestMapping("/api/message")
@RequiresRoles({"USER", "BIZ_ADMIN", "WAREHOUSE", "DIRECTOR", "DRIVER", "CLEANER"})
@RequiredArgsConstructor
public class MessageController {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    private final MessageService messageService;
    private final SysUserRepository sysUserRepository;

    /**
     * 发送消息（管理员或系统调用）
     */
    @PostMapping("/send")
    @Log(module="SYS", operation="发送消息", type="ADD")
    @RequiresRoles({"ADMIN"})
    public Result<Void> send(@Valid @RequestBody MessageSendRequest request) {
        messageService.send(request);
        return Result.success();
    }

    /**
     * 标记单条消息已读
     */
    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable("id") Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 当前用户全部消息标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        messageService.markAllAsRead(getCurrentUserId());
        return Result.success();
    }

    /**
     * 分页查询当前用户消息
     */
    @GetMapping("/page")
    public Result<PageResult<MessageVO>> page(MessageQuery query) {
        return Result.success(messageService.queryPage(query, getCurrentUserId()));
    }

    /**
     * 获取当前用户未读数量
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.countUnread(getCurrentUserId()));
    }

    /**
     * 获取当前用户最新消息（默认 5 条，首页用）
     */
    @GetMapping("/latest")
    public Result<List<MessageVO>> latest(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        return Result.success(messageService.getLatest(getCurrentUserId(), limit));
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
