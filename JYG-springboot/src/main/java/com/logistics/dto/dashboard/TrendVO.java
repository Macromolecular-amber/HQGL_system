package com.logistics.dto.dashboard;

import lombok.Data;

import java.util.List;

/**
 * 近7天资产趋势数据
 */
@Data
public class TrendVO {

    /** 日期列表（MM/dd） */
    private List<String> dates;

    /** 每日入仓数 */
    private List<Long> inStock;

    /** 每日借用数 */
    private List<Long> borrowed;
}
