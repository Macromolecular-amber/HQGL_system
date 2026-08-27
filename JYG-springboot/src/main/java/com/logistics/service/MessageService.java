package com.logistics.service;

import com.logistics.common.PageResult;
import com.logistics.dto.message.MessageQuery;
import com.logistics.dto.message.MessageSendRequest;
import com.logistics.dto.message.MessageVO;

import java.util.List;

/**
 * 消息中心服务
 */
public interface MessageService {

    /**
     * 发送消息（支持批量接收人）
     */
    void send(MessageSendRequest request);

    /**
     * 标记单条消息已读
     */
    void markAsRead(Long messageId);

    /**
     * 指定用户的全部消息标记已读
     */
    void markAllAsRead(Long receiverId);

    /**
     * 分页查询当前用户消息
     */
    PageResult<MessageVO> queryPage(MessageQuery query, Long userId);

    /**
     * 统计未读数量
     */
    Long countUnread(Long userId);

    /**
     * 获取最新N条消息
     */
    List<MessageVO> getLatest(Long userId, int limit);
}
