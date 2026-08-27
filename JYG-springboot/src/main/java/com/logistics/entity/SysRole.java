package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_role 实体
 */
@Data
@Entity
@Table(name = "sys_role")
public class SysRole {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** role_code */
    @Column(name = "role_code", length = 50)
    private String roleCode;

    /** role_name */
    @Column(name = "role_name", length = 50)
    private String roleName;

    /** role_desc */
    @Column(name = "role_desc", length = 200)
    private String roleDesc;

    /** role_type */
    @Column(name = "role_type", length = 20)
    private String roleType;

    /** is_system */
    @Column(name = "is_system")
    private Boolean isSystem;

    /** sort_order */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** create_by */
    @Column(name = "create_by")
    private Long createBy;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_by */
    @Column(name = "update_by")
    private Long updateBy;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    /** is_deleted */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
