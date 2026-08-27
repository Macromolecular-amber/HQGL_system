package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产借用申请请求
 */
@Data
public class BorrowApplyRequest {

    /** 借用资产ID列表 */
    @NotEmpty(message = "请选择借用资产")
    private List<Long> assetIds;

    /** 借用开始时间 */
    @NotNull(message = "借用开始时间不能为空")
    private LocalDateTime borrowStart;

    /** 借用结束时间 */
    @NotNull(message = "借用结束时间不能为空")
    private LocalDateTime borrowEnd;

    /** 借用事由 */
    @NotBlank(message = "借用事由不能为空")
    private String borrowReason;

    /** 备注 */
    private String remark;

    /** 申请人ID */
    @NotNull(message = "申请人不能为空")
    private Long applicantId;

    /** 申请单位ID */
    @NotNull(message = "申请单位不能为空")
    private Long applicantUnitId;
}
