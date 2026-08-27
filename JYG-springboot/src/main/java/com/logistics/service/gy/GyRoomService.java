package com.logistics.service.gy;

import com.logistics.common.PageResult;
import com.logistics.dto.gy.RoomPageQuery;
import com.logistics.dto.gy.RoomSaveRequest;
import com.logistics.dto.gy.RoomVO;

import java.util.List;

/**
 * 公寓基础信息建档服务
 */
public interface GyRoomService {

    /**
     * 新增或编辑房间
     */
    RoomVO save(RoomSaveRequest request);

    /**
     * 逻辑删除房间
     */
    void delete(Long id);

    /**
     * 分页查询房间
     */
    PageResult<RoomVO> queryPage(RoomPageQuery query);

    /**
     * 房间详情
     */
    RoomVO getDetail(Long id);

    /**
     * 获取空闲房间（供分配选择），可按房间类型过滤
     */
    List<RoomVO> getAvailableRooms(String roomType);
}
