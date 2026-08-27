package com.logistics.dto.st;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 食堂采购申请请求
 */
@Data
public class PurchaseApplyRequest {

    /** 采购原因（必填） */
    @NotBlank(message = "采购原因不能为空")
    private String purchaseReason;

    /** 采购明细（必填，至少1项） */
    @Valid
    @NotEmpty(message = "采购明细至少包含1项")
    private List<PurchaseItemDTO> items;

    /** 供应商ID（可选） */
    private Long supplierId;

    /** 供应商名称（可选） */
    private String supplierName;
}
