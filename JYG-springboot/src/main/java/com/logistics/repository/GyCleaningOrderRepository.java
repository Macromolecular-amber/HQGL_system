package com.logistics.repository;

import com.logistics.entity.GyCleaningOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GyCleaningOrderRepository extends JpaRepository<GyCleaningOrder, Long>, JpaSpecificationExecutor<GyCleaningOrder> {

    /**
     * 统计指定编号前缀的保洁单数量，用于生成保洁单编号序列
     */
    long countByCleaningNoStartingWith(String prefix);

    /**
     * 查询指定房间的全部保洁记录
     */
    List<GyCleaningOrder> findByRoomIdOrderByCreateTimeDesc(Long roomId);
}
