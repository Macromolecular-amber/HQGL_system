package com.logistics.repository;

import com.logistics.entity.ClRepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClRepairOrderRepository extends JpaRepository<ClRepairOrder, Long>, JpaSpecificationExecutor<ClRepairOrder> {

    /**
     * 统计指定编号前缀的维修单数量，用于生成维修单编号序列
     */
    long countByRepairNoStartingWith(String prefix);

    /**
     * 查询指定车辆的全部维修记录
     */
    List<ClRepairOrder> findByVehicleIdOrderByCreateTimeDesc(Long vehicleId);
}
