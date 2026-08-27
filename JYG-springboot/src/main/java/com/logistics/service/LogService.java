package com.logistics.service;

import com.logistics.common.PageResult;
import com.logistics.dto.sys.LogPageQuery;
import com.logistics.entity.SysOperationLog;

/**
 * 操作日志服务
 */
public interface LogService {

    /**
     * 分页查询操作日志
     */
    PageResult<SysOperationLog> queryPage(LogPageQuery query);
}
