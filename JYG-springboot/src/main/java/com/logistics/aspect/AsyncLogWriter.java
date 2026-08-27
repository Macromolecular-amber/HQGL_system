package com.logistics.aspect;

import com.logistics.entity.SysOperationLog;
import com.logistics.repository.SysOperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志异步落库：避免日志写入影响主业务响应时间
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncLogWriter {

    private final SysOperationLogRepository logRepository;

    @Async("logTaskExecutor")
    public void save(SysOperationLog logRecord) {
        try {
            logRepository.save(logRecord);
        } catch (Exception e) {
            // 日志写入失败不影响主流程
            log.warn("操作日志写入失败: {}", e.getMessage());
        }
    }
}
