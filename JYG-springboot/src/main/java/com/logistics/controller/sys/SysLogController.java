package com.logistics.controller.sys;

import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.sys.LogPageQuery;
import com.logistics.entity.SysOperationLog;
import com.logistics.security.RequiresRoles;
import com.logistics.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志查询
 */
@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@RequiresRoles({"ADMIN", "DIRECTOR"})
public class SysLogController {

    private final LogService logService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<PageResult<SysOperationLog>> page(LogPageQuery query) {
        return Result.success(logService.queryPage(query));
    }
}
