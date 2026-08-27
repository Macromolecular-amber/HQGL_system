package com.logistics.service.cl.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.cl.PartsItemDTO;
import com.logistics.dto.cl.RepairAcceptRequest;
import com.logistics.dto.cl.RepairApplyRequest;
import com.logistics.dto.cl.RepairAuditRequest;
import com.logistics.dto.cl.RepairPageQuery;
import com.logistics.dto.cl.RepairStartRequest;
import com.logistics.dto.cl.RepairVO;
import com.logistics.entity.ClCostDetail;
import com.logistics.entity.ClRepairOrder;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.SysUser;
import com.logistics.repository.ClCostDetailRepository;
import com.logistics.repository.ClRepairOrderRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.cl.ClRepairService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 维修保养管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ClRepairServiceImpl implements ClRepairService {

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
    /** 审批结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 费用类型：维修 */
    private static final String COST_TYPE_REPAIR = "REPAIR";
    /** 维修类型中文名 */
    private static final Map<String, String> REPAIR_TYPE_LABEL_MAP = new HashMap<>();
    /** 单据状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();
    /** 车型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();

    static {
        REPAIR_TYPE_LABEL_MAP.put("MAINTENANCE", "保养");
        REPAIR_TYPE_LABEL_MAP.put("REPAIR", "维修");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "已批准");
        STATUS_LABEL_MAP.put("REPAIRING", "维修中");
        STATUS_LABEL_MAP.put("COMPLETED", "已完成");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        // 兼容历史预置数据
        STATUS_LABEL_MAP.put("DONE", "已完成");
        TYPE_LABEL_MAP.put("SEDAN", "轿车");
        TYPE_LABEL_MAP.put("SUV", "SUV");
        TYPE_LABEL_MAP.put("MPV", "MPV");
        TYPE_LABEL_MAP.put("BUS", "客车");
    }

    private final ClRepairOrderRepository repairOrderRepository;
    private final ClVehicleArchiveRepository vehicleArchiveRepository;
    private final ClCostDetailRepository costDetailRepository;
    private final SysUserRepository sysUserRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RepairVO apply(RepairApplyRequest request) {
        // 校验车辆存在
        ClVehicleArchive vehicle = vehicleArchiveRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        if (!REPAIR_TYPE_LABEL_MAP.containsKey(request.getRepairType())) {
            throw new BusinessException("维修类型无效");
        }
        if (StringUtils.hasText(request.getUrgencyLevel())
                && !Arrays.asList("HIGH", "MEDIUM", "LOW").contains(request.getUrgencyLevel())) {
            throw new BusinessException("紧急程度无效");
        }

        OffsetDateTime now = OffsetDateTime.now();
        ClRepairOrder order = new ClRepairOrder();
        order.setRepairNo(generateRepairNo());
        order.setVehicleId(request.getVehicleId());
        order.setPlateNumber(vehicle.getPlateNumber());
        order.setRepairType(request.getRepairType());
        order.setFaultDesc(request.getFaultDesc());
        order.setFaultPhotos(request.getFaultPhotos() == null ? null
                : String.join(",", request.getFaultPhotos()));
        order.setUrgencyLevel(request.getUrgencyLevel());
        order.setRepairMileage(request.getRepairMileage());
        order.setOrderStatus(STATUS_PENDING);
        order.setCreateBy(resolveCurrentUserId());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(false);
        return toVO(repairOrderRepository.save(order));
    }

    @Override
    @Transactional
    public void audit(RepairAuditRequest request) {
        ClRepairOrder order = repairOrderRepository.findById(request.getRepairId())
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        if (!STATUS_PENDING.equals(order.getOrderStatus())) {
            throw new BusinessException("当前状态不可审批");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAuditResult())) {
            order.setOrderStatus(STATUS_APPROVED);
        } else if (AUDIT_REJECT.equals(request.getAuditResult())) {
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
        ClRepairOrder order = repairOrderRepository.findById(request.getRepairId())
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        if (!STATUS_APPROVED.equals(order.getOrderStatus())) {
            throw new BusinessException("仅已批准的维修单可开始维修");
        }
        order.setOrderStatus(STATUS_REPAIRING);
        order.setRepairShopName(request.getRepairShopName());
        order.setEstimatedCost(request.getEstimatedCost());
        order.setRepairStart(OffsetDateTime.now());
        order.setUpdateTime(OffsetDateTime.now());
        repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void accept(RepairAcceptRequest request) {
        ClRepairOrder order = repairOrderRepository.findById(request.getRepairId())
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
            // 联动生成费用记录（已审批，自动纳入单车台账）
            createRepairCost(order, request.getActualCost());
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

        Specification<ClRepairOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getRepairNo())) {
                predicates.add(cb.like(root.get("repairNo"), "%" + query.getRepairNo().trim() + "%"));
            }
            if (query.getVehicleId() != null) {
                predicates.add(cb.equal(root.get("vehicleId"), query.getVehicleId()));
            }
            if (StringUtils.hasText(query.getRepairType())) {
                predicates.add(cb.equal(root.get("repairType"), query.getRepairType()));
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

        Page<ClRepairOrder> result = repairOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<RepairVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public RepairVO getDetail(Long id) {
        ClRepairOrder order = repairOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("维修单不存在"));
        return toVO(order);
    }

    @Override
    public List<RepairVO> getByVehicleId(Long vehicleId) {
        return repairOrderRepository.findByVehicleIdOrderByCreateTimeDesc(vehicleId)
                .stream().filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                .map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 生成维修单编号：WX + 年月 + 4位序号，如 WX2026080001
     */
    private String generateRepairNo() {
        String prefix = "WX" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = repairOrderRepository.countByRepairNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 验收通过后生成费用记录（已审批，自动纳入单车台账）
     */
    private void createRepairCost(ClRepairOrder order, BigDecimal actualCost) {
        ClCostDetail cost = new ClCostDetail();
        cost.setVehicleId(order.getVehicleId());
        cost.setCostType(COST_TYPE_REPAIR);
        cost.setCostAmount(actualCost);
        cost.setCostTime(OffsetDateTime.now());
        cost.setCostDesc("维修费用：" + order.getRepairNo());
        cost.setBizOrderNo(order.getRepairNo());
        cost.setApprovalStatus("APPROVED");
        cost.setCreateBy(resolveCurrentUserId());
        cost.setCreateTime(OffsetDateTime.now());
        cost.setUpdateTime(OffsetDateTime.now());
        cost.setIsDeleted(false);
        costDetailRepository.save(cost);
    }

    /**
     * 维修单转 VO，补充车辆信息、中文名与配件明细
     */
    private RepairVO toVO(ClRepairOrder order) {
        RepairVO vo = new RepairVO();
        BeanUtils.copyProperties(order, vo);
        // 配件明细 JSON 反序列化（与实体 String 类型不一致，手动处理）
        vo.setPartsDetail(parseParts(order.getPartsDetail()));
        if (!StringUtils.hasText(vo.getPlateNumber())) {
            vo.setPlateNumber(vehicleArchiveRepository.findById(order.getVehicleId())
                    .map(ClVehicleArchive::getPlateNumber).orElse(null));
        }
        vehicleArchiveRepository.findById(order.getVehicleId()).ifPresent(v ->
                vo.setVehicleTypeLabel(TYPE_LABEL_MAP.getOrDefault(v.getVehicleType(), v.getVehicleType())));
        vo.setRepairTypeLabel(REPAIR_TYPE_LABEL_MAP.getOrDefault(order.getRepairType(), order.getRepairType()));
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
