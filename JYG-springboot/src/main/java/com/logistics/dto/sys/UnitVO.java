package com.logistics.dto.sys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单位下拉选项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitVO {

    /** 单位ID */
    private Long id;

    /** 单位名称 */
    private String unitName;
}
