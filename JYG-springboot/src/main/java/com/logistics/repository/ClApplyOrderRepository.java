package com.logistics.repository;

import com.logistics.entity.ClApplyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClApplyOrderRepository extends JpaRepository<ClApplyOrder, Long>, JpaSpecificationExecutor<ClApplyOrder> {

    /**
     * 统计指定编号前缀的用车申请数量，用于生成申请编号序列
     */
    long countByApplyNoStartingWith(String prefix);
}
