package com.logistics.service.st;

import com.logistics.common.PageResult;
import com.logistics.dto.st.ConsumeStatisticsVO;
import com.logistics.dto.st.PurchaseStatisticsVO;
import com.logistics.dto.st.StatisticsQuery;
import com.logistics.dto.st.WastePageQuery;
import com.logistics.dto.st.WasteRecordRequest;
import com.logistics.dto.st.WasteStatisticsVO;
import com.logistics.dto.st.WasteVO;

/**
 * 食堂餐余处理与统计分析服务
 */
public interface StStatisticsService {

    /**
     * 记录餐余
     */
    WasteVO recordWaste(WasteRecordRequest request);

    /**
     * 分页查询餐余记录
     */
    PageResult<WasteVO> queryWastePage(WastePageQuery query);

    /**
     * 餐余统计
     */
    WasteStatisticsVO getWasteStatistics(StatisticsQuery query);

    /**
     * 消费统计
     */
    ConsumeStatisticsVO getConsumeStatistics(StatisticsQuery query);

    /**
     * 采购统计
     */
    PurchaseStatisticsVO getPurchaseStatistics(StatisticsQuery query);
}
