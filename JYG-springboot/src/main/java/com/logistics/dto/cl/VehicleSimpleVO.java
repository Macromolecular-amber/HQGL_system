package com.logistics.dto.cl;

import lombok.Data;

/**
 * 精简车辆信息（供可调度车辆展示）
 */
@Data
public class VehicleSimpleVO {

    /** 车辆ID */
    private Long id;

    /** 车牌号 */
    private String plateNumber;

    /** 品牌型号 */
    private String brandModel;

    /** 车辆类型 */
    private String vehicleType;

    /** 车辆类型中文名 */
    private String vehicleTypeLabel;
}
