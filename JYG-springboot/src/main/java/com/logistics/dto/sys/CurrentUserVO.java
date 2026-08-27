package com.logistics.dto.sys;

import lombok.Data;

/**
 * 当前登录用户信息
 */
@Data
public class CurrentUserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String realName;

    /** 单位ID */
    private Long unitId;

    /** 单位名称 */
    private String unitName;
}
