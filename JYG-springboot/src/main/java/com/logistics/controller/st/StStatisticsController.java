package com.logistics.controller.st;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.st.ConsumeStatisticsVO;
import com.logistics.dto.st.PurchaseStatisticsVO;
import com.logistics.dto.st.StatisticsQuery;
import com.logistics.dto.st.WastePageQuery;
import com.logistics.dto.st.WasteRecordRequest;
import com.logistics.dto.st.WasteStatisticsVO;
import com.logistics.dto.st.WasteVO;
import com.logistics.service.st.StStatisticsService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 食堂餐余处理与统计分析
 */
@RestController
@RequestMapping("/api/st/statistics")
@RequiredArgsConstructor
public class StStatisticsController {

    private final StStatisticsService stStatisticsService;

    /**
     * 记录餐余
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @PostMapping("/waste/record")
    @Log(module="ST", operation="登记厨余垃圾", type="ADD")
    public Result<WasteVO> recordWaste(@Valid @RequestBody WasteRecordRequest request) {
        return Result.success(stStatisticsService.recordWaste(request));
    }

    /**
     * 分页查询餐余记录
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/waste/page")
    public Result<PageResult<WasteVO>> wastePage(WastePageQuery query) {
        return Result.success(stStatisticsService.queryWastePage(query));
    }

    /**
     * 餐余统计
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/waste/statistics")
    public Result<WasteStatisticsVO> wasteStatistics(StatisticsQuery query) {
        return Result.success(stStatisticsService.getWasteStatistics(query));
    }

    /**
     * 消费统计
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/consume/statistics")
    public Result<ConsumeStatisticsVO> consumeStatistics(StatisticsQuery query) {
        return Result.success(stStatisticsService.getConsumeStatistics(query));
    }

    /**
     * 采购统计
     */
    @RequiresRoles({"BIZ_ADMIN","WAREHOUSE"})
    @GetMapping("/purchase/statistics")
    public Result<PurchaseStatisticsVO> purchaseStatistics(StatisticsQuery query) {
        return Result.success(stStatisticsService.getPurchaseStatistics(query));
    }
}
