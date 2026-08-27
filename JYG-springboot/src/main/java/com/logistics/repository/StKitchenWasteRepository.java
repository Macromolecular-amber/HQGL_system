package com.logistics.repository;

import com.logistics.entity.StKitchenWaste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StKitchenWasteRepository extends JpaRepository<StKitchenWaste, Long>, JpaSpecificationExecutor<StKitchenWaste> {
}
