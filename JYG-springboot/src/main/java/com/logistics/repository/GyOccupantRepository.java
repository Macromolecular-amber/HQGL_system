package com.logistics.repository;

import com.logistics.entity.GyOccupant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface GyOccupantRepository extends JpaRepository<GyOccupant, Long>, JpaSpecificationExecutor<GyOccupant> {

    /**
     * 统计指定房间当前入住人数
     */
    long countByRoomIdAndOccupantStatus(Long roomId, String occupantStatus);

    /**
     * 查询在住且预计退租时间在指定区间内的入住记录（租期到期预警）
     */
    List<GyOccupant> findByOccupantStatusAndExpectedLeaveTimeBetween(
            String occupantStatus, OffsetDateTime start, OffsetDateTime end);
}
