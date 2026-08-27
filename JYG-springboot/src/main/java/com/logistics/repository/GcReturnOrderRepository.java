package com.logistics.repository;

import com.logistics.entity.GcReturnOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GcReturnOrderRepository extends JpaRepository<GcReturnOrder, Long>, JpaSpecificationExecutor<GcReturnOrder> {

    /**
     * 统计指定编号前缀的归还单数量，用于生成归还单编号序列
     */
    long countByReturnNoStartingWith(String prefix);
}
