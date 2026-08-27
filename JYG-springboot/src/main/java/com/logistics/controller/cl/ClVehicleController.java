package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.cl.VehiclePageQuery;
import com.logistics.dto.cl.VehicleSaveRequest;
import com.logistics.dto.cl.VehicleVO;
import com.logistics.service.cl.ClVehicleService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 公务用车车辆档案管理
 */
@RestController
@RequestMapping("/api/cl/vehicle")
@RequiredArgsConstructor
public class ClVehicleController {

    private final ClVehicleService clVehicleService;

    /**
     * 新增或编辑车辆
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/save")
    @Log(module="CL", operation="车辆建档", type="ADD")
    public Result<VehicleVO> save(@Valid @RequestBody VehicleSaveRequest request) {
        return Result.success(clVehicleService.save(request));
    }

    /**
     * 逻辑删除车辆
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @DeleteMapping("/{id}")
    @Log(module="CL", operation="删除车辆", type="DELETE")
    public Result<Void> delete(@PathVariable Long id) {
        clVehicleService.delete(id);
        return Result.success();
    }

    /**
     * 分页查询车辆
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/page")
    public Result<PageResult<VehicleVO>> page(VehiclePageQuery query) {
        return Result.success(clVehicleService.queryPage(query));
    }

    /**
     * 车辆详情
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/{id}")
    public Result<VehicleVO> detail(@PathVariable Long id) {
        return Result.success(clVehicleService.getDetail(id));
    }

    /**
     * 获取所有可用车辆（供调度选择）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/available")
    public Result<List<VehicleVO>> available() {
        return Result.success(clVehicleService.getAvailableVehicles());
    }
}
