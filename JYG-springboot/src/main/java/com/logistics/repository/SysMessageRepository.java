package com.logistics.repository;

import com.logistics.entity.SysMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SysMessageRepository extends JpaRepository<SysMessage, Long>, JpaSpecificationExecutor<SysMessage> {

    /**
     * 统计指定用户的未读消息数
     */
    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    /**
     * 查询指定用户的最新消息（按创建时间倒序）
     */
    List<SysMessage> findByReceiverIdOrderByCreateTimeDesc(Long receiverId, Pageable pageable);

    /**
     * 将指定用户的全部未读消息标记为已读，返回受影响条数
     */
    @Modifying
    @Query("update SysMessage m set m.isRead = true, m.readTime = :readTime " +
            "where m.receiverId = :receiverId and m.isRead = false")
    int markAllAsRead(@Param("receiverId") Long receiverId, @Param("readTime") OffsetDateTime readTime);
}
