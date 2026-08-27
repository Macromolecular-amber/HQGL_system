package com.logistics.dto.cl;

import lombok.Data;

import java.util.List;

/**
 * 轨迹响应
 */
@Data
public class TrackVO {

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNumber;

    /** 轨迹点列表 */
    private List<TrackPointDTO> points;
}
