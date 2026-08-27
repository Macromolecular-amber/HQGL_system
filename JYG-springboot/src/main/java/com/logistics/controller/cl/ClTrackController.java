package com.logistics.controller.cl;

import com.logistics.annotation.Log;
import com.logistics.common.Result;
import com.logistics.dto.cl.TrackQuery;
import com.logistics.dto.cl.TrackVO;
import com.logistics.dto.cl.VehicleLocationVO;
import com.logistics.service.cl.ClTrackService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 车辆运行监管与轨迹监控
 */
@RestController
@RequestMapping("/api/cl/track")
@RequiredArgsConstructor
public class ClTrackController {

    private final ClTrackService clTrackService;

    /**
     * 获取车辆最新位置
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/current/{vehicleId}")
    public Result<VehicleLocationVO> current(@PathVariable Long vehicleId) {
        return Result.success(clTrackService.getCurrentLocation(vehicleId));
    }

    /**
     * 获取所有出车中车辆的最新位置
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/current/all")
    public Result<List<VehicleLocationVO>> currentAll() {
        return Result.success(clTrackService.getAllCurrentLocations());
    }

    /**
     * 查询车辆轨迹历史（按时间范围）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PostMapping("/history")
    public Result<TrackVO> history(@Valid @RequestBody TrackQuery query) {
        return Result.success(clTrackService.getTrackHistory(query));
    }

    /**
     * 手动触发模拟轨迹生成（测试用）
     */
    @RequiresRoles({"USER","BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @PostMapping("/simulate/{dispatchId}")
    @Log(module="CL", operation="模拟行驶轨迹", type="UPDATE")
    public Result<Void> simulate(@PathVariable Long dispatchId) {
        clTrackService.simulateTrack(dispatchId);
        return Result.success();
    }
}
