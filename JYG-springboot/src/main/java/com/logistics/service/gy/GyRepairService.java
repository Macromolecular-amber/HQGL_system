package com.logistics.service.gy;

import com.logistics.common.PageResult;
import com.logistics.dto.gy.RepairAcceptRequest;
import com.logistics.dto.gy.RepairApplyRequest;
import com.logistics.dto.gy.RepairAuditRequest;
import com.logistics.dto.gy.RepairPageQuery;
import com.logistics.dto.gy.RepairStartRequest;
import com.logistics.dto.gy.RepairVO;

import java.util.List;

/**
 * 公寓维修管理服务
 */
public interface GyRepairService {

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
     * 维修验收
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
     * 获取某房间的所有维修记录
     */
    List<RepairVO> getByRoomId(Long roomId);
}
