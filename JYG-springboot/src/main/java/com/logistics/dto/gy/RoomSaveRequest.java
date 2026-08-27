package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 房间新增/编辑请求
 */
@Data
public class RoomSaveRequest {

    /** 房间ID（编辑时必填） */
    private Long id;

    /** 楼栋（必填） */
    @NotBlank(message = "楼栋不能为空")
    private String building;

    /** 楼层（必填，≥0） */
    @NotNull(message = "楼层不能为空")
    @Min(value = 0, message = "楼层不能小于0")
    private Integer floor;

    /** 房间号（必填） */
    @NotBlank(message = "房间号不能为空")
    private String roomNo;

    /** 房间类型：expert_apartment 专家公寓 / talent_apartment 人才公寓（必填） */
    @NotBlank(message = "房间类型不能为空")
    private String roomType;

    /** 户型 */
    private String layout;

    /** 面积（≥0） */
    @DecimalMin(value = "0", message = "面积不能小于0")
    private BigDecimal area;

    /** 配套设施，如 {"家具":["床","衣柜"], "家电":["空调","电视"]} */
    private Map<String, List<String>> facilities;

    /** 备注 */
    private String remark;
}
