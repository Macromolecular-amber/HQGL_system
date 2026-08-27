package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

/**
 * sys_unit 实体
 */
@Data
@Entity
@Table(name = "sys_unit")
public class SysUnit {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** unit_code */
    @Column(name = "unit_code", length = 50)
    private String unitCode;

    /** unit_name */
    @Column(name = "unit_name", length = 100)
    private String unitName;

    /** unit_type */
    @Column(name = "unit_type", length = 20)
    private String unitType;

    /** parent_id */
    @Column(name = "parent_id")
    private Long parentId;

    /** contact_person */
    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    /** contact_phone */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** sort_order */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** remark */
    @Column(name = "remark", length = 500)
    private String remark;

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
