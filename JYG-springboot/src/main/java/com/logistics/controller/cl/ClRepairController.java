package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.cl.RepairAcceptRequest;
import com.logistics.dto.cl.RepairApplyRequest;
import com.logistics.dto.cl.RepairAuditRequest;
import com.logistics.dto.cl.RepairPageQuery;
import com.logistics.dto.cl.RepairStartRequest;
import com.logistics.dto.cl.RepairVO;
import com.logistics.service.cl.ClRepairService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 维修保养管理
 */
@RestController
@RequestMapping("/api/cl/repair")
@RequiredArgsConstructor
public class ClRepairController {

    private final ClRepairService clRepairService;

    /**
     * 提交维修申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DRIVER"})
    @PostMapping("/apply")
    @Log(module="CL", operation="提交维修申请", type="ADD")
    public Result<RepairVO> apply(@Valid @RequestBody RepairApplyRequest request) {
        return Result.success(clRepairService.apply(request));
    }

    /**
     * 维修审批
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/audit")
    @Log(module="CL", operation="维修审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody RepairAuditRequest request) {
        clRepairService.audit(request);
        return Result.success();
    }

    /**
     * 开始维修
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/start")
    @Log(module="CL", operation="开始维修", type="UPDATE")
    public Result<Void> start(@Valid @RequestBody RepairStartRequest request) {
        clRepairService.startRepair(request);
        return Result.success();
    }

    /**
     * 维修验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/accept")
    @Log(module="CL", operation="维修验收", type="APPROVE")
    public Result<Void> accept(@Valid @RequestBody RepairAcceptRequest request) {
        clRepairService.accept(request);
        return Result.success();
    }

    /**
     * 分页查询维修单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DRIVER"})
    @GetMapping("/page")
    public Result<PageResult<RepairVO>> page(RepairPageQuery query) {
        return Result.success(clRepairService.queryPage(query));
    }

    /**
     * 维修单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DRIVER"})
    @GetMapping("/{id}")
    public Result<RepairVO> detail(@PathVariable Long id) {
        return Result.success(clRepairService.getDetail(id));
    }

    /**
     * 获取某车辆的所有维修记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DRIVER"})
    @GetMapping("/vehicle/{vehicleId}")
    public Result<List<RepairVO>> byVehicle(@PathVariable Long vehicleId) {
        return Result.success(clRepairService.getByVehicleId(vehicleId));
    }
}
