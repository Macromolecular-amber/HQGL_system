package com.logistics.controller.gc;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gc.AssetApplyRequest;
import com.logistics.dto.gc.AssetAuditRequest;
import com.logistics.dto.gc.AssetDetailVO;
import com.logistics.dto.gc.AssetListVO;
import com.logistics.dto.gc.AssetPageQuery;
import com.logistics.dto.gc.AssetQuery;
import com.logistics.dto.gc.AssetVO;
import com.logistics.service.gc.GcAssetService;
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
 * 公物仓资产入仓管理
 */
@RestController
@RequestMapping("/api/gc/asset")
@RequiredArgsConstructor
public class GcAssetController {

    private final GcAssetService gcAssetService;

    /**
     * 提交入仓申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/apply")
    @Log(module="GC", operation="提交入仓申请", type="ADD")
    public Result<AssetVO> apply(@Valid @RequestBody AssetApplyRequest request) {
        return Result.success(gcAssetService.apply(request));
    }

    /**
     * 审核入仓申请
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/audit")
    @Log(module="GC", operation="资产入仓审核", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody AssetAuditRequest request) {
        gcAssetService.audit(request);
        return Result.success();
    }

    /**
     * 分页查询资产
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<AssetVO>> page(AssetPageQuery query) {
        return Result.success(gcAssetService.queryPage(query));
    }

    /**
     * 资产详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<AssetVO> detail(@PathVariable Long id) {
        return Result.success(gcAssetService.getDetail(id));
    }

    /**
     * 分页查询资产列表（多条件筛选）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/list")
    public Result<PageResult<AssetListVO>> list(AssetQuery query) {
        return Result.success(gcAssetService.queryAssetList(query));
    }

    /**
     * 资产详情（完整字段）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/detail/{id}")
    public Result<AssetDetailVO> detailAsset(@PathVariable Long id) {
        return Result.success(gcAssetService.getAssetDetail(id));
    }
}
