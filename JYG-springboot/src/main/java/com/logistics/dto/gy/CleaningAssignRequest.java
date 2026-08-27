package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 公寓保洁派单请求
 */
@Data
public class CleaningAssignRequest {

    /** 保洁单ID（必填） */
    @NotNull(message = "保洁单ID不能为空")
    private Long cleaningId;

    /** 保洁员ID（必填） */
    @NotNull(message = "保洁员不能为空")
    private Long assigneeId;

    /** 保洁公司 */
    private String assigneeCompany;
}
