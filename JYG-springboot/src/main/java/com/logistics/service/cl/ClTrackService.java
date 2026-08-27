package com.logistics.service.cl;

import com.logistics.dto.cl.TrackQuery;
import com.logistics.dto.cl.TrackVO;
import com.logistics.dto.cl.VehicleLocationVO;
import com.logistics.entity.ClTrackPoint;

import java.util.List;

/**
 * 车辆运行监管与轨迹监控服务
 */
public interface ClTrackService {

    /**
     * 为派单生成模拟轨迹点（派单状态变为 ONGOING 时自动调用，亦支持手动触发）
     */
    void simulateTrack(Long dispatchId);

    /**
     * 获取车辆最新位置
     */
    VehicleLocationVO getCurrentLocation(Long vehicleId);

    /**
     * 获取所有出车中车辆的最新位置
     */
    List<VehicleLocationVO> getAllCurrentLocations();

    /**
     * 获取车辆轨迹历史（按时间范围）
     */
    TrackVO getTrackHistory(TrackQuery query);

    /**
     * 批量保存轨迹点（供后续对接 GPS 上报使用）
     */
    void batchSaveTrackPoints(List<ClTrackPoint> points);
}
