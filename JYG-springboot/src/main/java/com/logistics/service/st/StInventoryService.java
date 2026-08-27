package com.logistics.service.st;

import com.logistics.common.PageResult;
import com.logistics.dto.st.InventoryPageQuery;
import com.logistics.dto.st.InventoryRecordVO;
import com.logistics.dto.st.MaterialVO;
import com.logistics.dto.st.StockAdjustRequest;
import com.logistics.dto.st.StockOutRequest;

import java.util.List;

/**
 * 食堂进销存管理服务
 */
public interface StInventoryService {

    /**
     * 采购验收自动入库（由采购验收流程触发）
     */
    void stockIn(Long purchaseOrderId);

    /**
     * 领用出库
     */
    void stockOut(StockOutRequest request);

    /**
     * 库存调整（盘盈/盘亏）
     */
    void adjust(StockAdjustRequest request);

    /**
     * 分页查询库存流水
     */
    PageResult<InventoryRecordVO> queryPage(InventoryPageQuery query);

    /**
     * 查询某物资的所有流水
     */
    List<InventoryRecordVO> getByMaterial(Long materialId);

    /**
     * 定时任务：库存预警检查（打印日志）
     */
    void checkStockAlert();

    /**
     * 获取库存预警列表（短缺/积压）
     */
    List<MaterialVO> getStockAlerts();
}
