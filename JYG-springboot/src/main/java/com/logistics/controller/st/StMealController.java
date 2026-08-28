package com.logistics.controller.st;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.st.MealCancelRequest;
import com.logistics.dto.st.MealPageQuery;
import com.logistics.dto.st.MealReservationVO;
import com.logistics.dto.st.MealReserveRequest;
import com.logistics.dto.st.MealStatisticsQuery;
import com.logistics.dto.st.MealStatisticsVO;
import com.logistics.service.st.StMealService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 食堂预约订餐管理
 */
@RestController
@RequestMapping("/api/st/meal")
@RequiredArgsConstructor
public class StMealController {

    private final StMealService stMealService;

    /**
     * 预约订餐
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER"})
    @PostMapping("/reserve")
    @Log(module="ST", operation="预约订餐", type="ADD")
    public Result<MealReservationVO> reserve(@RequestBody MealReserveRequest request) {
        return Result.success(stMealService.reserve(request));
    }

    /**
     * 取消预约
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER"})
    @PutMapping("/cancel")
    @Log(module="ST", operation="取消预约", type="UPDATE")
    public Result<Void> cancel(@Valid @RequestBody MealCancelRequest request) {
        stMealService.cancel(request);
        return Result.success();
    }

    /**
     * 分页查询个人预约记录
     */
    @RequiresRoles({"USER","BIZ_ADMIN","DEPT_MANAGER"})
    @GetMapping("/page")
    public Result<PageResult<MealReservationVO>> page(MealPageQuery query) {
        return Result.success(stMealService.queryPage(query));
    }

    /**
     * 按日期查询所有预约（管理员用）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/date/{date}")
    public Result<List<MealReservationVO>> byDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(stMealService.getByDate(date));
    }

    /**
     * 备餐统计（管理员用）
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE","DIRECTOR"})
    @GetMapping("/statistics")
    public Result<MealStatisticsVO> statistics(MealStatisticsQuery query) {
        return Result.success(stMealService.getStatistics(query));
    }
}
