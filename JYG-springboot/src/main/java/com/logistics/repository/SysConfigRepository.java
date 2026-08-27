package com.logistics.repository;

import com.logistics.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {
}
