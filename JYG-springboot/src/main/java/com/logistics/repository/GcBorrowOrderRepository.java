package com.logistics.repository;

import com.logistics.entity.GcBorrowOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GcBorrowOrderRepository extends JpaRepository<GcBorrowOrder, Long>, JpaSpecificationExecutor<GcBorrowOrder> {

    /**
     * 统计指定编号前缀的借用单数量，用于生成借用单编号序列
     */
    long countByOrderNoStartingWith(String prefix);
}
