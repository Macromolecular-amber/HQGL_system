package com.logistics.repository;

import com.logistics.entity.ClDispatchOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClDispatchOrderRepository extends JpaRepository<ClDispatchOrder, Long>, JpaSpecificationExecutor<ClDispatchOrder> {

    /**
     * 判断车辆是否存在进行中/待执行的调度单
     */
    boolean existsByVehicleIdAndDispatchStatusIn(Long vehicleId, Collection<String> dispatchStatuses);

    /**
     * 统计指定编号前缀的调度单数量，用于生成派单编号序列
     */
    long countByDispatchNoStartingWith(String prefix);

    /**
     * 按申请ID查询派单记录
     */
    List<ClDispatchOrder> findByApplyId(Long applyId);

    /**
     * 按派单状态查询派单记录
     */
    List<ClDispatchOrder> findByDispatchStatus(String dispatchStatus);

    /**
     * 查询指定车辆指定状态的派单记录
     */
    List<ClDispatchOrder> findByVehicleIdAndDispatchStatus(Long vehicleId, String dispatchStatus);
}
