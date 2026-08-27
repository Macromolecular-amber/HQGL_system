package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分配入住请求（专家公寓直接分配）
 */
@Data
public class OccupantAssignRequest {

    /** 房间ID（必填） */
    @NotNull(message = "房间不能为空")
    private Long roomId;

    /** 入住人姓名（必填） */
    @NotBlank(message = "入住人姓名不能为空")
    private String occupantName;

    /** 身份证号 */
    private String idCard;

    /** 联系电话（必填） */
    @NotBlank(message = "联系电话不能为空")
    private String phone;

    /** 所属单位ID（必填） */
    @NotNull(message = "所属单位不能为空")
    private Long unitId;

    /** 职务 */
    private String position;

    /** 入住时间（必填） */
    @NotNull(message = "入住时间不能为空")
    private LocalDateTime checkinTime;

    /** 预计退租时间（必填） */
    @NotNull(message = "预计退租时间不能为空")
    private LocalDateTime expectedLeaveTime;

    /** 租金（人才公寓必填，专家公寓默认 0） */
    private BigDecimal rentAmount;

    /** 备注 */
    private String remark;
}
