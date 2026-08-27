package com.logistics.service.gc;

import com.logistics.common.PageResult;
import com.logistics.dto.gc.DisposeApplyRequest;
import com.logistics.dto.gc.DisposeAuditRequest;
import com.logistics.dto.gc.DisposeIncomeRequest;
import com.logistics.dto.gc.DisposeOrderVO;
import com.logistics.dto.gc.DisposePageQuery;
import com.logistics.dto.gc.TransferApplyRequest;
import com.logistics.dto.gc.TransferAuditRequest;
import com.logistics.dto.gc.TransferOrderVO;
import com.logistics.dto.gc.TransferPageQuery;

/**
 * 公物仓调剂共享与处置服务
 */
public interface GcTransferService {

    /**
     * 提交调剂申请
     */
    TransferOrderVO apply(TransferApplyRequest request);

    /**
     * 审批调剂/处置申请（按 transfer_type 区分）
     */
    void audit(TransferAuditRequest request);

    /**
     * 分页查询调剂单
     */
    PageResult<TransferOrderVO> queryPage(TransferPageQuery query);

    /**
     * 调剂单详情
     */
    TransferOrderVO getDetail(Long id);

    /**
     * 提交处置申请
     */
    DisposeOrderVO applyDispose(DisposeApplyRequest request);

    /**
     * 审批处置申请（仅限处置单）
     */
    void auditDispose(DisposeAuditRequest request);

    /**
     * 录入处置收益（处置执行后）
     */
    void recordDisposeIncome(DisposeIncomeRequest request);

    /**
     * 分页查询处置单（仅 DISPOSE 类型）
     */
    PageResult<DisposeOrderVO> queryDisposePage(DisposePageQuery query);

    /**
     * 处置单详情
     */
    DisposeOrderVO getDisposeDetail(Long id);
}
