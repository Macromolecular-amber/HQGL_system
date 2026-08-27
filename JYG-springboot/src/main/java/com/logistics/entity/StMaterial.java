package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * st_material 实体
 */
@Data
@Entity
@Table(name = "st_material")
public class StMaterial {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** material_code */
    @Column(name = "material_code", length = 50)
    private String materialCode;

    /** material_name */
    @Column(name = "material_name", length = 100)
    private String materialName;

    /** category */
    @Column(name = "category", length = 50)
    private String category;

    /** spec */
    @Column(name = "spec", length = 50)
    private String spec;

    /** unit */
    @Column(name = "unit", length = 10)
    private String unit;

    /** shelf_life */
    @Column(name = "shelf_life")
    private Integer shelfLife;

    /** safety_stock */
    @Column(name = "safety_stock")
    private BigDecimal safetyStock;

    /** max_stock */
    @Column(name = "max_stock")
    private BigDecimal maxStock;

    /** current_price */
    @Column(name = "current_price")
    private BigDecimal currentPrice;

    /** last_price */
    @Column(name = "last_price")
    private BigDecimal lastPrice;

    /** current_stock */
    @Column(name = "current_stock")
    private BigDecimal currentStock;

    /** occupied_stock */
    @Column(name = "occupied_stock")
    private BigDecimal occupiedStock;

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
