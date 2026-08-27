package com.logistics.dto.gy;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 公寓保洁验收请求
 */
@Data
public class CleaningAcceptRequest {

    /** 保洁单ID（必填） */
    @NotNull(message = "保洁单ID不能为空")
    private Long cleaningId;

    /** 验收结果：PASS 或 FAIL（必填） */
    @NotBlank(message = "验收结果不能为空")
    private String acceptResult;

    /** 验收评分（1-5） */
    @Min(value = 1, message = "评分范围为1-5")
    @Max(value = 5, message = "评分范围为1-5")
    private Integer acceptScore;

    /** 验收意见 */
    private String acceptRemark;

    /** 执行照片URL列表 */
    private List<String> executePhotos;
}
