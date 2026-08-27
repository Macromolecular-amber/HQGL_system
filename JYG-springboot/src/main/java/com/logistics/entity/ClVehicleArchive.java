package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * cl_vehicle_archive 实体
 */
@Data
@Entity
@Table(name = "cl_vehicle_archive")
public class ClVehicleArchive {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** plate_number */
    @Column(name = "plate_number", length = 20)
    private String plateNumber;

    /** brand_model */
    @Column(name = "brand_model", length = 100)
    private String brandModel;

    /** vehicle_type */
    @Column(name = "vehicle_type", length = 20)
    private String vehicleType;

    /** vehicle_type_name */
    @Column(name = "vehicle_type_name", length = 20)
    private String vehicleTypeName;

    /** engine_no */
    @Column(name = "engine_no", length = 50)
    private String engineNo;

    /** frame_no */
    @Column(name = "frame_no", length = 50)
    private String frameNo;

    /** seat_count */
    @Column(name = "seat_count")
    private Integer seatCount;

    /** displacement */
    @Column(name = "displacement")
    private BigDecimal displacement;

    /** color */
    @Column(name = "color", length = 20)
    private String color;

    /** purchase_date */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /** purchase_price */
    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    /** supplier */
    @Column(name = "supplier", length = 100)
    private String supplier;

    /** unit_id */
    @Column(name = "unit_id")
    private Long unitId;

    /** unit_name */
    @Column(name = "unit_name", length = 100)
    private String unitName;

    /** dept_id */
    @Column(name = "dept_id")
    private Long deptId;

    /** establishment_id */
    @Column(name = "establishment_id")
    private Long establishmentId;

    /** is_establishment */
    @Column(name = "is_establishment")
    private Boolean isEstablishment;

    /** vehicle_status */
    @Column(name = "vehicle_status", length = 20)
    private String vehicleStatus;

    /** current_mileage */
    @Column(name = "current_mileage")
    private BigDecimal currentMileage;

    /** last_maintenance_mileage */
    @Column(name = "last_maintenance_mileage")
    private BigDecimal lastMaintenanceMileage;

    /** next_maintenance_mileage */
    @Column(name = "next_maintenance_mileage")
    private BigDecimal nextMaintenanceMileage;

    /** insurance_company */
    @Column(name = "insurance_company", length = 100)
    private String insuranceCompany;

    /** insurance_policy_no */
    @Column(name = "insurance_policy_no", length = 50)
    private String insurancePolicyNo;

    /** insurance_start */
    @Column(name = "insurance_start")
    private LocalDate insuranceStart;

    /** insurance_end */
    @Column(name = "insurance_end")
    private LocalDate insuranceEnd;

    /** photo_urls */
    @Column(name = "photo_urls", columnDefinition = "text")
    private String photoUrls;

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
