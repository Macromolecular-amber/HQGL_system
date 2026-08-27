package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产归还申请请求
 */
@Data
public class ReturnApplyRequest {

    /** 借用单ID */
    @NotNull(message = "借用单不能为空")
    private Long borrowOrderId;

    /** 归还资产ID列表，为空表示全部归还 */
    private List<Long> assetIds;

    /** 计划归还时间 */
    @NotNull(message = "计划归还时间不能为空")
    private LocalDateTime planReturnTime;
}
