package com.logistics.dto.st;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 采购单验收请求
 */
@Data
public class PurchaseAcceptRequest {

    /** 采购单ID（必填） */
    @NotNull(message = "采购单ID不能为空")
    private Long orderId;

    /** 验收结果（必填）：PASS 通过 / FAIL 不通过 */
    @NotBlank(message = "验收结果不能为空")
    private String acceptResult;

    /** 验收意见 */
    private String acceptRemark;

    /** 实际验收明细（仅 PASS 时使用） */
    @Valid
    private List<ReceivedItemDTO> receivedItems;
}
