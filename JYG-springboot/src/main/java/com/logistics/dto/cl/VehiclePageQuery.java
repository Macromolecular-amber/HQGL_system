package com.logistics.dto.cl;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 车辆分页查询条件
 */
@Data
public class VehiclePageQuery {

    /** 车牌号（模糊） */
    private String plateNumber;

    /** 车辆类型 */
    private String vehicleType;

    /** 车辆状态 */
    private String vehicleStatus;

    /** 所属单位ID */
    private Long unitId;

    /** 购置日期起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDateStart;

    /** 购置日期止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate purchaseDateEnd;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
