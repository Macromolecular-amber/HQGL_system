package com.logistics.repository;

import com.logistics.entity.ClCostDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ClCostDetailRepository extends JpaRepository<ClCostDetail, Long>, JpaSpecificationExecutor<ClCostDetail> {

    /**
     * 查询指定车辆、指定类型、已审批、时间范围内的费用记录（按时间正序，用于油耗计算）
     */
    List<ClCostDetail> findByVehicleIdAndCostTypeAndApprovalStatusAndCostTimeBetweenOrderByCostTimeAsc(
            Long vehicleId, String costType, String approvalStatus, OffsetDateTime start, OffsetDateTime end);

    /**
     * 查询指定车辆、已审批、时间范围内的费用记录（按时间正序）
     */
    List<ClCostDetail> findByVehicleIdAndApprovalStatusAndCostTimeBetweenOrderByCostTimeAsc(
            Long vehicleId, String approvalStatus, OffsetDateTime start, OffsetDateTime end);

    /**
     * 查询所有已审批、时间范围内的费用记录（用于多车辆汇总）
     */
    List<ClCostDetail> findByApprovalStatusAndCostTimeBetween(
            String approvalStatus, OffsetDateTime start, OffsetDateTime end);
}
