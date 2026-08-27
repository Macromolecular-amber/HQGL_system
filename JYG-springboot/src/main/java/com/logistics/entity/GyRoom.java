package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.ColumnTransformer;

/**
 * gy_room 实体
 */
@Data
@Entity
@Table(name = "gy_room")
public class GyRoom {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** building */
    @Column(name = "building", length = 20)
    private String building;

    /** floor */
    @Column(name = "floor")
    private Integer floor;

    /** room_no */
    @Column(name = "room_no", length = 20)
    private String roomNo;

    /** room_type */
    @Column(name = "room_type", length = 20)
    private String roomType;

    /** layout */
    @Column(name = "layout", length = 20)
    private String layout;

    /** area */
    @Column(name = "area")
    private BigDecimal area;

    /** orientation */
    @Column(name = "orientation", length = 10)
    private String orientation;

    /** facilities */
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "facilities", columnDefinition = "jsonb")
    private String facilities;

    /** room_status */
    @Column(name = "room_status", length = 20)
    private String roomStatus;

    /** current_occupant_id */
    @Column(name = "current_occupant_id")
    private Long currentOccupantId;

    /** current_occupant_name */
    @Column(name = "current_occupant_name", length = 50)
    private String currentOccupantName;

    /** current_unit_id */
    @Column(name = "current_unit_id")
    private Long currentUnitId;

    /** asset_ids */
    @Column(name = "asset_ids", columnDefinition = "text")
    private String assetIds;

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
