package com.logistics.dto.auth;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    /** JWT 令牌 */
    private String token;

    /** 用户信息 */
    private UserInfo userInfo;

    /**
     * 登录用户信息
     */
    @Data
    public static class UserInfo {

        /** 用户ID */
        private Long id;

        /** 用户名 */
        private String username;

        /** 真实姓名 */
        private String realName;

        /** 所属单位ID */
        private Long unitId;

        /** 所属单位名称 */
        private String unitName;

        /** 角色列表 */
        private List<String> roles;
    }
}
