package com.logistics.service.gc;

import com.logistics.common.PageResult;
import com.logistics.dto.gc.BorrowApplyRequest;
import com.logistics.dto.gc.BorrowAuditRequest;
import com.logistics.dto.gc.BorrowDetailVO;
import com.logistics.dto.gc.BorrowOrderVO;
import com.logistics.dto.gc.BorrowPageQuery;

import java.util.List;

/**
 * 公物仓资产借用服务
 */
public interface GcBorrowService {

    /**
     * 提交借用申请
     */
    BorrowOrderVO apply(BorrowApplyRequest request);

    /**
     * 审批借用申请
     */
    void audit(BorrowAuditRequest request);

    /**
     * 分页查询借用单
     */
    PageResult<BorrowOrderVO> queryPage(BorrowPageQuery query);

    /**
     * 借用单详情
     */
    BorrowOrderVO getDetail(Long id);

    /**
     * 获取借用单下的资产列表
     */
    List<BorrowDetailVO> getBorrowAssets(Long borrowOrderId);
}
