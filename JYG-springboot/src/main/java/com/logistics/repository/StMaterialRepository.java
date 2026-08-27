package com.logistics.repository;

import com.logistics.entity.StMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StMaterialRepository extends JpaRepository<StMaterial, Long>, JpaSpecificationExecutor<StMaterial> {

    /**
     * 按物资编码查询（含逻辑删除过滤，用于唯一性校验）
     */
    List<StMaterial> findByMaterialCodeAndIsDeleted(String materialCode, Boolean isDeleted);
}
