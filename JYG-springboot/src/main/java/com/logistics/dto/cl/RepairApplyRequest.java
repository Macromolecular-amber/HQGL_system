package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 维修申请请求
 */
@Data
public class RepairApplyRequest {

    /** 车辆ID（必填） */
    @NotNull(message = "车辆不能为空")
    private Long vehicleId;

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

    /** 维修时里程（必填） */
    @NotNull(message = "维修里程不能为空")
    private BigDecimal repairMileage;

    /** 备注 */
    private String remark;
}
