package com.logistics.dto.gc;

import lombok.Data;

/**
 * 调剂资产明细视图
 */
@Data
public class TransferDetailVO {

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 规格型号 */
    private String specModel;
}
