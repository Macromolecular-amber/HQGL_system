package com.logistics.dto.gy;

import lombok.Data;

/**
 * 房间分页查询条件
 */
@Data
public class RoomPageQuery {

    /** 楼栋（模糊） */
    private String building;

    /** 楼层 */
    private Integer floor;

    /** 房间类型 */
    private String roomType;

    /** 房间状态 */
    private String roomStatus;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
