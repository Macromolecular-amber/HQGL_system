package com.logistics.dto.dashboard;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 领导驾驶舱聚合数据
 */
@Data
public class LeadershipDashboardVO {

    /** 核心 KPI 指标（totalAssets / inStockAssets / monthCarUses / occupancyRate / monthMealReserves / pendingApprovals） */
    private Map<String, Kpi> kpis;

    /** 近 7 天各模块趋势 */
    private TrendData trends;

    /** 本月业务分布（gc / cl / gy / st） */
    private Map<String, Number> distribution;

    /** 本月核心数据明细（gc / cl / gy / st） */
    private Map<String, ModuleDetail> details;

    @Data
    public static class Kpi {
        /** 指标数值 */
        private double value;
        /** 环比变化（%） */
        private double change;
        /** 趋势：up / down / flat */
        private String trend;

        public Kpi() {
        }

        public Kpi(double value, double change, String trend) {
            this.value = value;
            this.change = change;
            this.trend = trend;
        }
    }

    @Data
    public static class TrendData {
        /** 日期序列（M/d） */
        private List<String> dates;
        /** 公物仓入仓 */
        private List<Number> gc;
        /** 用车次数 */
        private List<Number> cl;
        /** 食堂预约 */
        private List<Number> st;
    }

    @Data
    public static class ModuleDetail {
        /** 本月新增 */
        private double monthNew;
        /** 本月完成 */
        private double monthDone;
        /** 环比上月（%） */
        private double lastMonth;
        /** 同比增长（%） */
        private double yoy;
        /** 状态：normal / abnormal */
        private String status;
    }
}
