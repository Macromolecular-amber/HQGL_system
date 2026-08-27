package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.message.MessageQuery;
import com.logistics.dto.message.MessageSendRequest;
import com.logistics.dto.message.MessageVO;
import com.logistics.entity.SysMessage;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysMessageRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息中心服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    /** 消息类型中文映射 */
    private static final Map<String, String> MESSAGE_TYPE_LABELS = new HashMap<>();

    /** 业务模块 -> 前端真实路由（消息详情"查看详情"跳转用） */
    private static final Map<String, String> MODULE_URL_MAP = new HashMap<>();

    /** 系统发送标识 */
    private static final Long SYSTEM_SENDER_ID = 0L;

    static {
        MESSAGE_TYPE_LABELS.put("SYSTEM", "系统消息");
        MESSAGE_TYPE_LABELS.put("BUSINESS", "业务消息");
        MESSAGE_TYPE_LABELS.put("WARNING", "预警消息");
        MESSAGE_TYPE_LABELS.put("APPROVAL", "审批通知");
        MODULE_URL_MAP.put("gc-asset", "/gc/asset-list");
        MODULE_URL_MAP.put("gc-borrow", "/gc/borrow-apply");
        MODULE_URL_MAP.put("cl-apply", "/cl/apply");
        MODULE_URL_MAP.put("gy-occupant", "/gy/occupant");
        MODULE_URL_MAP.put("st-purchase", "/st/purchase");
        MODULE_URL_MAP.put("st-inventory", "/st/inventory");
        MODULE_URL_MAP.put("st-meal", "/st/meal-reserve");
        MODULE_URL_MAP.put("pay", "/pay/card");
    }

    private final SysMessageRepository messageRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public void send(MessageSendRequest request) {
        if (request.getReceiverIds() == null || request.getReceiverIds().isEmpty()) {
            throw new BusinessException("接收人不能为空");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException("消息标题不能为空");
        }
        String messageType = request.getMessageType() == null ? null
                : request.getMessageType().toUpperCase(Locale.ROOT);
        if (!MESSAGE_TYPE_LABELS.containsKey(messageType)) {
            throw new BusinessException("消息类型无效");
        }
        Long currentUserId = resolveCurrentUserId();
        OffsetDateTime now = OffsetDateTime.now();
        for (Long receiverId : request.getReceiverIds()) {
            SysMessage message = new SysMessage();
            message.setReceiverId(receiverId);
            message.setSenderId(currentUserId); // 0 表示系统发送
            message.setTitle(request.getTitle().trim());
            message.setContent(request.getContent());
            message.setMessageType(messageType);
            message.setBizModule(request.getBizModule());
            message.setBizOrderNo(request.getBizOrderNo());
            message.setIsRead(false);
            message.setCreateBy(currentUserId); // 记录创建人（未解析到用户时为 0）
            message.setCreateTime(now);
            messageRepository.save(message);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        SysMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));
        Long currentUserId = resolveCurrentUserId();
        if (!message.getReceiverId().equals(currentUserId)) {
            throw new BusinessException("无权操作该消息");
        }
        if (!Boolean.TRUE.equals(message.getIsRead())) {
            message.setIsRead(true);
            message.setReadTime(OffsetDateTime.now());
            messageRepository.save(message);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long receiverId) {
        messageRepository.markAllAsRead(receiverId, OffsetDateTime.now());
    }

    @Override
    public PageResult<MessageVO> queryPage(MessageQuery query, Long userId) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<SysMessage> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("receiverId"), userId));
            if (StringUtils.hasText(query.getMessageType())) {
                predicates.add(cb.equal(cb.upper(root.get("messageType")),
                        query.getMessageType().toUpperCase(Locale.ROOT)));
            }
            if (query.getIsRead() != null) {
                predicates.add(cb.equal(root.get("isRead"), query.getIsRead()));
            }
            if (query.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndTime() != null) {
                predicates.add(cb.lessThan(root.get("createTime"),
                        query.getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SysMessage> result = messageRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MessageVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public Long countUnread(Long userId) {
        return messageRepository.countByReceiverIdAndIsRead(userId, false);
    }

    @Override
    public List<MessageVO> getLatest(Long userId, int limit) {
        int safeLimit = limit < 1 ? 5 : limit;
        List<SysMessage> messages = messageRepository.findByReceiverIdOrderByCreateTimeDesc(
                userId, PageRequest.of(0, safeLimit));
        return messages.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 消息实体转 VO：补充发送人姓名、类型中文名、关联业务链接
     */
    private MessageVO toVO(SysMessage message) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setMessageTypeLabel(MESSAGE_TYPE_LABELS.getOrDefault(message.getMessageType(), message.getMessageType()));
        vo.setSenderName(resolveSenderName(message.getSenderId()));
        // 关联业务链接：映射到前端真实存在的模块路由，避免跳转到不存在的路径
        if (StringUtils.hasText(message.getBizModule())) {
            vo.setRelativeUrl(MODULE_URL_MAP.get(message.getBizModule()));
        }
        return vo;
    }

    /**
     * 解析发送人姓名，系统发送或用户不存在时显示"系统"
     */
    private String resolveSenderName(Long senderId) {
        if (senderId == null || senderId.equals(SYSTEM_SENDER_ID)) {
            return "系统";
        }
        return sysUserRepository.findById(senderId).map(SysUser::getRealName).orElse("系统");
    }

    /**
     * 从 SecurityContext 解析当前用户 ID，未匹配到用户时返回 0（视为系统）
     */
    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                return sysUserRepository.findByUsername(authentication.getName())
                        .map(SysUser::getId)
                        .orElse(SYSTEM_SENDER_ID);
            }
        }
        return SYSTEM_SENDER_ID;
    }
}
