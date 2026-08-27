package com.logistics.repository;

import com.logistics.entity.StPurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface StPurchaseDetailRepository extends JpaRepository<StPurchaseDetail, Long> {

    /**
     * 按物资ID查询采购明细（用于删除前校验引用）
     */
    List<StPurchaseDetail> findByMaterialId(Long materialId);

    /**
     * 按采购单ID查询明细
     */
    List<StPurchaseDetail> findByPurchaseOrderId(Long purchaseOrderId);

    /**
     * 按采购单ID集合批量查询明细（用于采购统计）
     */
    List<StPurchaseDetail> findByPurchaseOrderIdIn(Collection<Long> purchaseOrderIds);
}
