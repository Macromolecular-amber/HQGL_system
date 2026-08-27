package com.logistics.repository;

import com.logistics.entity.GcTransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GcTransferOrderRepository extends JpaRepository<GcTransferOrder, Long>, JpaSpecificationExecutor<GcTransferOrder> {

    /**
     * 统计指定编号前缀的调剂单数量，用于生成调剂单编号序列
     */
    long countByOrderNoStartingWith(String prefix);
}
