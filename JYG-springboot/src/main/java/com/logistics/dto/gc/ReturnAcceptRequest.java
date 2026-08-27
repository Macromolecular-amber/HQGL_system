package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 资产归还验收请求
 */
@Data
public class ReturnAcceptRequest {

    /** 归还单ID */
    @NotNull(message = "归还单ID不能为空")
    private Long returnOrderId;

    /** 验收结果：PASS 通过 / FAIL 不通过 / REPAIR 需维修 */
    @NotBlank(message = "验收结果不能为空")
    private String acceptResult;

    /** 损坏描述 */
    private String damageInfo;

    /** 损坏责任：UNIT 单位 / PERSONAL 个人 / NATURAL 自然损耗 */
    private String damageResponsibility;

    /** 维修费用 */
    private BigDecimal repairCost;

    /** 赔偿金额 */
    private BigDecimal compensationAmount;

    /** 验收意见 */
    private String acceptRemark;

    /** 验收照片URL列表 */
    private List<String> acceptPhotos;
}
