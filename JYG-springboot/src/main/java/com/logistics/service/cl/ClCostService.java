package com.logistics.service.cl;

import com.logistics.common.PageResult;
import com.logistics.dto.cl.CostAuditRequest;
import com.logistics.dto.cl.CostPageQuery;
import com.logistics.dto.cl.CostSaveRequest;
import com.logistics.dto.cl.CostVO;
import com.logistics.dto.cl.VehicleCostSummaryVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用核算与单车台账服务
 */
public interface ClCostService {

    /**
     * 费用登记（初始状态 PENDING）
     */
    CostVO saveCost(CostSaveRequest request);

    /**
     * 编辑费用（仅 PENDING 状态可编辑）
     */
    CostVO updateCost(Long id, CostSaveRequest request);

    /**
     * 费用审批
     */
    void auditCost(CostAuditRequest request);

    /**
     * 分页查询费用明细
     */
    PageResult<CostVO> queryPage(CostPageQuery query);

    /**
     * 费用详情
     */
    CostVO getDetail(Long id);

    /**
     * 单车台账汇总（按月，格式 yyyy-MM）
     */
    VehicleCostSummaryVO getVehicleSummary(Long vehicleId, String yearMonth);

    /**
     * 所有车辆台账汇总（按月）
     */
    List<VehicleCostSummaryVO> getAllVehicleSummary(String yearMonth);

    /**
     * 计算车辆在时间段内的百公里油耗（L/100km），无有效数据返回 null
     */
    BigDecimal calculateFuelConsumption(Long vehicleId, LocalDateTime start, LocalDateTime end);
}
