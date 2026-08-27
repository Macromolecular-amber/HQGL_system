package com.logistics.repository;

import com.logistics.entity.StInventoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StInventoryRecordRepository extends JpaRepository<StInventoryRecord, Long>, JpaSpecificationExecutor<StInventoryRecord> {

    /**
     * 按物资ID查询所有库存流水（按时间倒序）
     */
    List<StInventoryRecord> findByMaterialIdOrderByCreateTimeDesc(Long materialId);
}
