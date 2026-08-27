package com.logistics.dto.gy;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 公寓保洁单分页查询条件
 */
@Data
public class CleaningPageQuery {

    /** 保洁单号（模糊） */
    private String cleaningNo;

    /** 房间ID */
    private Long roomId;

    /** 保洁类型 */
    private String cleaningType;

    /** 单据状态 */
    private String orderStatus;

    /** 保洁日期起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cleaningDateStart;

    /** 保洁日期止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cleaningDateEnd;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
