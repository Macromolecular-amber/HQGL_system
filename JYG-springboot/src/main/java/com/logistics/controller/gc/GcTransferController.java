package com.logistics.controller.gc;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gc.AssetListVO;
import com.logistics.dto.gc.DisposeApplyRequest;
import com.logistics.dto.gc.DisposeAuditRequest;
import com.logistics.dto.gc.DisposeIncomeRequest;
import com.logistics.dto.gc.DisposeOrderVO;
import com.logistics.dto.gc.DisposePageQuery;
import com.logistics.dto.gc.TransferApplyRequest;
import com.logistics.dto.gc.TransferAuditRequest;
import com.logistics.dto.gc.TransferOrderVO;
import com.logistics.dto.gc.TransferPageQuery;
import com.logistics.service.gc.GcAssetService;
import com.logistics.service.gc.GcTransferService;
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
 * 公物仓调剂共享与处置管理
 */
@RestController
@RequestMapping("/api/gc/transfer")
@RequiredArgsConstructor
public class GcTransferController {

    private final GcTransferService gcTransferService;
    private final GcAssetService gcAssetService;

    /**
     * 提交调剂申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/apply")
    @Log(module="GC", operation="提交调剂申请", type="ADD")
    public Result<TransferOrderVO> apply(@Valid @RequestBody TransferApplyRequest request) {
        return Result.success(gcTransferService.apply(request));
    }

    /**
     * 审批调剂申请
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="GC", operation="调剂审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody TransferAuditRequest request) {
        gcTransferService.audit(request);
        return Result.success();
    }

    /**
     * 分页查询调剂单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<TransferOrderVO>> page(TransferPageQuery query) {
        return Result.success(gcTransferService.queryPage(query));
    }

    /**
     * 调剂单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<TransferOrderVO> detail(@PathVariable Long id) {
        return Result.success(gcTransferService.getDetail(id));
    }

    /**
     * 查询所有在仓资产（供前端选择，可带分类筛选）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/available-assets")
    public Result<List<AssetListVO>> availableAssets(@RequestParam(required = false) String categoryCode) {
        return Result.success(gcAssetService.listAvailableAssets(categoryCode));
    }

    /**
     * 提交处置申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/dispose/apply")
    @Log(module="GC", operation="提交处置申请", type="ADD")
    public Result<DisposeOrderVO> disposeApply(@Valid @RequestBody DisposeApplyRequest request) {
        return Result.success(gcTransferService.applyDispose(request));
    }

    /**
     * 审批处置申请
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/dispose/audit")
    @Log(module="GC", operation="处置审批", type="APPROVE")
    public Result<Void> disposeAudit(@Valid @RequestBody DisposeAuditRequest request) {
        gcTransferService.auditDispose(request);
        return Result.success();
    }

    /**
     * 录入处置收益（处置执行后）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/dispose/income")
    @Log(module="GC", operation="登记处置收入", type="ADD")
    public Result<Void> disposeIncome(@Valid @RequestBody DisposeIncomeRequest request) {
        gcTransferService.recordDisposeIncome(request);
        return Result.success();
    }

    /**
     * 分页查询处置单（仅处置类型）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/dispose/page")
    public Result<PageResult<DisposeOrderVO>> disposePage(DisposePageQuery query) {
        return Result.success(gcTransferService.queryDisposePage(query));
    }

    /**
     * 处置单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/dispose/{id}")
    public Result<DisposeOrderVO> disposeDetail(@PathVariable Long id) {
        return Result.success(gcTransferService.getDisposeDetail(id));
    }
}
