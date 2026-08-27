package com.logistics.dto.cl;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 配件明细
 */
@Data
public class PartsItemDTO {

    /** 配件名称 */
    private String name;

    /** 数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal price;
}
