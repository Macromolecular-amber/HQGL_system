package com.logistics.controller.st;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.st.PurchaseAcceptRequest;
import com.logistics.dto.st.PurchaseApplyRequest;
import com.logistics.dto.st.PurchaseAuditRequest;
import com.logistics.dto.st.PurchaseOrderVO;
import com.logistics.dto.st.PurchasePageQuery;
import com.logistics.service.st.StPurchaseService;
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
 * 食堂采购供应链管理
 */
@RestController
@RequestMapping("/api/st/purchase")
@RequiredArgsConstructor
public class StPurchaseController {

    private final StPurchaseService stPurchaseService;

    /**
     * 提交采购申请
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/apply")
    @Log(module="ST", operation="提交采购申请", type="ADD")
    public Result<PurchaseOrderVO> apply(@Valid @RequestBody PurchaseApplyRequest request) {
        return Result.success(stPurchaseService.apply(request));
    }

    /**
     * 采购单审批
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/audit")
    @Log(module="ST", operation="采购审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody PurchaseAuditRequest request) {
        stPurchaseService.audit(request);
        return Result.success();
    }

    /**
     * 采购单验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/accept")
    @Log(module="ST", operation="采购收货", type="APPROVE")
    public Result<Void> accept(@Valid @RequestBody PurchaseAcceptRequest request) {
        stPurchaseService.accept(request);
        return Result.success();
    }

    /**
     * 分页查询采购单
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/page")
    public Result<PageResult<PurchaseOrderVO>> page(PurchasePageQuery query) {
        return Result.success(stPurchaseService.queryPage(query));
    }

    /**
     * 采购单详情
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> detail(@PathVariable Long id) {
        return Result.success(stPurchaseService.getDetail(id));
    }
}
