package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆建档/编辑请求
 */
@Data
public class VehicleSaveRequest {

    /** 车辆ID（编辑时必填） */
    private Long id;

    /** 车牌号 */
    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    /** 品牌型号 */
    @NotBlank(message = "品牌型号不能为空")
    private String brandModel;

    /** 车辆类型：SEDAN / SUV / MPV / BUS */
    @NotBlank(message = "车辆类型不能为空")
    private String vehicleType;

    /** 发动机号 */
    private String engineNo;

    /** 车架号 */
    private String frameNo;

    /** 座位数 */
    @NotNull(message = "座位数不能为空")
    @Min(value = 1, message = "座位数必须大于0")
    private Integer seatCount;

    /** 排量 */
    private BigDecimal displacement;

    /** 颜色 */
    private String color;

    /** 购置日期 */
    private LocalDate purchaseDate;

    /** 购置价格 */
    @DecimalMin(value = "0", message = "购置价格不能小于0")
    private BigDecimal purchasePrice;

    /** 所属单位ID */
    @NotNull(message = "所属单位不能为空")
    private Long unitId;

    /** 备注 */
    private String remark;
}
