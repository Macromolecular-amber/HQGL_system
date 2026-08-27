package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_config 实体
 */
@Data
@Entity
@Table(name = "sys_config")
public class SysConfig {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** config_key */
    @Column(name = "config_key", length = 100)
    private String configKey;

    /** config_value */
    @Column(name = "config_value", columnDefinition = "text")
    private String configValue;

    /** config_type */
    @Column(name = "config_type", length = 20)
    private String configType;

    /** config_group */
    @Column(name = "config_group", length = 50)
    private String configGroup;

    /** config_desc */
    @Column(name = "config_desc", length = 200)
    private String configDesc;

    /** is_public */
    @Column(name = "is_public")
    private Boolean isPublic;

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
