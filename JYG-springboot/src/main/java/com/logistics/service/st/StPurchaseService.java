package com.logistics.service.st;

import com.logistics.common.PageResult;
import com.logistics.dto.st.PurchaseAcceptRequest;
import com.logistics.dto.st.PurchaseApplyRequest;
import com.logistics.dto.st.PurchaseAuditRequest;
import com.logistics.dto.st.PurchaseOrderVO;
import com.logistics.dto.st.PurchasePageQuery;

/**
 * 食堂采购供应链管理服务
 */
public interface StPurchaseService {

    /**
     * 提交采购申请
     */
    PurchaseOrderVO apply(PurchaseApplyRequest request);

    /**
     * 采购单审批
     */
    void audit(PurchaseAuditRequest request);

    /**
     * 采购单验收（含入库）
     */
    void accept(PurchaseAcceptRequest request);

    /**
     * 分页查询采购单
     */
    PageResult<PurchaseOrderVO> queryPage(PurchasePageQuery query);

    /**
     * 采购单详情
     */
    PurchaseOrderVO getDetail(Long id);

    /**
     * 定时任务：检查并处理过期单据
     */
    void checkExpiredOrders();
}
