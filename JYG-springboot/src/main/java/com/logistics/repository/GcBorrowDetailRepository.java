package com.logistics.repository;

import com.logistics.entity.GcBorrowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GcBorrowDetailRepository extends JpaRepository<GcBorrowDetail, Long> {

    /**
     * 查询指定借用单的明细列表
     */
    List<GcBorrowDetail> findByBorrowOrderId(Long borrowOrderId);
}
