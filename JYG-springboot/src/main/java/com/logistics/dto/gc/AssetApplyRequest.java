package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 资产入仓申请请求
 */
@Data
public class AssetApplyRequest {

    /** 资产名称 */
    @NotBlank(message = "资产名称不能为空")
    private String assetName;

    /** 资产分类编码，如 FURNITURE */
    @NotBlank(message = "资产分类不能为空")
    private String categoryCode;

    /** 规格型号 */
    private String specModel;

    /** 品牌 */
    private String brand;

    /** 原值 */
    @NotNull(message = "资产原值不能为空")
    @DecimalMin(value = "0", message = "资产原值不能小于0")
    private BigDecimal originalValue;

    /** 购置日期 */
    @NotNull(message = "购置日期不能为空")
    private LocalDate purchaseDate;

    /** 使用年限（年） */
    @NotNull(message = "使用年限不能为空")
    @Min(value = 1, message = "使用年限必须大于0")
    private Integer usefulLife;

    /** 存放地点 */
    private String location;

    /** 所属单位ID */
    @NotNull(message = "所属单位不能为空")
    private Long ownerUnitId;

    /** 资产描述 */
    private String description;

    /** 图片URL列表 */
    private List<String> photoUrls;

    /** 备注 */
    private String remark;
}
