package com.logistics.controller.gc;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.gc.BorrowApplyRequest;
import com.logistics.dto.gc.BorrowAuditRequest;
import com.logistics.dto.gc.BorrowDetailVO;
import com.logistics.dto.gc.BorrowOrderVO;
import com.logistics.dto.gc.BorrowPageQuery;
import com.logistics.service.gc.GcBorrowService;
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
 * 公物仓资产借用审批管理
 */
@RestController
@RequestMapping("/api/gc/borrow")
@RequiredArgsConstructor
public class GcBorrowController {

    private final GcBorrowService gcBorrowService;

    /**
     * 提交借用申请
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DEPT_MANAGER"})
    @PostMapping("/apply")
    @Log(module="GC", operation="提交借用申请", type="ADD")
    public Result<BorrowOrderVO> apply(@Valid @RequestBody BorrowApplyRequest request) {
        return Result.success(gcBorrowService.apply(request));
    }

    /**
     * 审批借用申请
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @PutMapping("/audit")
    @Log(module="GC", operation="借用审批", type="APPROVE")
    public Result<Void> audit(@Valid @RequestBody BorrowAuditRequest request) {
        gcBorrowService.audit(request);
        return Result.success();
    }

    /**
     * 分页查询借用单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @GetMapping("/page")
    public Result<PageResult<BorrowOrderVO>> page(BorrowPageQuery query) {
        return Result.success(gcBorrowService.queryPage(query));
    }

    /**
     * 借用单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @GetMapping("/{id}")
    public Result<BorrowOrderVO> detail(@PathVariable Long id) {
        return Result.success(gcBorrowService.getDetail(id));
    }

    /**
     * 借用单下的资产列表（用于归还选择）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR","DEPT_MANAGER"})
    @GetMapping("/{id}/assets")
    public Result<List<BorrowDetailVO>> assets(@PathVariable Long id) {
        return Result.success(gcBorrowService.getBorrowAssets(id));
    }
}
