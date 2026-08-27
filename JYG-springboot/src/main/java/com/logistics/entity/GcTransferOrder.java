package com.logistics.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * gc_transfer_order 实体
 */
@Data
@Entity
@Table(name = "gc_transfer_order")
public class GcTransferOrder {

    /** id */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** order_no */
    @Column(name = "order_no", length = 50)
    private String orderNo;

    /** transfer_type */
    @Column(name = "transfer_type", length = 20)
    private String transferType;

    /** asset_count */
    @Column(name = "asset_count")
    private Integer assetCount;

    /** total_value */
    @Column(name = "total_value")
    private BigDecimal totalValue;

    /** applicant_unit_id */
    @Column(name = "applicant_unit_id")
    private Long applicantUnitId;

    /** receive_unit_id */
    @Column(name = "receive_unit_id")
    private Long receiveUnitId;

    /** apply_reason */
    @Column(name = "apply_reason", columnDefinition = "text")
    private String applyReason;

    /** remark */
    @Column(name = "remark", columnDefinition = "text")
    private String remark;

    /** dispose_method */
    @Column(name = "dispose_method", length = 30)
    private String disposeMethod;

    /** appraisal_org */
    @Column(name = "appraisal_org", length = 100)
    private String appraisalOrg;

    /** appraisal_value */
    @Column(name = "appraisal_value")
    private BigDecimal appraisalValue;

    /** appraisal_report_url */
    @Column(name = "appraisal_report_url", length = 255)
    private String appraisalReportUrl;

    /** appraisal_time */
    @Column(name = "appraisal_time")
    private OffsetDateTime appraisalTime;

    /** order_status */
    @Column(name = "order_status", length = 20)
    private String orderStatus;

    /** exec_time */
    @Column(name = "exec_time")
    private OffsetDateTime execTime;

    /** exec_result */
    @Column(name = "exec_result", columnDefinition = "text")
    private String execResult;

    /** income_amount */
    @Column(name = "income_amount")
    private BigDecimal incomeAmount;

    /** expense_amount */
    @Column(name = "expense_amount")
    private BigDecimal expenseAmount;

    /** net_profit（数据库生成列：income_amount - expense_amount，不可写入） */
    @Column(name = "net_profit", insertable = false, updatable = false)
    private BigDecimal netProfit;

    /** process_instance_id */
    @Column(name = "process_instance_id", length = 50)
    private String processInstanceId;

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

    /** audit_user_id */
    @Column(name = "audit_user_id")
    private Long auditUserId;

    /** audit_user_name */
    @Column(name = "audit_user_name", length = 50)
    private String auditUserName;

    /** audit_time */
    @Column(name = "audit_time")
    private OffsetDateTime auditTime;

    /** audit_remark */
    @Column(name = "audit_remark", columnDefinition = "text")
    private String auditRemark;

}
