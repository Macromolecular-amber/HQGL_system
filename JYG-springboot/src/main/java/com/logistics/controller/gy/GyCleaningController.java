package com.logistics.controller.gy;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gy.CleaningAcceptRequest;
import com.logistics.dto.gy.CleaningApplyRequest;
import com.logistics.dto.gy.CleaningAssignRequest;
import com.logistics.dto.gy.CleaningAuditRequest;
import com.logistics.dto.gy.CleaningPageQuery;
import com.logistics.dto.gy.CleaningVO;
import com.logistics.service.gy.GyCleaningService;
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
 * 公寓保洁服务管理
 */
@RestController
@RequestMapping("/api/gy/cleaning")
@RequiredArgsConstructor
public class GyCleaningController {

    private final GyCleaningService gyCleaningService;

    /**
     * 提交保洁申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER","CLEANER"})
    @PostMapping("/apply")
    @Log(module="GY", operation="提交保洁申请", type="ADD")
    public Result<CleaningVO> apply(@Valid @RequestBody CleaningApplyRequest request) {
        return Result.success(gyCleaningService.apply(request));
    }

    /**
     * 保洁审批
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @PutMapping("/audit")
    @Log(module="GY", operation="保洁审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody CleaningAuditRequest request) {
        gyCleaningService.audit(request);
        return Result.success();
    }

    /**
     * 保洁派单
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @PutMapping("/assign")
    @Log(module="GY", operation="保洁派工", type="UPDATE")
    public Result<Void> assign(@Valid @RequestBody CleaningAssignRequest request) {
        gyCleaningService.assign(request);
        return Result.success();
    }

    /**
     * 保洁验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER","CLEANER"})
    @PutMapping("/accept")
    @Log(module="GY", operation="保洁验收", type="APPROVE")
    public Result<Void> accept(@Valid @RequestBody CleaningAcceptRequest request) {
        gyCleaningService.accept(request);
        return Result.success();
    }

    /**
     * 分页查询保洁单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER","CLEANER"})
    @GetMapping("/page")
    public Result<PageResult<CleaningVO>> page(CleaningPageQuery query) {
        return Result.success(gyCleaningService.queryPage(query));
    }

    /**
     * 保洁单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER","CLEANER"})
    @GetMapping("/{id}")
    public Result<CleaningVO> detail(@PathVariable Long id) {
        return Result.success(gyCleaningService.getDetail(id));
    }

    /**
     * 获取某房间的所有保洁记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER","CLEANER"})
    @GetMapping("/room/{roomId}")
    public Result<List<CleaningVO>> byRoom(@PathVariable Long roomId) {
        return Result.success(gyCleaningService.getByRoomId(roomId));
    }
}
