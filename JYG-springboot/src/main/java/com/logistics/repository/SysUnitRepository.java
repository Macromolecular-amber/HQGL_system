package com.logistics.repository;

import com.logistics.entity.SysUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUnitRepository extends JpaRepository<SysUnit, Long> {
}
