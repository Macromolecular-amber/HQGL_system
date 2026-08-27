package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * cl_track_point 车辆轨迹点实体
 */
@Data
@Entity
@Table(name = "cl_track_point")
public class ClTrackPoint {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 派单ID */
    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    /** 车辆ID */
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /** 经度 */
    @Column(name = "lng", precision = 10, scale = 7, nullable = false)
    private BigDecimal lng;

    /** 纬度 */
    @Column(name = "lat", precision = 10, scale = 7, nullable = false)
    private BigDecimal lat;

    /** 速度（km/h） */
    @Column(name = "speed", precision = 8, scale = 2)
    private BigDecimal speed;

    /** 方向（度） */
    @Column(name = "direction")
    private Integer direction;

    /** 轨迹时间 */
    @Column(name = "track_time", nullable = false)
    private OffsetDateTime trackTime;

    /** 创建时间（数据库默认 CURRENT_TIMESTAMP） */
    @Column(name = "create_time", insertable = false, updatable = false)
    private OffsetDateTime createTime;

}
