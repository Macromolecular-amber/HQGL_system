package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 单车台账汇总
 */
@Data
public class VehicleCostSummaryVO {

    /** 车辆ID */
    private Long vehicleId;

    /** 车牌号 */
    private String plateNumber;

    /** 统计期间（如 2026-08） */
    private String period;

    /** 加油费用 */
    private BigDecimal totalFuelCost;

    /** 维修费用 */
    private BigDecimal totalRepairCost;

    /** 保险费用 */
    private BigDecimal totalInsuranceCost;

    /** 过路费 */
    private BigDecimal totalTollCost;

    /** ETC费用 */
    private BigDecimal totalEtcCost;

    /** 停车费用 */
    private BigDecimal totalParkingCost;

    /** 其他费用 */
    private BigDecimal totalOtherCost;

    /** 总费用 */
    private BigDecimal totalCost;

    /** 百公里油耗（L/100km） */
    private BigDecimal avgFuelConsumption;
}
