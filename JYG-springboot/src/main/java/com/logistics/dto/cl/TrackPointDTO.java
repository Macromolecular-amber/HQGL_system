package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 轨迹点数据传输对象
 */
@Data
public class TrackPointDTO {

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 速度（km/h） */
    private BigDecimal speed;

    /** 方向（度） */
    private Integer direction;

    /** 轨迹时间 */
    private LocalDateTime trackTime;
}
