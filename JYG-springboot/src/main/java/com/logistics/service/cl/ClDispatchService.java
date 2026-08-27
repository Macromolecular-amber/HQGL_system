package com.logistics.service.cl;

import com.logistics.common.PageResult;
import com.logistics.dto.cl.DispatchPageQuery;
import com.logistics.dto.cl.DispatchRequest;
import com.logistics.dto.cl.DispatchVO;
import com.logistics.dto.cl.ReturnRequest;

import java.util.List;

/**
 * 公务用车车辆调度服务
 */
public interface ClDispatchService {

    /**
     * 派单
     */
    DispatchVO dispatch(DispatchRequest request);

    /**
     * 开始出车：待出车 -> 出车中，并自动生成模拟轨迹
     */
    DispatchVO startTrip(Long dispatchId);

    /**
     * 车辆归还
     */
    void returnVehicle(ReturnRequest request);

    /**
     * 分页查询调度单
     */
    PageResult<DispatchVO> queryPage(DispatchPageQuery query);

    /**
     * 调度单详情
     */
    DispatchVO getDetail(Long id);

    /**
     * 根据申请ID查询派单记录
     */
    List<DispatchVO> getByApplyId(Long applyId);
}
