package com.logistics.controller.gy;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gy.RepairAcceptRequest;
import com.logistics.dto.gy.RepairApplyRequest;
import com.logistics.dto.gy.RepairAuditRequest;
import com.logistics.dto.gy.RepairPageQuery;
import com.logistics.dto.gy.RepairStartRequest;
import com.logistics.dto.gy.RepairVO;
import com.logistics.service.gy.GyRepairService;
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
 * 公寓维修管理
 */
@RestController
@RequestMapping("/api/gy/repair")
@RequiredArgsConstructor
public class GyRepairController {

    private final GyRepairService gyRepairService;

    /**
     * 提交维修申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @PostMapping("/apply")
    @Log(module="GY", operation="提交维修申请", type="ADD")
    public Result<RepairVO> apply(@Valid @RequestBody RepairApplyRequest request) {
        return Result.success(gyRepairService.apply(request));
    }

    /**
     * 维修审批
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="GY", operation="维修审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody RepairAuditRequest request) {
        gyRepairService.audit(request);
        return Result.success();
    }

    /**
     * 开始维修
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/start")
    @Log(module="GY", operation="开始维修", type="UPDATE")
    public Result<Void> start(@Valid @RequestBody RepairStartRequest request) {
        gyRepairService.startRepair(request);
        return Result.success();
    }

    /**
     * 维修验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/accept")
    @Log(module="GY", operation="维修验收", type="APPROVE")
    public Result<Void> accept(@Valid @RequestBody RepairAcceptRequest request) {
        gyRepairService.accept(request);
        return Result.success();
    }

    /**
     * 分页查询维修单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<RepairVO>> page(RepairPageQuery query) {
        return Result.success(gyRepairService.queryPage(query));
    }

    /**
     * 维修单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<RepairVO> detail(@PathVariable Long id) {
        return Result.success(gyRepairService.getDetail(id));
    }

    /**
     * 获取某房间的所有维修记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/room/{roomId}")
    public Result<List<RepairVO>> byRoom(@PathVariable Long roomId) {
        return Result.success(gyRepairService.getByRoomId(roomId));
    }
}
