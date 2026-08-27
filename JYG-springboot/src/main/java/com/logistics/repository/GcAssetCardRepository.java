package com.logistics.repository;

import com.logistics.entity.GcAssetCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface GcAssetCardRepository extends JpaRepository<GcAssetCard, Long>, JpaSpecificationExecutor<GcAssetCard> {

    /**
     * 统计当天（按创建时间）指定编号前缀的资产数量，用于生成资产编号序列
     */
    long countByAssetCodeStartingWithAndCreateTimeAfter(String prefix, OffsetDateTime createTime);
}
