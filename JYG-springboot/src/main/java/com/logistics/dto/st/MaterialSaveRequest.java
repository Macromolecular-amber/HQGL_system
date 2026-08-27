package com.logistics.dto.st;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 食堂物资新增/编辑请求
 */
@Data
public class MaterialSaveRequest {

    /** 物资ID（编辑时必填） */
    private Long id;

    /** 物资编码（必填，唯一） */
    @NotBlank(message = "物资编码不能为空")
    private String materialCode;

    /** 物资名称（必填） */
    @NotBlank(message = "物资名称不能为空")
    private String materialName;

    /** 分类（必填）：FRESH_INGREDIENTS 生鲜食材 / CONDIMENT 调味品 / DAILY_GOODS 日用品 */
    @NotBlank(message = "物资分类不能为空")
    private String category;

    /** 规格 */
    private String spec;

    /** 计量单位（必填） */
    @NotBlank(message = "计量单位不能为空")
    private String unit;

    /** 保质期（天） */
    private Integer shelfLife;

    /** 安全库存，默认 0 */
    @DecimalMin(value = "0", message = "安全库存不能小于0")
    private BigDecimal safetyStock;

    /** 库存上限，默认 1000 */
    @DecimalMin(value = "0", message = "库存上限不能小于0")
    private BigDecimal maxStock;

    /** 当前单价 */
    @DecimalMin(value = "0", message = "当前单价不能小于0")
    private BigDecimal currentPrice;

    /** 备注 */
    private String remark;
}
