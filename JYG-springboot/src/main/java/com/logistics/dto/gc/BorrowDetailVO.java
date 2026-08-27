package com.logistics.dto.gc;

import lombok.Data;

/**
 * 资产借用明细视图
 */
@Data
public class BorrowDetailVO {

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 规格型号 */
    private String specModel;

    /** 借用数量 */
    private Integer borrowQuantity;
}
