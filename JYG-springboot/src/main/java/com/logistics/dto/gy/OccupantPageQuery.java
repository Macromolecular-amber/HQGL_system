package com.logistics.dto.gy;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 入住记录分页查询条件
 */
@Data
public class OccupantPageQuery {

    /** 公寓类型：expert_apartment / talent_apartment */
    private String roomType;

    /** 入住人姓名（模糊） */
    private String occupantName;

    /** 所属单位ID */
    private Long unitId;

    /** 入住状态：active / resigned / pending */
    private String occupantStatus;

    /** 入住时间起 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    /** 入住时间止 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 20 */
    private Integer size = 20;
}
