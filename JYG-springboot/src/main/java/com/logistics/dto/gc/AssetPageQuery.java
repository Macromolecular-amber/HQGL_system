package com.logistics.dto.gc;

import lombok.Data;

/**
 * 资产分页查询条件
 */
@Data
public class AssetPageQuery {

    /** 资产名称（模糊） */
    private String assetName;

    /** 资产分类编码 */
    private String categoryCode;

    /** 资产状态 */
    private String status;

    /** 所属单位ID */
    private Long ownerUnitId;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
