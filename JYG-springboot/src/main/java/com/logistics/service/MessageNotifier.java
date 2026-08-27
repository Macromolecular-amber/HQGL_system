package com.logistics.service;

import com.logistics.dto.message.MessageSendRequest;
import com.logistics.repository.SysRoleRepository;
import com.logistics.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 业务审批流消息通知：提交/审批通过/驳回时向相关角色或申请人发送站内消息
 */
@Component
@RequiredArgsConstructor
public class MessageNotifier {

    /** 消息类型：业务消息 */
    private static final String MESSAGE_TYPE_BUSINESS = "BUSINESS";

    private final MessageService messageService;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    /**
     * 按角色编码通知（角色下无用户时静默跳过）
     *
     * @param title      消息标题
     * @param bizModule  业务模块
     * @param bizOrderNo 业务单号
     * @param roleCodes  接收人角色编码
     */
    public void notifyRoles(String title, String bizModule, String bizOrderNo, String... roleCodes) {
        List<Long> receiverIds = resolveUserIds(Arrays.asList(roleCodes));
        if (receiverIds.isEmpty()) {
            return;
        }
        messageService.send(buildRequest(title, bizModule, bizOrderNo, receiverIds));
    }

    /**
     * 通知指定用户（用户 ID 为空时跳过）
     */
    public void notifyUser(String title, String bizModule, String bizOrderNo, Long userId) {
        if (userId == null) {
            return;
        }
        messageService.send(buildRequest(title, bizModule, bizOrderNo, Collections.singletonList(userId)));
    }

    private MessageSendRequest buildRequest(String title, String bizModule, String bizOrderNo, List<Long> receiverIds) {
        MessageSendRequest request = new MessageSendRequest();
        request.setReceiverIds(receiverIds);
        request.setTitle(title);
        request.setContent(bizOrderNo == null ? null : "单号：" + bizOrderNo);
        request.setMessageType(MESSAGE_TYPE_BUSINESS);
        request.setBizModule(bizModule);
        request.setBizOrderNo(bizOrderNo);
        return request;
    }

    /**
     * 解析角色编码对应的全部用户 ID（去重）
     */
    private List<Long> resolveUserIds(List<String> roleCodes) {
        Set<Long> userIds = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            sysRoleRepository.findByRoleCode(roleCode)
                    .ifPresent(role -> sysUserRoleRepository.findByRoleId(role.getId())
                            .forEach(ur -> userIds.add(ur.getUserId())));
        }
        return new ArrayList<>(userIds);
    }
}
