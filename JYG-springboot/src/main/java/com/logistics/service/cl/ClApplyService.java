package com.logistics.service.cl;

import com.logistics.common.PageResult;
import com.logistics.dto.cl.ApplyAuditRequest;
import com.logistics.dto.cl.ApplyPageQuery;
import com.logistics.dto.cl.ApplyRequest;
import com.logistics.dto.cl.ApplyVO;

/**
 * 公务用车申请与审批服务
 */
public interface ClApplyService {

    /**
     * 提交用车申请
     */
    ApplyVO apply(ApplyRequest request);

    /**
     * 审批用车申请
     */
    void audit(ApplyAuditRequest request);

    /**
     * 分页查询用车申请
     */
    PageResult<ApplyVO> queryPage(ApplyPageQuery query);

    /**
     * 用车申请详情
     */
    ApplyVO getDetail(Long id);

    /**
     * 取消用车申请（仅 PENDING 状态可取消）
     */
    void cancel(Long id);
}
