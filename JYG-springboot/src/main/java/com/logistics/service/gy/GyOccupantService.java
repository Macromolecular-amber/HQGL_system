package com.logistics.service.gy;

import com.logistics.common.PageResult;
import com.logistics.dto.gy.CheckoutAcceptRequest;
import com.logistics.dto.gy.OccupantApplyRequest;
import com.logistics.dto.gy.OccupantAssignRequest;
import com.logistics.dto.gy.OccupantAuditRequest;
import com.logistics.dto.gy.OccupantPageQuery;
import com.logistics.dto.gy.OccupantVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 专家公寓与人才公寓管理服务
 */
public interface GyOccupantService {

    /**
     * 专家公寓直接分配入住
     */
    OccupantVO assignDirectly(OccupantAssignRequest request);

    /**
     * 人才公寓提交入住申请（初始状态 PENDING）
     */
    OccupantVO apply(OccupantApplyRequest request);

    /**
     * 人才公寓入住审批
     */
    void audit(OccupantAuditRequest request);

    /**
     * 分页查询入住记录
     */
    PageResult<OccupantVO> queryPage(OccupantPageQuery query);

    /**
     * 入住记录详情
     */
    OccupantVO getDetail(Long id);

    /**
     * 退住
     */
    void checkout(Long id, LocalDateTime actualLeaveTime, String remark);

    /**
     * 退住验收
     */
    void acceptCheckout(CheckoutAcceptRequest request);

    /**
     * 租期到期预警（预计退租时间在 N 天内的在住记录）
     */
    List<OccupantVO> getExpiringOccupants(int daysBefore);
}
