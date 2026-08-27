package com.logistics.repository;

import com.logistics.entity.GcLogisticsOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GcLogisticsOrderRepository extends JpaRepository<GcLogisticsOrder, Long> {
}
