package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公寓退住验收请求
 */
@Data
public class CheckoutAcceptRequest {

    /** 入住记录ID（必填） */
    @NotNull(message = "入住记录ID不能为空")
    private Long occupantId;

    /** 退住时间（必填） */
    @NotNull(message = "退住时间不能为空")
    private LocalDateTime checkoutTime;

    /** 房屋状况描述 */
    private String roomCondition;

    /** 资产核对结果，键为设施名称，值为 完好/损坏 */
    private Map<String, String> facilityCheckResult;

    /** 结算金额 */
    private BigDecimal settlementAmount;

    /** 结算明细 */
    private String settlementDetail;

    /** 退房照片URL列表 */
    private List<String> checkoutPhotos;

    /** 备注 */
    private String remark;
}
