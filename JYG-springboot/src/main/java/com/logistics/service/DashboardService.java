package com.logistics.service;

import com.logistics.dto.dashboard.DashboardStatisticsVO;
import com.logistics.dto.dashboard.LeadershipDashboardVO;
import com.logistics.dto.dashboard.MessageVO;
import com.logistics.dto.dashboard.TodoVO;
import com.logistics.dto.dashboard.TrendVO;

import java.util.List;

/**
 * 首页数据聚合服务
 */
public interface DashboardService {

    /**
     * 统计卡片数据
     */
    DashboardStatisticsVO getStatistics(Long userId);

    /**
     * 待办审批列表
     */
    List<TodoVO> getTodos(Long userId);

    /**
     * 消息通知列表
     */
    List<MessageVO> getMessages(Long userId);

    /**
     * 近7天资产趋势
     */
    TrendVO getTrend();

    /**
     * 领导驾驶舱聚合数据
     */
    LeadershipDashboardVO getLeadershipData();
}
