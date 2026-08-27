package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * st_meal_reservation 实体
 */
@Data
@Entity
@Table(name = "st_meal_reservation")
public class StMealReservation {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** user_id */
    @Column(name = "user_id")
    private Long userId;

    /** user_name */
    @Column(name = "user_name", length = 50)
    private String userName;

    /** unit_id */
    @Column(name = "unit_id")
    private Long unitId;

    /** unit_name */
    @Column(name = "unit_name", length = 100)
    private String unitName;

    /** meal_date */
    @Column(name = "meal_date")
    private LocalDate mealDate;

    /** meal_type */
    @Column(name = "meal_type", length = 20)
    private String mealType;

    /** meal_count */
    @Column(name = "meal_count")
    private Integer mealCount;

    /** reservation_time */
    @Column(name = "reservation_time")
    private OffsetDateTime reservationTime;

    /** cancel_time */
    @Column(name = "cancel_time")
    private OffsetDateTime cancelTime;

    /** 是否已取消 */
    @Column(name = "is_cancelled")
    private Boolean isCancelled;

    /** 备注 */
    @Column(name = "remark", length = 500)
    private String remark;

    /** create_time */
    @Column(name = "create_time")
    private OffsetDateTime createTime;

    /** update_time */
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

}
