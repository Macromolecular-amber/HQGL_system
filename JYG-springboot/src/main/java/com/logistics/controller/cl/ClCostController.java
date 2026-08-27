package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.cl.CostAuditRequest;
import com.logistics.dto.cl.CostPageQuery;
import com.logistics.dto.cl.CostSaveRequest;
import com.logistics.dto.cl.CostVO;
import com.logistics.dto.cl.VehicleCostSummaryVO;
import com.logistics.service.cl.ClCostService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 费用核算与单车台账管理
 */
@RestController
@RequestMapping("/api/cl/cost")
@RequiredArgsConstructor
public class ClCostController {

    private final ClCostService clCostService;

    /**
     * 费用登记
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/save")
    @Log(module="CL", operation="新增费用记录", type="ADD")
    public Result<CostVO> save(@Valid @RequestBody CostSaveRequest request) {
        return Result.success(clCostService.saveCost(request));
    }

    /**
     * 编辑费用（仅待审批状态）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/update/{id}")
    @Log(module="CL", operation="编辑费用记录", type="UPDATE")
    public Result<CostVO> update(@PathVariable Long id, @Valid @RequestBody CostSaveRequest request) {
        return Result.success(clCostService.updateCost(id, request));
    }

    /**
     * 费用审批
     */
    @RequiresRoles({"BIZ_ADMIN","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="CL", operation="费用审核", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody CostAuditRequest request) {
        clCostService.auditCost(request);
        return Result.success();
    }

    /**
     * 分页查询费用明细
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<CostVO>> page(CostPageQuery query) {
        return Result.success(clCostService.queryPage(query));
    }

    /**
     * 费用详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<CostVO> detail(@PathVariable Long id) {
        return Result.success(clCostService.getDetail(id));
    }

    /**
     * 单车台账汇总（按月）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/summary/vehicle/{vehicleId}")
    public Result<VehicleCostSummaryVO> vehicleSummary(@PathVariable Long vehicleId,
                                                       @RequestParam String yearMonth) {
        return Result.success(clCostService.getVehicleSummary(vehicleId, yearMonth));
    }

    /**
     * 所有车辆台账汇总（按月）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/summary/all")
    public Result<List<VehicleCostSummaryVO>> allSummary(@RequestParam String yearMonth) {
        return Result.success(clCostService.getAllVehicleSummary(yearMonth));
    }
}
