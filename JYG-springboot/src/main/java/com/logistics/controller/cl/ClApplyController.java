package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.cl.ApplyAuditRequest;
import com.logistics.dto.cl.ApplyPageQuery;
import com.logistics.dto.cl.ApplyRequest;
import com.logistics.dto.cl.ApplyVO;
import com.logistics.service.cl.ClApplyService;
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

/**
 * 公务用车申请与审批管理
 */
@RestController
@RequestMapping("/api/cl/apply")
@RequiredArgsConstructor
public class ClApplyController {

    private final ClApplyService clApplyService;

    /**
     * 提交用车申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER"})
    @PostMapping("/apply")
    @Log(module="CL", operation="提交用车申请", type="ADD")
    public Result<ApplyVO> apply(@Valid @RequestBody ApplyRequest request) {
        return Result.success(clApplyService.apply(request));
    }

    /**
     * 审批用车申请
     */
    @RequiresRoles({"BIZ_ADMIN","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="CL", operation="用车申请审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody ApplyAuditRequest request) {
        clApplyService.audit(request);
        return Result.success();
    }

    /**
     * 分页查询用车申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @GetMapping("/page")
    public Result<PageResult<ApplyVO>> page(ApplyPageQuery query) {
        return Result.success(clApplyService.queryPage(query));
    }

    /**
     * 用车申请详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @GetMapping("/{id}")
    public Result<ApplyVO> detail(@PathVariable Long id) {
        return Result.success(clApplyService.getDetail(id));
    }

    /**
     * 取消用车申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER"})
    @PutMapping("/cancel/{id}")
    @Log(module="CL", operation="取消用车申请", type="UPDATE")
    public Result<Void> cancel(@PathVariable Long id) {
        clApplyService.cancel(id);
        return Result.success();
    }
}
