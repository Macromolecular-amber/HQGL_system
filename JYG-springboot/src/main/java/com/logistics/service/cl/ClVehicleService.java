package com.logistics.service.cl;

import com.logistics.common.PageResult;
import com.logistics.dto.cl.VehiclePageQuery;
import com.logistics.dto.cl.VehicleSaveRequest;
import com.logistics.dto.cl.VehicleVO;

import java.util.List;

/**
 * 公务用车车辆档案服务
 */
public interface ClVehicleService {

    /**
     * 新增或编辑车辆
     */
    VehicleVO save(VehicleSaveRequest request);

    /**
     * 逻辑删除车辆
     */
    void delete(Long id);

    /**
     * 分页查询车辆
     */
    PageResult<VehicleVO> queryPage(VehiclePageQuery query);

    /**
     * 车辆详情
     */
    VehicleVO getDetail(Long id);

    /**
     * 获取所有可用车辆（供调度选择）
     */
    List<VehicleVO> getAvailableVehicles();
}
