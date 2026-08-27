package com.logistics.service.gc;

import com.logistics.common.PageResult;
import com.logistics.dto.gc.ReturnAcceptRequest;
import com.logistics.dto.gc.ReturnApplyRequest;
import com.logistics.dto.gc.ReturnOrderVO;
import com.logistics.dto.gc.ReturnPageQuery;

/**
 * 公物仓资产归还验收服务
 */
public interface GcReturnService {

    /**
     * 提交归还申请
     */
    ReturnOrderVO apply(ReturnApplyRequest request);

    /**
     * 归还验收
     */
    void accept(ReturnAcceptRequest request);

    /**
     * 分页查询归还单
     */
    PageResult<ReturnOrderVO> queryPage(ReturnPageQuery query);

    /**
     * 归还单详情
     */
    ReturnOrderVO getDetail(Long id);
}
