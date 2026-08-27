package com.logistics.controller.gc;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gc.ReturnAcceptRequest;
import com.logistics.dto.gc.ReturnApplyRequest;
import com.logistics.dto.gc.ReturnOrderVO;
import com.logistics.dto.gc.ReturnPageQuery;
import com.logistics.service.gc.GcReturnService;
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
 * 公物仓资产归还验收管理
 */
@RestController
@RequestMapping("/api/gc/return")
@RequiredArgsConstructor
public class GcReturnController {

    private final GcReturnService gcReturnService;

    /**
     * 提交归还申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/apply")
    @Log(module="GC", operation="提交归还申请", type="ADD")
    public Result<ReturnOrderVO> apply(@Valid @RequestBody ReturnApplyRequest request) {
        return Result.success(gcReturnService.apply(request));
    }

    /**
     * 归还验收
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PutMapping("/accept")
    @Log(module="GC", operation="归还验收", type="APPROVE")
    public Result<Void> accept(@Valid @RequestBody ReturnAcceptRequest request) {
        gcReturnService.accept(request);
        return Result.success();
    }

    /**
     * 分页查询归还单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<ReturnOrderVO>> page(ReturnPageQuery query) {
        return Result.success(gcReturnService.queryPage(query));
    }

    /**
     * 归还单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<ReturnOrderVO> detail(@PathVariable Long id) {
        return Result.success(gcReturnService.getDetail(id));
    }
}
