package com.logistics.service.cl.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.cl.ApplyAuditRequest;
import com.logistics.dto.cl.ApplyPageQuery;
import com.logistics.dto.cl.ApplyRequest;
import com.logistics.dto.cl.ApplyVO;
import com.logistics.dto.cl.VehicleSimpleVO;
import com.logistics.dto.cl.VehicleVO;
import com.logistics.entity.ClApplyOrder;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.ClApplyOrderRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageNotifier;
import com.logistics.service.cl.ClApplyService;
import com.logistics.service.cl.ClVehicleService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公务用车申请与审批服务实现
 */
@Service
@RequiredArgsConstructor
public class ClApplyServiceImpl implements ClApplyService {

    /** 申请状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 申请状态：已通过 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 申请状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 申请状态：已取消 */
    private static final String STATUS_CANCELLED = "CANCELLED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 审批结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 车型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();
    /** 状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        TYPE_LABEL_MAP.put("SEDAN", "轿车");
        TYPE_LABEL_MAP.put("SUV", "SUV");
        TYPE_LABEL_MAP.put("MPV", "MPV");
        TYPE_LABEL_MAP.put("BUS", "客车");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "已通过");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        STATUS_LABEL_MAP.put("CANCELLED", "已取消");
        STATUS_LABEL_MAP.put("DISPATCHED", "已派车");
    }

    private final ClApplyOrderRepository applyOrderRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUnitRepository sysUnitRepository;
    private final ClVehicleService clVehicleService;
    private final MessageNotifier messageNotifier;

    @Override
    @Transactional
    public ApplyVO apply(ApplyRequest request) {
        // 时间校验
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
        if (request.getPassengerCount() == null || request.getPassengerCount() < 1) {
            throw new BusinessException("乘车人数至少1人");
        }

        // 智能审批扩展点（PRD 4.2.4.2）：当前阶段所有申请均进入人工审批
        boolean autoApprove = shouldAutoApprove(request);

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
        ClApplyOrder order = new ClApplyOrder();
        order.setApplyNo(generateApplyNo());
        order.setApplicantId(user.getId());
        order.setApplicantName(user.getRealName());
        order.setApplicantUnitId(applicantUnitId);
        order.setApplicantPhone(user.getPhone());
        order.setPurpose(request.getPurpose());
        order.setDestination(request.getDestination());
        order.setStartTime(toOffsetDateTime(request.getStartTime()));
        order.setEndTime(toOffsetDateTime(request.getEndTime()));
        order.setPassengerCount(request.getPassengerCount());
        order.setRequiredVehicleType(request.getRequiredVehicleType());
        order.setRemark(request.getRemark());
        order.setApplyStatus(STATUS_PENDING);
        order.setAutoApprove(autoApprove);
        order.setCreateBy(user.getId());
        order.setIsDeleted(false);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        ClApplyOrder saved = applyOrderRepository.save(order);
        // 通知业务管理员/审批人审批
        messageNotifier.notifyRoles("您有新的用车申请待审批", "cl-apply", saved.getApplyNo(), "BIZ_ADMIN", "DIRECTOR");
        return toVO(saved);
    }

    @Override
    @Transactional
    public void audit(ApplyAuditRequest request) {
        ClApplyOrder order = applyOrderRepository.findById(request.getApplyId())
                .orElseThrow(() -> new BusinessException("申请不存在"));
        if (!STATUS_PENDING.equals(order.getApplyStatus())) {
            throw new BusinessException("当前状态不可审批");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String result = request.getAuditResult();
        if (AUDIT_PASS.equals(result)) {
            order.setApplyStatus(STATUS_APPROVED);
        } else if (AUDIT_REJECT.equals(result)) {
            order.setApplyStatus(STATUS_REJECTED);
            order.setRejectReason(request.getAuditRemark());
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        // 记录审批信息
        order.setAuditTime(now);
        order.setAuditRemark(request.getAuditRemark());
        order.setAuditUserName(currentUserName());
        order.setUpdateTime(now);

        applyOrderRepository.save(order);
        // 通知申请人审批结果
        String resultTitle = AUDIT_PASS.equals(result) ? "您的用车申请已审核通过" : "您的用车申请已驳回";
        messageNotifier.notifyUser(resultTitle, "cl-apply", order.getApplyNo(), order.getApplicantId());
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        ClApplyOrder order = applyOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        if (!STATUS_PENDING.equals(order.getApplyStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        order.setApplyStatus(STATUS_CANCELLED);
        order.setUpdateTime(OffsetDateTime.now());
        applyOrderRepository.save(order);
    }

    @Override
    public PageResult<ApplyVO> queryPage(ApplyPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<ClApplyOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getApplyNo())) {
                predicates.add(cb.like(root.get("applyNo"), "%" + query.getApplyNo().trim() + "%"));
            }
            if (StringUtils.hasText(query.getApplyStatus())) {
                predicates.add(cb.equal(root.get("applyStatus"), query.getApplyStatus()));
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

        Page<ClApplyOrder> result = applyOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<ApplyVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public ApplyVO getDetail(Long id) {
        ClApplyOrder order = applyOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请不存在"));
        return toVO(order);
    }

    /**
     * 生成申请编号：CL + 年月 + 4位序号，如 CL2026080001
     */
    private String generateApplyNo() {
        String prefix = "CL" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = applyOrderRepository.countByApplyNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 智能审批扩展点（PRD 4.2.4.2）：
     * 自动审批条件为目的地在本市三区范围内，当前阶段所有申请先走人工审批，后续可扩展配置。
     */
    private boolean shouldAutoApprove(ApplyRequest request) {
        return false;
    }

    /**
     * 申请转 VO，补充申请人/单位名称、车型与状态中文名、可调度车辆
     */
    private ApplyVO toVO(ClApplyOrder order) {
        ApplyVO vo = new ApplyVO();
        BeanUtils.copyProperties(order, vo);
        if (!StringUtils.hasText(vo.getApplicantName())) {
            vo.setApplicantName(resolveUserName(order.getApplicantId()));
        }
        vo.setApplicantUnitName(resolveUnitName(order.getApplicantUnitId()));
        vo.setVehicleTypeLabel(TYPE_LABEL_MAP.getOrDefault(order.getRequiredVehicleType(), order.getRequiredVehicleType()));
        vo.setStatusLabel(STATUS_LABEL_MAP.getOrDefault(order.getApplyStatus(), order.getApplyStatus()));
        // 可调度车辆：按所需车型过滤
        vo.setAvailableVehicles(clVehicleService.getAvailableVehicles().stream()
                .filter(v -> !StringUtils.hasText(order.getRequiredVehicleType())
                        || order.getRequiredVehicleType().equals(v.getVehicleType()))
                .map(this::toVehicleSimpleVO)
                .collect(Collectors.toList()));
        return vo;
    }

    private VehicleSimpleVO toVehicleSimpleVO(VehicleVO vehicle) {
        VehicleSimpleVO vo = new VehicleSimpleVO();
        vo.setId(vehicle.getId());
        vo.setPlateNumber(vehicle.getPlateNumber());
        vo.setBrandModel(vehicle.getBrandModel());
        vo.setVehicleType(vehicle.getVehicleType());
        vo.setVehicleTypeLabel(vehicle.getVehicleTypeLabel());
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
