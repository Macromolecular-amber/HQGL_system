package com.logistics.repository;

import com.logistics.entity.ClTrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClTrackPointRepository extends JpaRepository<ClTrackPoint, Long> {

    /**
     * 查询指定车辆的最新轨迹点
     */
    Optional<ClTrackPoint> findFirstByVehicleIdOrderByTrackTimeDesc(Long vehicleId);

    /**
     * 按时间范围查询指定车辆的轨迹点（时间正序）
     */
    List<ClTrackPoint> findByVehicleIdAndTrackTimeBetweenOrderByTrackTimeAsc(
            Long vehicleId, OffsetDateTime start, OffsetDateTime end);

    /**
     * 查询指定派单的全部轨迹点（时间正序）
     */
    List<ClTrackPoint> findByDispatchIdOrderByTrackTimeAsc(Long dispatchId);

    /**
     * 删除指定派单的全部轨迹点（模拟轨迹重新生成前清理）
     */
    void deleteByDispatchId(Long dispatchId);
}
