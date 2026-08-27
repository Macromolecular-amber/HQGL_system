package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 车辆视图：包含 ClVehicleArchive 全部字段，并补充单位名称与类型/状态中文名
 */
@Data
public class VehicleVO {

    /** id */
    private Long id;

    /** 车牌号 */
    private String plateNumber;

    /** 品牌型号 */
    private String brandModel;

    /** 车辆类型 */
    private String vehicleType;

    /** 车辆类型名称 */
    private String vehicleTypeName;

    /** 发动机号 */
    private String engineNo;

    /** 车架号 */
    private String frameNo;

    /** 座位数 */
    private Integer seatCount;

    /** 排量 */
    private BigDecimal displacement;

    /** 颜色 */
    private String color;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /** 购置价格 */
    private BigDecimal purchasePrice;

    /** 供应商 */
    private String supplier;

    /** 所属单位ID */
    private Long unitId;

    /** 部门ID */
    private Long deptId;

    /** 编制ID */
    private Long establishmentId;

    /** 是否在编 */
    private Boolean isEstablishment;

    /** 车辆状态 */
    private String vehicleStatus;

    /** 当前里程 */
    private BigDecimal currentMileage;

    /** 上次保养里程 */
    private BigDecimal lastMaintenanceMileage;

    /** 下次保养里程 */
    private BigDecimal nextMaintenanceMileage;

    /** 保险公司 */
    private String insuranceCompany;

    /** 保险单号 */
    private String insurancePolicyNo;

    /** 保险起期 */
    private LocalDate insuranceStart;

    /** 保险止期 */
    private LocalDate insuranceEnd;

    /** 照片URL */
    private String photoUrls;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 逻辑删除 */
    private Boolean isDeleted;

    /** 所属单位名称（冗余） */
    private String unitName;

    /** 车辆类型中文名 */
    private String vehicleTypeLabel;

    /** 车辆状态中文名 */
    private String vehicleStatusLabel;
}