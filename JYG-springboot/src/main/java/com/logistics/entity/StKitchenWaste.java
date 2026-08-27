package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * st_kitchen_waste 实体（食堂餐余记录）
 */
@Data
@Entity
@Table(name = "st_kitchen_waste")
public class StKitchenWaste {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 记录日期 */
    @Column(name = "record_date")
    private LocalDate recordDate;

    /** 餐次：BREAKFAST / LUNCH / DINNER */
    @Column(name = "meal_type", length = 20)
    private String mealType;

    /** 餐余重量（kg） */
    @Column(name = "waste_weight")
    private BigDecimal wasteWeight;

    /** 餐余类型：FOOD 食物 / PACKAGING 包装 / OTHER 其他 */
    @Column(name = "waste_type", length = 20)
    private String wasteType;

    /** 处理方式：COMPOST 堆肥 / FEED 饲料 / WASTE 废弃物 */
    @Column(name = "disposal_method", length = 20)
    private String disposalMethod;

    /** 处理人 */
    @Column(name = "disposal_person", length = 50)
    private String disposalPerson;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** 创建人 */
    @Column(name = "create_by")
    private Long createBy;

    /** 创建时间 */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** 更新人 */
    @Column(name = "update_by")
    private Long updateBy;

    /** 更新时间 */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    /** 逻辑删除 */
    @Column(name = "is_deleted")
    private Boolean isDeleted;

}
