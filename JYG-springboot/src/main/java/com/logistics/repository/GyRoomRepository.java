package com.logistics.repository;

import com.logistics.entity.GyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GyRoomRepository extends JpaRepository<GyRoom, Long>, JpaSpecificationExecutor<GyRoom> {

    /**
     * 按楼栋+楼层+房间号查询未删除的房间（用于唯一性校验）
     */
    List<GyRoom> findByBuildingAndFloorAndRoomNoAndIsDeleted(String building, Integer floor, String roomNo, Boolean isDeleted);
}
