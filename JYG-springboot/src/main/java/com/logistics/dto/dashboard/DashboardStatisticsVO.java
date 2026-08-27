package com.logistics.dto.dashboard;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页统计卡片数据
 */
@Data
public class DashboardStatisticsVO {

    /** 总资产数 */
    private Long totalAssets;

    /** 在仓资产数 */
    private Long inStockAssets;

    /** 已借用资产数 */
    private Long borrowedAssets;

    /** 待审批数 */
    private Long pendingApprovals;

    /** 车辆总数 */
    private Long totalVehicles;

    /** 出车中数 */
    private Long onDutyVehicles;

    /** 总房间数 */
    private Long totalRooms;

    /** 空闲房间数 */
    private Long idleRooms;

    /** 今日预约人数 */
    private Integer todayReservations;

    /** 本月采购金额 */
    private BigDecimal monthlyPurchaseAmount;
}
