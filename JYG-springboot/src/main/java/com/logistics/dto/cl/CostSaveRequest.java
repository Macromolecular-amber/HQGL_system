package com.logistics.dto.cl;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用登记请求
 */
@Data
public class CostSaveRequest {

    /** 车辆ID（必填） */
    @NotNull(message = "车辆不能为空")
    private Long vehicleId;

    /** 费用类型：FUEL/REPAIR/INSURANCE/TOLL/ETC/PARKING/OTHER（必填） */
    @NotBlank(message = "费用类型不能为空")
    private String costType;

    /** 费用金额（必填，≥0） */
    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0", message = "费用金额不能小于0")
    private BigDecimal costAmount;

    /** 费用发生时间（必填） */
    @NotNull(message = "费用时间不能为空")
    private LocalDateTime costTime;

    /** 费用说明 */
    private String costDesc;

    /** 关联业务单号 */
    private String bizOrderNo;

    /** 附件URL列表 */
    private List<String> attachmentUrls;

    /** 加油时里程（加油类型必填） */
    private BigDecimal currentMileage;

    /** 加油量（升，加油类型必填） */
    private BigDecimal fuelQuantity;
}
