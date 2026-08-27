package com.logistics.controller.st;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.st.InventoryPageQuery;
import com.logistics.dto.st.InventoryRecordVO;
import com.logistics.dto.st.MaterialVO;
import com.logistics.dto.st.StockAdjustRequest;
import com.logistics.dto.st.StockOutRequest;
import com.logistics.service.st.StInventoryService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 食堂进销存管理
 */
@RestController
@RequestMapping("/api/st/inventory")
@RequiredArgsConstructor
public class StInventoryController {

    private final StInventoryService stInventoryService;

    /**
     * 领用出库
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/stock-out")
    @Log(module="ST", operation="物资出库", type="UPDATE")
    public Result<Void> stockOut(@Valid @RequestBody StockOutRequest request) {
        stInventoryService.stockOut(request);
        return Result.success();
    }

    /**
     * 库存调整（盘盈/盘亏）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/adjust")
    @Log(module="ST", operation="库存调整", type="UPDATE")
    public Result<Void> adjust(@Valid @RequestBody StockAdjustRequest request) {
        stInventoryService.adjust(request);
        return Result.success();
    }

    /**
     * 分页查询库存流水
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/page")
    public Result<PageResult<InventoryRecordVO>> page(InventoryPageQuery query) {
        return Result.success(stInventoryService.queryPage(query));
    }

    /**
     * 查询某物资的所有流水
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/material/{materialId}")
    public Result<List<InventoryRecordVO>> byMaterial(@PathVariable Long materialId) {
        return Result.success(stInventoryService.getByMaterial(materialId));
    }

    /**
     * 获取库存预警列表（短缺/积压）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/alerts")
    public Result<List<MaterialVO>> alerts() {
        return Result.success(stInventoryService.getStockAlerts());
    }
}
