package com.logistics.service.cl;

import com.logistics.common.PageResult;
import com.logistics.dto.cl.RepairAcceptRequest;
import com.logistics.dto.cl.RepairApplyRequest;
import com.logistics.dto.cl.RepairAuditRequest;
import com.logistics.dto.cl.RepairPageQuery;
import com.logistics.dto.cl.RepairStartRequest;
import com.logistics.dto.cl.RepairVO;

import java.util.List;

/**
 * 维修保养管理服务
 */
public interface ClRepairService {

    /**
     * 提交维修申请
     */
    RepairVO apply(RepairApplyRequest request);

    /**
     * 维修审批
     */
    void audit(RepairAuditRequest request);

    /**
     * 开始维修
     */
    void startRepair(RepairStartRequest request);

    /**
     * 维修验收（通过后自动生成费用记录）
     */
    void accept(RepairAcceptRequest request);

    /**
     * 分页查询维修单
     */
    PageResult<RepairVO> queryPage(RepairPageQuery query);

    /**
     * 维修单详情
     */
    RepairVO getDetail(Long id);

    /**
     * 获取某车辆的所有维修记录
     */
    List<RepairVO> getByVehicleId(Long vehicleId);
}
