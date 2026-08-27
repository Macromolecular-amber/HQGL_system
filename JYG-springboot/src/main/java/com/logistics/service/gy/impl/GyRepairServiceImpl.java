package com.logistics.service.gy.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gy.PartsItemDTO;
import com.logistics.dto.gy.RepairAcceptRequest;
import com.logistics.dto.gy.RepairApplyRequest;
import com.logistics.dto.gy.RepairAuditRequest;
import com.logistics.dto.gy.RepairPageQuery;
import com.logistics.dto.gy.RepairStartRequest;
import com.logistics.dto.gy.RepairVO;
import com.logistics.entity.GyRepairOrder;
import com.logistics.entity.GyRoom;
import com.logistics.entity.SysUser;
import com.logistics.repository.GyRepairOrderRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.gy.GyRepairService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公寓维修管理服务实现
 */
@Service
@RequiredArgsConstructor
public class GyRepairServiceImpl implements GyRepairService {

    /** 单据状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 单据状态：已批准 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 单据状态：维修中 */
    private static final String STATUS_REPAIRING = "REPAIRING";
    /** 单据状态：已完成 */
    private static final String STATUS_COMPLETED = "COMPLETED";
    /** 单据状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 维修类型中文名 */
    private static final Map<String, String> REPAIR_TYPE_LABEL_MAP = new HashMap<>();
    /** 费用承担中文名 */
    private static final Map<String, String> COST_TYPE_LABEL_MAP = new HashMap<>();
    /** 单据状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        REPAIR_TYPE_LABEL_MAP.put("MAINTENANCE", "保养");
        REPAIR_TYPE_LABEL_MAP.put("REPAIR", "维修");
        COST_TYPE_LABEL_MAP.put("UNIT", "单位承担");
        COST_TYPE_LABEL_MAP.put("PERSONAL", "个人自费");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "已批准");
        STATUS_LABEL_MAP.put("REPAIRING", "维修中");
        STATUS_LABEL_MAP.put("COMPLETED", "已完成");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        // 兼容历史预置数据
        STATUS_LABEL_MAP.put("DONE", "已完成");
    }

    private final GyRepairOrderRepository repairOrderRepository;
    private final GyRoomRepository roomRepository;
    private final SysUserRepository sysUserRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RepairVO apply(RepairApplyRequest request) {
        // 校验房间存在
        GyRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException("房间不存在"));
        if (!REPAIR_TYPE_LABEL_MAP.containsKey(request.getRepairType())) {
            throw new BusinessException("维修类型无效");
        }
        if (!COST_TYPE_LABEL_MAP.containsKey(request.getCostType())) {
            throw new BusinessException("费用承担方式无效");
        }
        if (StringUtils.hasText(request.getUrgencyLevel())
                && !Arrays.asList("HIGH", "MEDIUM", "LOW").contains(request.getUrgencyLevel())) {
            throw new BusinessException("紧急程度无效");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GyRepairOrder order = new GyRepairOrder();
        order.setRepairNo(generateRepairNo());
        order.setRoomId(request.getRoomId());
        order.setRoomNo(room.getRoomNo());
        order.setRepairType(request.getRepairType());
        order.setFaultDesc(request.getFaultDesc());
        order.setFaultPhotos(request.getFaultPhotos() == null ? null
                : String.join(",", request.getFaultPhotos()));
        order.setUrgencyLevel(request.getUrgencyLevel());
        order.setCostType(request.getCostType());
        order.setOrderStatus(STATUS_PENDING);
        // 申请人信息（从当前登录用户获取）
        order.setApplicantId(resolveCurrentUserId());
        order.setApplicantName(currentUserName());
        sysUserRepository.findById(resolveCurrentUserId()).ifPresent(u ->
                order.setApplicantPhone(u.getPhone()));
        order.setCreateBy(resolveCurrentUserId());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(false);
        return toVO(repairOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void audit(RepairAuditRequest request) {
        GyRepairOrder order = repairOrderRepository.findById(request.getRepairId())
                .orElseThrow(() -> new BusinessException("维修单不存在"));
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
        repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void startRepair(RepairStartRequest request) {
        GyRepairOrder order = repairOrderRepository.findById(request.getRepairId())
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        if (!STATUS_APPROVED.equals(order.getOrderStatus())) {
            throw new BusinessException("仅已批准的维修单可开始维修");
        }
        order.setOrderStatus(STATUS_REPAIRING);
        order.setEstimatedCost(request.getEstimatedCost());
        order.setRepairStart(OffsetDateTime.now());
        order.setUpdateTime(OffsetDateTime.now());
        repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void accept(RepairAcceptRequest request) {
        GyRepairOrder order = repairOrderRepository.findById(request.getRepairId())
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        if (!STATUS_REPAIRING.equals(order.getOrderStatus())) {
            throw new BusinessException("仅维修中的维修单可验收");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAcceptResult())) {
            order.setOrderStatus(STATUS_COMPLETED);
            order.setRepairEnd(now);
            order.setActualCost(request.getActualCost());
            order.setLaborCost(request.getLaborCost());
            order.setPartsDetail(serializeParts(request.getPartsDetail()));
            order.setAcceptResult(AUDIT_PASS);
            order.setAcceptUserId(resolveCurrentUserId());
            order.setAcceptTime(now);
            order.setAcceptRemark(request.getAcceptRemark());
        } else if ("FAIL".equals(request.getAcceptResult())) {
            order.setOrderStatus(STATUS_REJECTED);
            order.setAcceptResult("FAIL");
            order.setAcceptRemark(request.getAcceptRemark());
        } else {
            throw new BusinessException("验收结果无效，只能为 PASS 或 FAIL");
        }
        order.setUpdateTime(now);
        repairOrderRepository.save(order);
    }

    @Override
    public PageResult<RepairVO> queryPage(RepairPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GyRepairOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getRepairNo())) {
                predicates.add(cb.like(root.get("repairNo"), "%" + query.getRepairNo().trim() + "%"));
            }
            if (query.getRoomId() != null) {
                predicates.add(cb.equal(root.get("roomId"), query.getRoomId()));
            }
            if (StringUtils.hasText(query.getRepairType())) {
                predicates.add(cb.equal(root.get("repairType"), query.getRepairType()));
            }
            if (StringUtils.hasText(query.getCostType())) {
                predicates.add(cb.equal(root.get("costType"), query.getCostType()));
            }
            if (StringUtils.hasText(query.getOrderStatus())) {
                predicates.add(cb.equal(root.get("orderStatus"), query.getOrderStatus()));
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

        Page<GyRepairOrder> result = repairOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<RepairVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public RepairVO getDetail(Long id) {
        GyRepairOrder order = repairOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        return toVO(order);
    }

    @Override
    public List<RepairVO> getByRoomId(Long roomId) {
        return repairOrderRepository.findByRoomIdOrderByCreateTimeDesc(roomId)
                .stream().filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 生成维修单编号：GW + 年月 + 4位序号，如 GW2026080001
     */
    private String generateRepairNo() {
        String prefix = "GW" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = repairOrderRepository.countByRepairNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 维修单转 VO，补充房间信息、中文名与配件明细
     */
    private RepairVO toVO(GyRepairOrder order) {
        RepairVO vo = new RepairVO();
        BeanUtils.copyProperties(order, vo);
        // 配件明细 JSON 反序列化（与实体 String 类型不一致，手动处理）
        vo.setPartsDetail(parseParts(order.getPartsDetail()));
        roomRepository.findById(order.getRoomId()).ifPresent(room -> {
            vo.setBuilding(room.getBuilding());
            vo.setRoomType(room.getRoomType());
        });
        vo.setRepairTypeLabel(REPAIR_TYPE_LABEL_MAP.getOrDefault(order.getRepairType(), order.getRepairType()));
        vo.setCostTypeLabel(COST_TYPE_LABEL_MAP.getOrDefault(order.getCostType(), order.getCostType()));
        vo.setStatusLabel(STATUS_LABEL_MAP.getOrDefault(order.getOrderStatus(), order.getOrderStatus()));
        return vo;
    }

    /**
     * 配件明细序列化为 JSON 存储
     */
    private String serializeParts(List<PartsItemDTO> parts) {
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(parts);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 配件明细 JSON 反序列化
     */
    private List<PartsItemDTO> parseParts(String partsJson) {
        if (!StringUtils.hasText(partsJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(partsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PartsItemDTO.class));
        } catch (JsonProcessingException e) {
            return null;
        }
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
