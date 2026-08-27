package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆实时位置响应
 */
@Data
public class VehicleLocationVO {

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNumber;

    /** 车型 */
    private String vehicleType;

    /** 驾驶员姓名 */
    private String driverName;

    /** 派单ID */
    private Long dispatchId;

    /** 车辆状态 */
    private String status;

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 速度（km/h） */
    private BigDecimal speed;

    /** 方向（度） */
    private Integer direction;

    /** 最后上报时间 */
    private LocalDateTime lastUpdateTime;

    /** 目的地 */
    private String destination;
}
