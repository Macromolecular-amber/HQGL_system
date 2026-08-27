package com.logistics.service.gc.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gc.DisposeApplyRequest;
import com.logistics.dto.gc.DisposeAuditRequest;
import com.logistics.dto.gc.DisposeIncomeRequest;
import com.logistics.dto.gc.DisposeOrderVO;
import com.logistics.dto.gc.DisposePageQuery;
import com.logistics.dto.gc.TransferApplyRequest;
import com.logistics.dto.gc.TransferAuditRequest;
import com.logistics.dto.gc.TransferDetailVO;
import com.logistics.dto.gc.TransferOrderVO;
import com.logistics.dto.gc.TransferPageQuery;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.GcTransferDetail;
import com.logistics.entity.GcTransferOrder;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.GcTransferDetailRepository;
import com.logistics.repository.GcTransferOrderRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.gc.GcTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 公物仓调剂共享服务实现
 */
@Service
@RequiredArgsConstructor
public class GcTransferServiceImpl implements GcTransferService {

    /** 调剂单状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 调剂单状态：已通过 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 调剂单状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 处置单状态：已完成 */
    private static final String STATUS_COMPLETED = "COMPLETED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 审批结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";
    /** 调剂类型：调剂 */
    private static final String TYPE_TRANSFER = "TRANSFER";
    /** 调剂类型：处置 */
    private static final String TYPE_DISPOSE = "DISPOSE";
    /** 资产状态：在仓 */
    private static final String ASSET_IN_STOCK = "IN_STOCK";
    /** 资产状态：待入仓 */
    private static final String ASSET_PENDING = "PENDING";
    /** 资产状态：已调剂 */
    private static final String ASSET_TRANSFERRED = "TRANSFERRED";
    /** 资产状态：已处置 */
    private static final String ASSET_DISPOSED = "DISPOSED";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 处置方式中文名 */
    private static final Map<String, String> DISPOSE_METHOD_LABEL_MAP = new HashMap<>();

    static {
        DISPOSE_METHOD_LABEL_MAP.put("AUCTION", "拍卖");
        DISPOSE_METHOD_LABEL_MAP.put("SCRAP", "报废");
        DISPOSE_METHOD_LABEL_MAP.put("DONATE", "捐赠");
    }

    private final GcTransferOrderRepository transferOrderRepository;
    private final GcTransferDetailRepository transferDetailRepository;
    private final GcAssetCardRepository gcAssetCardRepository;
    private final SysUnitRepository sysUnitRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public TransferOrderVO apply(TransferApplyRequest request) {
        // 校验资产：均在仓
        List<GcAssetCard> assets = gcAssetCardRepository.findAllById(request.getAssetIds());
        if (assets.size() != request.getAssetIds().size()) {
            throw new BusinessException("存在不存在的资产");
        }
        for (GcAssetCard asset : assets) {
            if (!ASSET_IN_STOCK.equals(asset.getAssetStatus())) {
                throw new BusinessException("资产[" + asset.getAssetName() + "]当前状态不可调剂");
            }
        }

        // 申请人信息（从当前登录用户获取）
        SysUser user = resolveCurrentUser();
        if (user == null) {
            user = sysUserRepository.findById(DEFAULT_USER_ID).orElse(null);
        }
        if (user == null) {
            throw new BusinessException("未找到申请人");
        }
        Long applicantUnitId = user.getUnitId() != null ? user.getUnitId() : DEFAULT_USER_ID;

        // 不能调给自己
        if (applicantUnitId.equals(request.getReceiveUnitId())) {
            throw new BusinessException("不能调剂给本单位");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GcTransferOrder order = new GcTransferOrder();
        order.setOrderNo(generateOrderNo());
        order.setTransferType(TYPE_TRANSFER);
        order.setApplicantUnitId(applicantUnitId);
        order.setReceiveUnitId(request.getReceiveUnitId());
        order.setApplyReason(request.getApplyReason());
        order.setRemark(request.getRemark());
        order.setAssetCount(assets.size());
        order.setTotalValue(assets.stream()
                .map(GcAssetCard::getOriginalValue)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setOrderStatus(STATUS_PENDING);
        order.setCreateBy(user.getId());
        order.setIsDeleted(false);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        GcTransferOrder saved = transferOrderRepository.save(order);

        // 保存明细（资产快照）
        List<GcTransferDetail> details = new ArrayList<>();
        for (GcAssetCard asset : assets) {
            GcTransferDetail detail = new GcTransferDetail();
            detail.setTransferOrderId(saved.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetCode(asset.getAssetCode());
            detail.setAssetName(asset.getAssetName());
            detail.setCreateTime(now);
            details.add(detail);
        }
        transferDetailRepository.saveAll(details);

        return toVO(saved);
    }

    @Override
    @Transactional
    public void audit(TransferAuditRequest request) {
        doAudit(request.getOrderId(), request.getAuditResult(), request.getAuditRemark());
    }

    @Override
    @Transactional
    public void auditDispose(DisposeAuditRequest request) {
        GcTransferOrder order = transferOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("处置单不存在"));
        if (!TYPE_DISPOSE.equals(order.getTransferType())) {
            throw new BusinessException("该单不是处置单");
        }
        doAudit(request.getOrderId(), request.getAuditResult(), request.getAuditRemark());
    }

    /**
     * 审批通用逻辑：按 transfer_type 区分调剂/处置的资产状态变更
     */
    @Transactional
    protected void doAudit(Long orderId, String auditResult, String auditRemark) {
        GcTransferOrder order = transferOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("调剂单不存在"));
        if (!STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("当前状态不可审批");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String result = auditResult;
        if (AUDIT_PASS.equals(result)) {
            order.setOrderStatus(STATUS_APPROVED);
            List<GcTransferDetail> details = transferDetailRepository.findByTransferOrderId(order.getId());
            List<Long> assetIds = details.stream().map(GcTransferDetail::getAssetId).collect(Collectors.toList());
            List<GcAssetCard> assets = gcAssetCardRepository.findAllById(assetIds);
            if (TYPE_TRANSFER.equals(order.getTransferType())) {
                // 调剂通过：更新资产归属单位与状态
                for (GcAssetCard asset : assets) {
                    asset.setOwnerUnitId(order.getReceiveUnitId());
                    asset.setAssetStatus(ASSET_TRANSFERRED);
                    asset.setUpdateTime(now);
                }
            } else if (TYPE_DISPOSE.equals(order.getTransferType())) {
                // 处置通过：资产状态更新为已处置
                for (GcAssetCard asset : assets) {
                    asset.setAssetStatus(ASSET_DISPOSED);
                    asset.setUpdateTime(now);
                }
            }
            gcAssetCardRepository.saveAll(assets);
        } else if (AUDIT_REJECT.equals(result)) {
            order.setOrderStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        // 记录审批信息
        order.setAuditTime(now);
        order.setAuditRemark(auditRemark);
        order.setAuditUserName(currentUserName());
        order.setUpdateTime(now);

        transferOrderRepository.save(order);
    }

    @Override
    public PageResult<TransferOrderVO> queryPage(TransferPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcTransferOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅调剂单 + 排除已删除
            predicates.add(cb.equal(root.get("transferType"), TYPE_TRANSFER));
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getOrderNo())) {
                predicates.add(cb.like(root.get("orderNo"), "%" + query.getOrderNo().trim() + "%"));
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("orderStatus"), query.getStatus()));
            }
            if (query.getApplicantUnitId() != null) {
                predicates.add(cb.equal(root.get("applicantUnitId"), query.getApplicantUnitId()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"),
                        query.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GcTransferOrder> result = transferOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<TransferOrderVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public TransferOrderVO getDetail(Long id) {
        GcTransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("调剂单不存在"));
        return toVO(order);
    }

    @Override
    @Transactional
    public DisposeOrderVO applyDispose(DisposeApplyRequest request) {
        // 校验处置方式
        if (!DISPOSE_METHOD_LABEL_MAP.containsKey(request.getDisposeMethod())) {
            throw new BusinessException("处置方式无效，只能为 AUCTION、SCRAP 或 DONATE");
        }
        // 校验资产：在仓或待入仓（已入仓即可处置）
        List<GcAssetCard> assets = gcAssetCardRepository.findAllById(request.getAssetIds());
        if (assets.size() != request.getAssetIds().size()) {
            throw new BusinessException("存在不存在的资产");
        }
        for (GcAssetCard asset : assets) {
            if (!ASSET_IN_STOCK.equals(asset.getAssetStatus())
                    && !ASSET_PENDING.equals(asset.getAssetStatus())) {
                throw new BusinessException("资产[" + asset.getAssetName() + "]当前状态不可处置");
            }
        }

        // 申请人信息（从当前登录用户获取）
        SysUser user = resolveCurrentUser();
        if (user == null) {
            user = sysUserRepository.findById(DEFAULT_USER_ID).orElse(null);
        }
        if (user == null) {
            throw new BusinessException("未找到申请人");
        }
        Long applicantUnitId = user.getUnitId() != null ? user.getUnitId() : DEFAULT_USER_ID;

        OffsetDateTime now = OffsetDateTime.now();
        GcTransferOrder order = new GcTransferOrder();
        order.setOrderNo(generateDisposeOrderNo());
        order.setTransferType(TYPE_DISPOSE);
        order.setApplicantUnitId(applicantUnitId);
        order.setApplyReason(request.getApplyReason());
        order.setRemark(request.getRemark());
        order.setDisposeMethod(request.getDisposeMethod());
        order.setAppraisalOrg(request.getAppraisalOrg());
        order.setAppraisalValue(request.getAppraisalValue());
        order.setAppraisalReportUrl(request.getAppraisalReportUrl());
        order.setAssetCount(assets.size());
        order.setTotalValue(assets.stream()
                .map(GcAssetCard::getOriginalValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order.setOrderStatus(STATUS_PENDING);
        order.setCreateBy(user.getId());
        order.setIsDeleted(false);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        GcTransferOrder saved = transferOrderRepository.save(order);

        // 保存明细（资产快照）
        List<GcTransferDetail> details = new ArrayList<>();
        for (GcAssetCard asset : assets) {
            GcTransferDetail detail = new GcTransferDetail();
            detail.setTransferOrderId(saved.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetCode(asset.getAssetCode());
            detail.setAssetName(asset.getAssetName());
            detail.setCreateTime(now);
            details.add(detail);
        }
        transferDetailRepository.saveAll(details);

        return toDisposeVO(saved);
    }

    @Override
    @Transactional
    public void recordDisposeIncome(DisposeIncomeRequest request) {
        GcTransferOrder order = transferOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("处置单不存在"));
        if (!TYPE_DISPOSE.equals(order.getTransferType())) {
            throw new BusinessException("该单不是处置单");
        }
        if (!STATUS_APPROVED.equals(order.getOrderStatus())) {
            throw new BusinessException("仅已通过的处置单可录入收益");
        }
        // 业务校验：收益/费用不能小于 0
        if (request.getIncomeAmount().compareTo(BigDecimal.ZERO) < 0
                || request.getExpenseAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("处置收入与费用不能小于0");
        }
        order.setIncomeAmount(request.getIncomeAmount());
        order.setExpenseAmount(request.getExpenseAmount());
        order.setRemark(request.getRemark());
        order.setOrderStatus(STATUS_COMPLETED);
        order.setExecTime(OffsetDateTime.now());
        order.setUpdateTime(OffsetDateTime.now());
        // net_profit 为数据库生成列，自动计算
        transferOrderRepository.save(order);
    }

    @Override
    public PageResult<DisposeOrderVO> queryDisposePage(DisposePageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcTransferOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅处置单 + 排除已删除
            predicates.add(cb.equal(root.get("transferType"), TYPE_DISPOSE));
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getOrderNo())) {
                predicates.add(cb.like(root.get("orderNo"), "%" + query.getOrderNo().trim() + "%"));
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("orderStatus"), query.getStatus()));
            }
            if (StringUtils.hasText(query.getDisposeMethod())) {
                predicates.add(cb.equal(root.get("disposeMethod"), query.getDisposeMethod()));
            }
            if (query.getApplicantUnitId() != null) {
                predicates.add(cb.equal(root.get("applicantUnitId"), query.getApplicantUnitId()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"),
                        query.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GcTransferOrder> result = transferOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<DisposeOrderVO> vos = result.getContent().stream().map(this::toDisposeVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public DisposeOrderVO getDisposeDetail(Long id) {
        GcTransferOrder order = transferOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("处置单不存在"));
        if (!TYPE_DISPOSE.equals(order.getTransferType())) {
            throw new BusinessException("该单不是处置单");
        }
        return toDisposeVO(order);
    }

    /**
     * 生成调剂单编号：TZ + 年月 + 4位序号，如 TZ2026080001
     */
    private String generateOrderNo() {
        return generateOrderNo("TZ");
    }

    /**
     * 生成处置单编号：CZ + 年月 + 4位序号，如 CZ2026080001
     */
    private String generateDisposeOrderNo() {
        return generateOrderNo("CZ");
    }

    private String generateOrderNo(String code) {
        String prefix = code + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = transferOrderRepository.countByOrderNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 调剂单转 VO，补充申请/接收单位名称与资产明细
     */
    private TransferOrderVO toVO(GcTransferOrder order) {
        TransferOrderVO vo = new TransferOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setApplicantUnitName(resolveUnitName(order.getApplicantUnitId()));
        vo.setReceiveUnitName(resolveUnitName(order.getReceiveUnitId()));
        List<GcTransferDetail> details = transferDetailRepository.findByTransferOrderId(order.getId());
        vo.setAssetCount(details.size());
        vo.setDetailList(details.stream().map(this::toDetailVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 处置单转 VO，补充申请单位名称、处置方式中文名与资产明细
     */
    private DisposeOrderVO toDisposeVO(GcTransferOrder order) {
        DisposeOrderVO vo = new DisposeOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setApplicantUnitName(resolveUnitName(order.getApplicantUnitId()));
        vo.setDisposeMethodLabel(DISPOSE_METHOD_LABEL_MAP.getOrDefault(order.getDisposeMethod(), order.getDisposeMethod()));
        List<GcTransferDetail> details = transferDetailRepository.findByTransferOrderId(order.getId());
        vo.setAssetCount(details.size());
        vo.setDetailList(details.stream().map(this::toDetailVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 明细转 VO，补充规格型号（来自资产卡片）
     */
    private TransferDetailVO toDetailVO(GcTransferDetail detail) {
        TransferDetailVO vo = new TransferDetailVO();
        vo.setAssetId(detail.getAssetId());
        vo.setAssetCode(detail.getAssetCode());
        vo.setAssetName(detail.getAssetName());
        vo.setSpecModel(gcAssetCardRepository.findById(detail.getAssetId())
                .map(GcAssetCard::getSpecModel).orElse(null));
        return vo;
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户
     */
    private SysUser resolveCurrentUser() {
        String username = currentUserName();
        if (StringUtils.hasText(username)) {
            return sysUserRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /**
     * 获取当前登录用户名（HTTP Basic 认证用户）
     */
    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))) {
                return authentication.getName();
            }
        }
        return null;
    }
}
