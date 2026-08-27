package com.logistics.service.gc.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gc.BorrowApplyRequest;
import com.logistics.dto.gc.BorrowAuditRequest;
import com.logistics.dto.gc.BorrowDetailVO;
import com.logistics.dto.gc.BorrowOrderVO;
import com.logistics.dto.gc.BorrowPageQuery;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.GcBorrowDetail;
import com.logistics.entity.GcBorrowOrder;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.GcBorrowDetailRepository;
import com.logistics.repository.GcBorrowOrderRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageNotifier;
import com.logistics.service.gc.GcBorrowService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公物仓资产借用服务实现
 */
@Service
@RequiredArgsConstructor
public class GcBorrowServiceImpl implements GcBorrowService {

    /** 借用单状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 借用单状态：已通过 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 借用单状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 审批结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";
    /** 资产状态：在仓 */
    private static final String ASSET_STATUS_IN_STOCK = "IN_STOCK";
    /** 资产状态：已借用 */
    private static final String ASSET_STATUS_BORROWED = "BORROWED";
    /** 最大借用天数 */
    private static final int MAX_BORROW_DAYS = 180;

    private final GcBorrowOrderRepository borrowOrderRepository;
    private final GcBorrowDetailRepository borrowDetailRepository;
    private final GcAssetCardRepository gcAssetCardRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUnitRepository sysUnitRepository;
    private final MessageNotifier messageNotifier;

    @Override
    @Transactional
    public BorrowOrderVO apply(BorrowApplyRequest request) {
        // 时间校验：开始早于结束，且不超过 180 天
        if (!request.getBorrowStart().isBefore(request.getBorrowEnd())) {
            throw new BusinessException("借用开始时间必须早于结束时间");
        }
        long days = ChronoUnit.DAYS.between(request.getBorrowStart(), request.getBorrowEnd());
        if (days > MAX_BORROW_DAYS) {
            throw new BusinessException("借用期限不能超过" + MAX_BORROW_DAYS + "天");
        }
        // 资产存在性与状态校验
        List<GcAssetCard> assets = gcAssetCardRepository.findAllById(request.getAssetIds());
        if (assets.size() != request.getAssetIds().size()) {
            throw new BusinessException("存在不存在的资产");
        }
        for (GcAssetCard asset : assets) {
            if (!ASSET_STATUS_IN_STOCK.equals(asset.getAssetStatus())) {
                throw new BusinessException("资产[" + asset.getAssetName() + "]当前状态不可借用");
            }
        }

        // 申请人信息（从 sys_user / sys_unit 解析）
        String applicantName = resolveUserName(request.getApplicantId());
        String applicantUnitName = resolveUnitName(request.getApplicantUnitId());

        OffsetDateTime now = OffsetDateTime.now();
        GcBorrowOrder order = new GcBorrowOrder();
        order.setOrderNo(generateOrderNo());
        order.setApplicantId(request.getApplicantId());
        order.setApplicantName(applicantName);
        order.setApplicantUnitId(request.getApplicantUnitId());
        order.setApplicantUnitName(applicantUnitName);
        order.setBorrowStart(toOffsetDateTime(request.getBorrowStart()));
        order.setBorrowEnd(toOffsetDateTime(request.getBorrowEnd()));
        order.setBorrowReason(request.getBorrowReason());
        order.setRemark(request.getRemark());
        order.setOrderStatus(STATUS_PENDING);
        order.setExtensionCount(0);
        order.setMaxExtension(2);
        order.setIsDeleted(false);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        GcBorrowOrder saved = borrowOrderRepository.save(order);

        // 保存明细
        List<GcBorrowDetail> details = new ArrayList<>();
        for (GcAssetCard asset : assets) {
            GcBorrowDetail detail = new GcBorrowDetail();
            detail.setBorrowOrderId(saved.getId());
            detail.setAssetId(asset.getId());
            detail.setAssetCode(asset.getAssetCode());
            detail.setAssetName(asset.getAssetName());
            detail.setBorrowQuantity(1);
            detail.setDetailStatus(STATUS_PENDING);
            detail.setCreateTime(now);
            detail.setUpdateTime(now);
            details.add(detail);
        }
        borrowDetailRepository.saveAll(details);

        // 通知业务管理员审批
        messageNotifier.notifyRoles("您有新的资产借用申请待审批", "gc-borrow", saved.getOrderNo(), "BIZ_ADMIN");

        return toVO(saved);
    }

    @Override
    @Transactional
    public void audit(BorrowAuditRequest request) {
        GcBorrowOrder order = borrowOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("借用单不存在"));
        if (!STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("当前状态不可审批");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String result = request.getAuditResult();
        if (AUDIT_PASS.equals(result)) {
            order.setOrderStatus(STATUS_APPROVED);
            // 将关联资产状态更新为已借用
            List<GcBorrowDetail> details = borrowDetailRepository.findByBorrowOrderId(order.getId());
            List<Long> assetIds = details.stream().map(GcBorrowDetail::getAssetId).collect(Collectors.toList());
            List<GcAssetCard> assets = gcAssetCardRepository.findAllById(assetIds);
            for (GcAssetCard asset : assets) {
                asset.setAssetStatus(ASSET_STATUS_BORROWED);
                asset.setUpdateTime(now);
            }
            gcAssetCardRepository.saveAll(assets);
        } else if (AUDIT_REJECT.equals(result)) {
            order.setOrderStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        // 记录审批信息
        order.setAuditTime(now);
        order.setAuditRemark(request.getAuditRemark());
        order.setAuditUserName(currentUserName());
        order.setUpdateTime(now);

        borrowOrderRepository.save(order);
        // 通知申请人审批结果
        String resultTitle = AUDIT_PASS.equals(result) ? "您的资产借用申请已审核通过" : "您的资产借用申请已驳回";
        messageNotifier.notifyUser(resultTitle, "gc-borrow", order.getOrderNo(), order.getApplicantId());
    }

    @Override
    public PageResult<BorrowOrderVO> queryPage(BorrowPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcBorrowOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
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

        Page<GcBorrowOrder> result = borrowOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<BorrowOrderVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public BorrowOrderVO getDetail(Long id) {
        GcBorrowOrder order = borrowOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("借用单不存在"));
        return toVO(order);
    }

    @Override
    public List<BorrowDetailVO> getBorrowAssets(Long borrowOrderId) {
        if (!borrowOrderRepository.existsById(borrowOrderId)) {
            throw new BusinessException("借用单不存在");
        }
        return borrowDetailRepository.findByBorrowOrderId(borrowOrderId)
                .stream().map(this::toDetailVO).collect(Collectors.toList());
    }

    /**
     * 生成借用单编号：JY + 年月 + 4位序号，如 JY2026080001
     */
    private String generateOrderNo() {
        String prefix = "JY" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = borrowOrderRepository.countByOrderNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 借用单转 VO，补充申请人姓名、单位名称、明细
     */
    private BorrowOrderVO toVO(GcBorrowOrder order) {
        BorrowOrderVO vo = new BorrowOrderVO();
        BeanUtils.copyProperties(order, vo);
        if (!StringUtils.hasText(vo.getApplicantName())) {
            vo.setApplicantName(resolveUserName(order.getApplicantId()));
        }
        if (!StringUtils.hasText(vo.getApplicantUnitName())) {
            vo.setApplicantUnitName(resolveUnitName(order.getApplicantUnitId()));
        }
        List<GcBorrowDetail> details = borrowDetailRepository.findByBorrowOrderId(order.getId());
        vo.setAssetCount(details.size());
        vo.setDetailList(details.stream().map(this::toDetailVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 明细转 VO，补充规格型号（来自资产卡片）
     */
    private BorrowDetailVO toDetailVO(GcBorrowDetail detail) {
        BorrowDetailVO vo = new BorrowDetailVO();
        vo.setAssetId(detail.getAssetId());
        vo.setAssetCode(detail.getAssetCode());
        vo.setAssetName(detail.getAssetName());
        vo.setBorrowQuantity(detail.getBorrowQuantity());
        vo.setSpecModel(gcAssetCardRepository.findById(detail.getAssetId())
                .map(GcAssetCard::getSpecModel).orElse(null));
        return vo;
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        return sysUserRepository.findById(userId).map(SysUser::getRealName).orElse(null);
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
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
