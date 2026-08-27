package com.logistics.service.gy.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gy.CleaningAcceptRequest;
import com.logistics.dto.gy.CleaningApplyRequest;
import com.logistics.dto.gy.CleaningAssignRequest;
import com.logistics.dto.gy.CleaningAuditRequest;
import com.logistics.dto.gy.CleaningPageQuery;
import com.logistics.dto.gy.CleaningVO;
import com.logistics.entity.GyCleaningOrder;
import com.logistics.entity.GyRoom;
import com.logistics.entity.SysUser;
import com.logistics.repository.GyCleaningOrderRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.gy.GyCleaningService;
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
 * 公寓保洁服务管理服务实现
 */
@Service
@RequiredArgsConstructor
public class GyCleaningServiceImpl implements GyCleaningService {

    /** 单据状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 单据状态：已批准 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 单据状态：执行中 */
    private static final String STATUS_ONGOING = "ONGOING";
    /** 单据状态：已完成 */
    private static final String STATUS_COMPLETED = "COMPLETED";
    /** 单据状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 保洁类型中文名 */
    private static final Map<String, String> CLEANING_TYPE_LABEL_MAP = new HashMap<>();
    /** 单据状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        CLEANING_TYPE_LABEL_MAP.put("REGULAR", "定期");
        CLEANING_TYPE_LABEL_MAP.put("ON_DEMAND", "按需");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "已批准");
        STATUS_LABEL_MAP.put("ONGOING", "执行中");
        STATUS_LABEL_MAP.put("COMPLETED", "已完成");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        // 兼容历史预置数据
        STATUS_LABEL_MAP.put("DONE", "已完成");
    }

    private final GyCleaningOrderRepository cleaningOrderRepository;
    private final GyRoomRepository roomRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public CleaningVO apply(CleaningApplyRequest request) {
        // 校验房间存在
        GyRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException("房间不存在"));
        if (!CLEANING_TYPE_LABEL_MAP.containsKey(request.getCleaningType())) {
            throw new BusinessException("保洁类型无效");
        }
        if (StringUtils.hasText(request.getCleaningTimeSlot())
                && !"MORNING".equals(request.getCleaningTimeSlot())
                && !"AFTERNOON".equals(request.getCleaningTimeSlot())) {
            throw new BusinessException("保洁时段无效");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GyCleaningOrder order = new GyCleaningOrder();
        order.setCleaningNo(generateCleaningNo());
        order.setRoomId(request.getRoomId());
        order.setRoomNo(room.getRoomNo());
        order.setCleaningType(request.getCleaningType());
        // 保洁时间 = 日期 + 时段（MORNING 09:00 / AFTERNOON 14:00 / 默认 09:00）
        LocalTime slotTime = "AFTERNOON".equals(request.getCleaningTimeSlot()) ? LocalTime.of(14, 0) : LocalTime.of(9, 0);
        order.setCleaningTime(request.getCleaningDate().atTime(slotTime)
                .atZone(ZoneId.systemDefault()).toOffsetDateTime());
        order.setCleaningScope(request.getCleaningScope());
        order.setCleaningRequirement(request.getCleaningRequirement());
        order.setOrderStatus(STATUS_PENDING);
        // 申请人信息（从当前登录用户获取）
        order.setApplicantId(resolveCurrentUserId());
        order.setApplicantName(currentUserName());
        order.setCreateBy(resolveCurrentUserId());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(false);
        return toVO(cleaningOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void audit(CleaningAuditRequest request) {
        GyCleaningOrder order = cleaningOrderRepository.findById(request.getCleaningId())
                .orElseThrow(() -> new BusinessException("保洁单不存在"));
        if (!STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("当前状态不可审批");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAuditResult())) {
            order.setOrderStatus(STATUS_APPROVED);
        } else if ("REJECT".equals(request.getAuditResult())) {
            order.setOrderStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        order.setAuditRemark(request.getAuditRemark());
        order.setAuditUserId(resolveCurrentUserId());
        order.setAuditUserName(currentUserName());
        order.setAuditTime(now);
        order.setUpdateTime(now);
        cleaningOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void assign(CleaningAssignRequest request) {
        GyCleaningOrder order = cleaningOrderRepository.findById(request.getCleaningId())
                .orElseThrow(() -> new BusinessException("保洁单不存在"));
        if (!STATUS_APPROVED.equals(order.getOrderStatus())) {
            throw new BusinessException("仅已批准的保洁单可派单");
        }
        order.setOrderStatus(STATUS_ONGOING);
        order.setAssigneeId(request.getAssigneeId());
        order.setAssigneeName(sysUserRepository.findById(request.getAssigneeId())
                .map(SysUser::getRealName).orElse(null));
        order.setAssigneeCompany(request.getAssigneeCompany());
        order.setAssignTime(OffsetDateTime.now());
        order.setUpdateTime(OffsetDateTime.now());
        cleaningOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void accept(CleaningAcceptRequest request) {
        GyCleaningOrder order = cleaningOrderRepository.findById(request.getCleaningId())
                .orElseThrow(() -> new BusinessException("保洁单不存在"));
        if (!STATUS_ONGOING.equals(order.getOrderStatus())) {
            throw new BusinessException("仅执行中的保洁单可验收");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAcceptResult())) {
            order.setOrderStatus(STATUS_COMPLETED);
            order.setAcceptScore(request.getAcceptScore());
            order.setAcceptUserId(resolveCurrentUserId());
            order.setAcceptTime(now);
            order.setAcceptResult(AUDIT_PASS);
            order.setExecutePhotos(request.getExecutePhotos() == null ? null
                    : String.join(",", request.getExecutePhotos()));
        } else if ("FAIL".equals(request.getAcceptResult())) {
            order.setOrderStatus(STATUS_REJECTED);
            order.setAcceptResult("FAIL");
        } else {
            throw new BusinessException("验收结果无效，只能为 PASS 或 FAIL");
        }
        order.setAcceptRemark(request.getAcceptRemark());
        order.setUpdateTime(now);
        cleaningOrderRepository.save(order);
    }

    @Override
    public PageResult<CleaningVO> queryPage(CleaningPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GyCleaningOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getCleaningNo())) {
                predicates.add(cb.like(root.get("cleaningNo"), "%" + query.getCleaningNo().trim() + "%"));
            }
            if (query.getRoomId() != null) {
                predicates.add(cb.equal(root.get("roomId"), query.getRoomId()));
            }
            if (StringUtils.hasText(query.getCleaningType())) {
                predicates.add(cb.equal(root.get("cleaningType"), query.getCleaningType()));
            }
            if (StringUtils.hasText(query.getOrderStatus())) {
                predicates.add(cb.equal(root.get("orderStatus"), query.getOrderStatus()));
            }
            if (query.getCleaningDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("cleaningTime"),
                        query.getCleaningDateStart().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getCleaningDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("cleaningTime"),
                        query.getCleaningDateEnd().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GyCleaningOrder> result = cleaningOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<CleaningVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public CleaningVO getDetail(Long id) {
        GyCleaningOrder order = cleaningOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("保洁单不存在"));
        return toVO(order);
    }

    @Override
    public List<CleaningVO> getByRoomId(Long roomId) {
        return cleaningOrderRepository.findByRoomIdOrderByCreateTimeDesc(roomId)
                .stream().filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 生成保洁单编号：BJ + 年月 + 4位序号，如 BJ2026080001
     */
    private String generateCleaningNo() {
        String prefix = "BJ" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = cleaningOrderRepository.countByCleaningNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 保洁单转 VO，补充房间信息与中文名
     */
    private CleaningVO toVO(GyCleaningOrder order) {
        CleaningVO vo = new CleaningVO();
        BeanUtils.copyProperties(order, vo);
        vo.setCleaningTypeLabel(CLEANING_TYPE_LABEL_MAP.getOrDefault(order.getCleaningType(), order.getCleaningType()));
        vo.setStatusLabel(STATUS_LABEL_MAP.getOrDefault(order.getOrderStatus(), order.getOrderStatus()));
        roomRepository.findById(order.getRoomId()).ifPresent(room -> {
            vo.setBuilding(room.getBuilding());
            vo.setRoomType(room.getRoomType());
        });
        if (!StringUtils.hasText(vo.getAssigneeName()) && order.getAssigneeId() != null) {
            vo.setAssigneeName(sysUserRepository.findById(order.getAssigneeId())
                    .map(SysUser::getRealName).orElse(null));
        }
        return vo;
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户 ID，未匹配时兜底默认用户
     */
    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                SysUser user = sysUserRepository.findByUsername(authentication.getName()).orElse(null);
                if (user != null) {
                    return user.getId();
                }
            }
        }
        return DEFAULT_USER_ID;
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
