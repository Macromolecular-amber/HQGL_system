package com.logistics.dto.gy;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 房间视图：包含 GyRoom 全部字段，并补充中文名与入住信息
 */
@Data
public class RoomVO {

    /** id */
    private Long id;

    /** 楼栋 */
    private String building;

    /** 楼层 */
    private Integer floor;

    /** 房间号 */
    private String roomNo;

    /** 房间类型 */
    private String roomType;

    /** 户型 */
    private String layout;

    /** 面积 */
    private BigDecimal area;

    /** 朝向 */
    private String orientation;

    /** 配套设施 */
    private String facilities;

    /** 房间状态 */
    private String roomStatus;

    /** 当前入住人ID */
    private Long currentOccupantId;

    /** 当前入住人姓名 */
    private String currentOccupantName;

    /** 当前入住单位ID */
    private Long currentUnitId;

    /** 资产ID列表 */
    private String assetIds;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private OffsetDateTime createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private OffsetDateTime updateTime;

    /** 逻辑删除 */
    private Boolean isDeleted;

    /** 房间类型中文名 */
    private String roomTypeLabel;

    /** 房间状态中文名 */
    private String roomStatusLabel;

    /** 当前入住人数 */
    private Integer occupantCount;
}
