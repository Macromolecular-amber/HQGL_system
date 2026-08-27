package com.logistics.dto.st;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 库存流水分页查询条件
 */
@Data
public class InventoryPageQuery {

    /** 物资ID */
    private Long materialId;

    /** 流水类型：IN 入库 / OUT 出库 / ADJUST 调整 / LOSS 损耗 */
    private String recordType;

    /** 起始时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 结束时间 */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
