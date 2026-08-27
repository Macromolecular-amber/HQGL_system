package com.logistics.service.gy;

import com.logistics.common.PageResult;
import com.logistics.dto.gy.CleaningAcceptRequest;
import com.logistics.dto.gy.CleaningApplyRequest;
import com.logistics.dto.gy.CleaningAssignRequest;
import com.logistics.dto.gy.CleaningAuditRequest;
import com.logistics.dto.gy.CleaningPageQuery;
import com.logistics.dto.gy.CleaningVO;

import java.util.List;

/**
 * 公寓保洁服务管理服务
 */
public interface GyCleaningService {

    /**
     * 提交保洁申请
     */
    CleaningVO apply(CleaningApplyRequest request);

    /**
     * 保洁审批
     */
    void audit(CleaningAuditRequest request);

    /**
     * 保洁派单
     */
    void assign(CleaningAssignRequest request);

    /**
     * 保洁验收
     */
    void accept(CleaningAcceptRequest request);

    /**
     * 分页查询保洁单
     */
    PageResult<CleaningVO> queryPage(CleaningPageQuery query);

    /**
     * 保洁单详情
     */
    CleaningVO getDetail(Long id);

    /**
     * 获取某房间的所有保洁记录
     */
    List<CleaningVO> getByRoomId(Long roomId);
}
