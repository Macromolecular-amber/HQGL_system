package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_user_role 实体
 */
@Data
@Entity
@Table(name = "sys_user_role")
public class SysUserRole {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** user_id */
    @Column(name = "user_id")
    private Long userId;

    /** role_id */
    @Column(name = "role_id")
    private Long roleId;

    /** create_by */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

}
