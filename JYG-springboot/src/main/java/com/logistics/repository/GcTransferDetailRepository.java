package com.logistics.repository;

import com.logistics.entity.GcTransferDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GcTransferDetailRepository extends JpaRepository<GcTransferDetail, Long> {

    /**
     * 查询指定调剂单的资产明细列表
     */
    List<GcTransferDetail> findByTransferOrderId(Long transferOrderId);
}
