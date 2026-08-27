package com.logistics.service.st;

import com.logistics.common.PageResult;
import com.logistics.dto.st.MealCancelRequest;
import com.logistics.dto.st.MealPageQuery;
import com.logistics.dto.st.MealReservationVO;
import com.logistics.dto.st.MealReserveRequest;
import com.logistics.dto.st.MealStatisticsQuery;
import com.logistics.dto.st.MealStatisticsVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 食堂预约订餐管理服务
 */
public interface StMealService {

    /**
     * 预约订餐
     */
    MealReservationVO reserve(MealReserveRequest request);

    /**
     * 取消预约
     */
    void cancel(MealCancelRequest request);

    /**
     * 分页查询个人预约记录
     */
    PageResult<MealReservationVO> queryPage(MealPageQuery query);

    /**
     * 按日期查询所有预约（管理员用）
     */
    List<MealReservationVO> getByDate(LocalDate date);

    /**
     * 备餐统计（管理员用）
     */
    MealStatisticsVO getStatistics(MealStatisticsQuery query);
}
