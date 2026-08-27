package com.logistics.controller.gy;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gy.CheckoutAcceptRequest;
import com.logistics.dto.gy.OccupantApplyRequest;
import com.logistics.dto.gy.OccupantAssignRequest;
import com.logistics.dto.gy.OccupantAuditRequest;
import com.logistics.dto.gy.OccupantPageQuery;
import com.logistics.dto.gy.OccupantVO;
import com.logistics.service.gy.GyOccupantService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 专家公寓与人才公寓管理
 */
@RestController
@RequestMapping("/api/gy/occupant")
@RequiredArgsConstructor
public class GyOccupantController {

    private final GyOccupantService gyOccupantService;

    /**
     * 专家公寓直接分配入住
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/assign/direct")
    @Log(module="GY", operation="直接分配入住", type="ADD")
    public Result<OccupantVO> assignDirectly(@Valid @RequestBody OccupantAssignRequest request) {
        return Result.success(gyOccupantService.assignDirectly(request));
    }

    /**
     * 人才公寓提交入住申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @PostMapping("/apply")
    @Log(module="GY", operation="提交入住申请", type="ADD")
    public Result<OccupantVO> apply(@Valid @RequestBody OccupantApplyRequest request) {
        return Result.success(gyOccupantService.apply(request));
    }

    /**
     * 人才公寓入住审批
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="GY", operation="入住审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody OccupantAuditRequest request) {
        gyOccupantService.audit(request);
        return Result.success();
    }

    /**
     * 分页查询入住记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<OccupantVO>> page(OccupantPageQuery query) {
        return Result.success(gyOccupantService.queryPage(query));
    }

    /**
     * 入住记录详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<OccupantVO> detail(@PathVariable Long id) {
        return Result.success(gyOccupantService.getDetail(id));
    }

    /**
     * 退住
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/checkout/{id}")
    @Log(module="GY", operation="办理退宿", type="UPDATE")
    public Result<Void> checkout(@PathVariable Long id,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime actualLeaveTime,
                                 @RequestParam(required = false) String remark) {
        gyOccupantService.checkout(id, actualLeaveTime, remark);
        return Result.success();
    }

    /**
     * 退住验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/checkout/accept")
    @Log(module="GY", operation="退宿验收", type="APPROVE")
    public Result<Void> acceptCheckout(@Valid @RequestBody CheckoutAcceptRequest request) {
        gyOccupantService.acceptCheckout(request);
        return Result.success();
    }

    /**
     * 租期到期预警
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/expiring")
    public Result<List<OccupantVO>> expiring(@RequestParam(defaultValue = "7") int days) {
        return Result.success(gyOccupantService.getExpiringOccupants(days));
    }
}
