package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_user 实体
 */
@Data
@Entity
@Table(name = "sys_user")
public class SysUser {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** username */
    @Column(name = "username", length = 50)
    private String username;

    /** password */
    @Column(name = "password", length = 255)
    private String password;

    /** real_name */
    @Column(name = "real_name", length = 50)
    private String realName;

    /** phone */
    @Column(name = "phone", length = 20)
    private String phone;

    /** email */
    @Column(name = "email", length = 100)
    private String email;

    /** face_id */
    @Column(name = "face_id", length = 100)
    private String faceId;

    /** face_image_url */
    @Column(name = "face_image_url", length = 255)
    private String faceImageUrl;

    /** unit_id */
    @Column(name = "unit_id")
    private Long unitId;

    /** unit_name */
    @Column(name = "unit_name", length = 100)
    private String unitName;

    /** dept_id */
    @Column(name = "dept_id")
    private Long deptId;

    /** position */
    @Column(name = "position", length = 50)
    private String position;

    /** user_type */
    @Column(name = "user_type", length = 20)
    private String userType;

    /** user_status */
    @Column(name = "user_status", length = 20)
    private String userStatus;

    /** ext_json */
    @Column(name = "ext_json", columnDefinition = "jsonb")
    private String extJson;

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
