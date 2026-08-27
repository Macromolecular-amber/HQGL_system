package com.logistics.repository;

import com.logistics.entity.StPurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StPurchaseOrderRepository extends JpaRepository<StPurchaseOrder, Long>, JpaSpecificationExecutor<StPurchaseOrder> {

    /**
     * 按ID集合批量查询采购单（用于删除前校验引用）
     */
    List<StPurchaseOrder> findByIdIn(Collection<Long> ids);

    /**
     * 查询指定编号前缀下编号最大的一条（用于生成当月序号）
     */
    Optional<StPurchaseOrder> findTopByOrderNoStartingWithOrderByOrderNoDesc(String prefix);
}
