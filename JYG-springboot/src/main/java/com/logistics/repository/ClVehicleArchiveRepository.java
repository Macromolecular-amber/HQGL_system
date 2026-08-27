package com.logistics.repository;

import com.logistics.entity.ClVehicleArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClVehicleArchiveRepository extends JpaRepository<ClVehicleArchive, Long>, JpaSpecificationExecutor<ClVehicleArchive> {

    /**
     * 按车牌号查询车辆
     */
    Optional<ClVehicleArchive> findByPlateNumber(String plateNumber);
}
