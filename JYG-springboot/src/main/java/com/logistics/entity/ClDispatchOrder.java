package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * cl_dispatch_order 实体
 */
@Data
@Entity
@Table(name = "cl_dispatch_order")
public class ClDispatchOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** dispatch_no */
    @Column(name = "dispatch_no", length = 50)
    private String dispatchNo;

    /** apply_id */
    @Column(name = "apply_id")
    private Long applyId;

    /** vehicle_id */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** plate_number */
    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    /** driver_id */
    @Column(name = "driver_id")
    private Long driverId;

    /** driver_name */
    @Column(name = "driver_name", length = 50)
    private String driverName;

    /** driver_phone */
    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    /** scheduled_start */
    @Column(name = "scheduled_start")
    private OffsetDateTime scheduledStart;

    /** scheduled_end */
    @Column(name = "scheduled_end")
    private OffsetDateTime scheduledEnd;

    /** actual_start */
    @Column(name = "actual_start")
    private OffsetDateTime actualStart;

    /** actual_end */
    @Column(name = "actual_end")
    private OffsetDateTime actualEnd;

    /** actual_mileage */
    @Column(name = "actual_mileage")
    private BigDecimal actualMileage;

    /** dispatch_status */
    @Column(name = "dispatch_status", length = 20)
    private String dispatchStatus;

    /** is_emergency */
    @Column(name = "is_emergency")
    private Boolean isEmergency;

    /** emergency_reason */
    @Column(name = "emergency_reason", length = 200)
    private String emergencyReason;

    /** remark */
    @Column(name = "remark", length = 500)
    private String remark;

    /** create_by */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_by */
    @Column(name = "update_by")
    private Long updateBy;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    /** is_deleted */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
