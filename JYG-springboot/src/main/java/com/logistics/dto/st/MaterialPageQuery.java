package com.logistics.dto.st;

import lombok.Data;

/**
 * 食堂物资分页查询条件
 */
@Data
public class MaterialPageQuery {

    /** 物资编码（模糊） */
    private String materialCode;

    /** 物资名称（模糊） */
    private String materialName;

    /** 分类 */
    private String category;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
