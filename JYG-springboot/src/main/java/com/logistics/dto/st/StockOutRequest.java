package com.logistics.dto.st;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 领用出库请求
 */
@Data
public class StockOutRequest {

    /** 出库明细（必填，至少1项） */
    @Valid
    @NotEmpty(message = "出库明细至少包含1项")
    private List<StockOutItemDTO> items;

    /** 出库备注 */
    private String remark;
}
