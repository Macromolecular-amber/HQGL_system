package com.logistics.dto.gc;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 资产调剂申请请求
 */
@Data
public class TransferApplyRequest {

    /** 调剂资产ID列表 */
    @NotEmpty(message = "请选择调剂资产")
    private List<Long> assetIds;

    /** 接收单位ID */
    @NotNull(message = "接收单位不能为空")
    private Long receiveUnitId;

    /** 调剂事由 */
    @NotBlank(message = "调剂事由不能为空")
    private String applyReason;

    /** 备注 */
    private String remark;
}
