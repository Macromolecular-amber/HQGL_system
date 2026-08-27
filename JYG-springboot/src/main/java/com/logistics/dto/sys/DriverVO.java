package com.logistics.dto.sys;

import lombok.Data;

/**
 * 驾驶员视图：驾驶员下拉选择用
 */
@Data
public class DriverVO {

    /** 用户ID */
    private Long id;

    /** 姓名 */
    private String realName;

    /** 电话 */
    private String phone;
}
