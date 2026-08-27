package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 公寓维修申请请求
 */
@Data
public class RepairApplyRequest {

    /** 房间ID（必填） */
    @NotNull(message = "房间不能为空")
    private Long roomId;

    /** 维修类型：MAINTENANCE 保养 / REPAIR 维修（必填） */
    @NotBlank(message = "维修类型不能为空")
    private String repairType;

    /** 故障描述（必填） */
    @NotBlank(message = "故障描述不能为空")
    private String faultDesc;

    /** 故障照片URL列表 */
    private List<String> faultPhotos;

    /** 紧急程度：HIGH/MEDIUM/LOW */
    private String urgencyLevel;

    /** 费用承担：UNIT 单位承担 / PERSONAL 个人自费（必填） */
    @NotBlank(message = "费用承担方式不能为空")
    private String costType;

    /** 备注 */
    private String remark;
}
