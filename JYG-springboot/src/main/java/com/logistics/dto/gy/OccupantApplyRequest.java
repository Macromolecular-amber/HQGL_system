package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 人才公寓入住申请请求
 */
@Data
public class OccupantApplyRequest {

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

    /** 申请原因（必填） */
    @NotBlank(message = "申请原因不能为空")
    private String applyReason;

    /** 备注 */
    private String remark;
}
