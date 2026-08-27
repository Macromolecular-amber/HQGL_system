package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.cl.DispatchPageQuery;
import com.logistics.dto.cl.DispatchRequest;
import com.logistics.dto.cl.DispatchVO;
import com.logistics.dto.cl.ReturnRequest;
import com.logistics.service.cl.ClDispatchService;
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
 * 公务用车车辆调度管理
 */
@RestController
@RequestMapping("/api/cl/dispatch")
@RequiredArgsConstructor
public class ClDispatchController {

    private final ClDispatchService clDispatchService;

    /**
     * 派单
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/dispatch")
    @Log(module="CL", operation="车辆调度", type="UPDATE")
    public Result<DispatchVO> dispatch(@Valid @RequestBody DispatchRequest request) {
        return Result.success(clDispatchService.dispatch(request));
    }

    /**
     * 开始出车：待出车 -> 出车中，自动生成模拟轨迹
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/start/{id}")
    @Log(module="CL", operation="开始出车", type="UPDATE")
    public Result<DispatchVO> start(@PathVariable Long id) {
        return Result.success(clDispatchService.startTrip(id));
    }

    /**
     * 车辆归还
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PutMapping("/return")
    @Log(module="CL", operation="车辆归还", type="UPDATE")
    public Result<Void> returnVehicle(@Valid @RequestBody ReturnRequest request) {
        clDispatchService.returnVehicle(request);
        return Result.success();
    }

    /**
     * 分页查询调度单
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<DispatchVO>> page(DispatchPageQuery query) {
        return Result.success(clDispatchService.queryPage(query));
    }

    /**
     * 调度单详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<DispatchVO> detail(@PathVariable Long id) {
        return Result.success(clDispatchService.getDetail(id));
    }

    /**
     * 根据申请ID查询派单记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/by-apply/{applyId}")
    public Result<List<DispatchVO>> byApply(@PathVariable Long applyId) {
        return Result.success(clDispatchService.getByApplyId(applyId));
    }
}
