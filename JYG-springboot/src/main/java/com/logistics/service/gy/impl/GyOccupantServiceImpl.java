package com.logistics.service.gy.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gy.CheckoutAcceptRequest;
import com.logistics.dto.gy.OccupantApplyRequest;
import com.logistics.dto.gy.OccupantAssignRequest;
import com.logistics.dto.gy.OccupantAuditRequest;
import com.logistics.dto.gy.OccupantPageQuery;
import com.logistics.dto.gy.OccupantVO;
import com.logistics.entity.GyOccupant;
import com.logistics.entity.GyRoom;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.GyOccupantRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageNotifier;
import com.logistics.service.gy.GyOccupantService;
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
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专家公寓与人才公寓管理服务实现
 */
@Service
@RequiredArgsConstructor
public class GyOccupantServiceImpl implements GyOccupantService {

    /** 入住人类型：专家 */
    private static final String TYPE_EXPERT = "EXPERT";
    /** 入住人类型：人才 */
    private static final String TYPE_TALENT = "TALENT";
    /** 分配方式：直接分配 */
    private static final String ASSIGN_DIRECT = "DIRECT";
    /** 分配方式：审批分配 */
    private static final String ASSIGN_APPROVAL = "APPROVAL";
    /** 入住状态：在住 */
    private static final String STATUS_ACTIVE = "ACTIVE";
    /** 入住状态：已退住 */
    private static final String STATUS_RESIGNED = "RESIGNED";
    /** 入住状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 入住状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 房间状态：空闲 */
    private static final String ROOM_STATUS_IDLE = "idle";
    /** 房间状态：已入住 */
    private static final String ROOM_STATUS_OCCUPIED = "occupied";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 入住状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();
    /** 分配方式中文名 */
    private static final Map<String, String> ASSIGN_LABEL_MAP = new HashMap<>();
    /** 公寓类型中文名 */
    private static final Map<String, String> ROOM_TYPE_LABEL_MAP = new HashMap<>();

    static {
        STATUS_LABEL_MAP.put("active", "在住");
        STATUS_LABEL_MAP.put("resigned", "已退住");
        STATUS_LABEL_MAP.put("pending", "待审批");
        STATUS_LABEL_MAP.put("rejected", "已驳回");
        ASSIGN_LABEL_MAP.put("direct", "直接分配");
        ASSIGN_LABEL_MAP.put("approval", "审批分配");
        ROOM_TYPE_LABEL_MAP.put("expert_apartment", "专家公寓");
        ROOM_TYPE_LABEL_MAP.put("talent_apartment", "人才公寓");
    }

    private final GyOccupantRepository occupantRepository;
    private final GyRoomRepository roomRepository;
    private final SysUnitRepository sysUnitRepository;
    private final SysUserRepository sysUserRepository;
    private final MessageNotifier messageNotifier;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OccupantVO assignDirectly(OccupantAssignRequest request) {
        // 校验房间：存在、空闲、专家公寓
        GyRoom room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException("房间不存在"));
        if (!ROOM_STATUS_IDLE.equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("房间当前状态不可分配");
        }
        if (!"expert_apartment".equalsIgnoreCase(room.getRoomType())) {
            throw new BusinessException("仅专家公寓房间支持直接分配");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GyOccupant occupant = new GyOccupant();
        occupant.setOccupantName(request.getOccupantName());
        occupant.setOccupantType(TYPE_EXPERT);
        occupant.setIdCard(request.getIdCard());
        occupant.setPhone(request.getPhone());
        occupant.setUnitId(request.getUnitId());
        occupant.setUnitName(resolveUnitName(request.getUnitId()));
        occupant.setPosition(request.getPosition());
        occupant.setRoomId(room.getId());
        occupant.setRoomNo(room.getRoomNo());
        occupant.setCheckinTime(toOffsetDateTime(request.getCheckinTime()));
        occupant.setExpectedLeaveTime(toOffsetDateTime(request.getExpectedLeaveTime()));
        occupant.setRentAmount(request.getRentAmount() == null ? new java.math.BigDecimal("0") : request.getRentAmount());
        occupant.setAssignMethod(ASSIGN_DIRECT);
        occupant.setOccupantStatus(STATUS_ACTIVE);
        occupant.setRemark(request.getRemark());
        occupant.setCreateBy(resolveCurrentUserId());
        occupant.setCreateTime(now);
        occupant.setUpdateTime(now);
        occupant.setIsDeleted(false);
        GyOccupant saved = occupantRepository.save(occupant);

        // 更新房间状态与当前入住人
        updateRoomOccupancy(room, saved, true);
        return toVO(saved);
    }

    @Override
    @Transactional
    public OccupantVO apply(OccupantApplyRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        GyOccupant occupant = new GyOccupant();
        occupant.setOccupantName(request.getOccupantName());
        occupant.setOccupantType(TYPE_TALENT);
        occupant.setIdCard(request.getIdCard());
        occupant.setPhone(request.getPhone());
        occupant.setUnitId(request.getUnitId());
        occupant.setUnitName(resolveUnitName(request.getUnitId()));
        occupant.setPosition(request.getPosition());
        occupant.setApplyReason(request.getApplyReason());
        occupant.setRemark(request.getRemark());
        // 申请阶段不分配房间
        occupant.setAssignMethod(ASSIGN_APPROVAL);
        occupant.setOccupantStatus(STATUS_PENDING);
        occupant.setCreateBy(resolveCurrentUserId());
        occupant.setCreateTime(now);
        occupant.setUpdateTime(now);
        occupant.setIsDeleted(false);
        GyOccupant saved = occupantRepository.save(occupant);
        // 通知公寓管理员审批
        messageNotifier.notifyRoles("您有新的公寓入住申请待审批", "gy-occupant", String.valueOf(saved.getId()), "BIZ_ADMIN", "WAREHOUSE");
        return toVO(saved);
    }

    @Override
    @Transactional
    public void audit(OccupantAuditRequest request) {
        GyOccupant occupant = occupantRepository.findById(request.getOccupantId())
                .orElseThrow(() -> new BusinessException("入住记录不存在"));
        if (!STATUS_PENDING.equalsIgnoreCase(occupant.getOccupantStatus())) {
            throw new BusinessException("当前状态不可审批");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAuditResult())) {
            occupant.setOccupantStatus(STATUS_ACTIVE);
            // 审批通过后分配人才公寓房间（选填）
            if (request.getRoomId() != null) {
                assignRoomToOccupant(occupant, request.getRoomId());
            }
        } else if ("REJECT".equals(request.getAuditResult())) {
            occupant.setOccupantStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        occupant.setAuditRemark(request.getAuditRemark());
        occupant.setAuditUserId(resolveCurrentUserId());
        occupant.setAuditUserName(currentUserName());
        occupant.setAuditTime(now);
        occupant.setUpdateTime(now);
        occupantRepository.save(occupant);
        // 通知申请人审批结果
        String resultTitle = AUDIT_PASS.equals(request.getAuditResult()) ? "您的公寓入住申请已审核通过" : "您的公寓入住申请已驳回";
        messageNotifier.notifyUser(resultTitle, "gy-occupant", String.valueOf(occupant.getId()), occupant.getCreateBy());
    }

    @Override
    public PageResult<OccupantVO> queryPage(OccupantPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GyOccupant> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getOccupantName())) {
                predicates.add(cb.like(root.get("occupantName"), "%" + query.getOccupantName().trim() + "%"));
            }
            if (query.getUnitId() != null) {
                predicates.add(cb.equal(root.get("unitId"), query.getUnitId()));
            }
            if (StringUtils.hasText(query.getOccupantStatus())) {
                predicates.add(cb.equal(cb.lower(root.get("occupantStatus")), query.getOccupantStatus().toLowerCase()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkinTime"),
                        query.getStartDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkinTime"),
                        query.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            // 按公寓类型过滤：有房间的按房间类型匹配；无房间的申请按入住人类型匹配（EXPERT/TALENT）
            if (StringUtils.hasText(query.getRoomType())) {
                Subquery<Long> sub = cq.subquery(Long.class);
                Root<GyRoom> roomRoot = sub.from(GyRoom.class);
                sub.select(roomRoot.get("id"))
                        .where(cb.equal(cb.lower(roomRoot.get("roomType")), query.getRoomType().toLowerCase()));
                String occupantType = "expert_apartment".equalsIgnoreCase(query.getRoomType())
                        ? TYPE_EXPERT : TYPE_TALENT;
                Predicate noRoomByType = cb.and(cb.isNull(root.get("roomId")),
                        cb.equal(root.get("occupantType"), occupantType));
                predicates.add(cb.or(root.get("roomId").in(sub), noRoomByType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GyOccupant> result = occupantRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<OccupantVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public OccupantVO getDetail(Long id) {
        GyOccupant occupant = occupantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("入住记录不存在"));
        return toVO(occupant);
    }

    @Override
    @Transactional
    public void checkout(Long id, LocalDateTime actualLeaveTime, String remark) {
        GyOccupant occupant = occupantRepository.findById(id)
                .orElseThrow(() -> new BusinessException("入住记录不存在"));
        if (!STATUS_ACTIVE.equalsIgnoreCase(occupant.getOccupantStatus())) {
            throw new BusinessException("仅入住中的记录可退住");
        }
        OffsetDateTime now = OffsetDateTime.now();
        occupant.setOccupantStatus(STATUS_RESIGNED);
        occupant.setActualLeaveTime(actualLeaveTime == null ? now : toOffsetDateTime(actualLeaveTime));
        occupant.setRemark(remark);
        occupant.setUpdateTime(now);
        occupantRepository.save(occupant);

        // 释放房间
        if (occupant.getRoomId() != null) {
            roomRepository.findById(occupant.getRoomId()).ifPresent(room -> updateRoomOccupancy(room, null, false));
        }
    }

    @Override
    public List<OccupantVO> getExpiringOccupants(int daysBefore) {
        int days = daysBefore <= 0 ? 7 : daysBefore;
        OffsetDateTime now = OffsetDateTime.now();
        List<GyOccupant> occupants = occupantRepository
                .findByOccupantStatusAndExpectedLeaveTimeBetween(STATUS_ACTIVE,
                        now, now.plusDays(days));
        return occupants.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptCheckout(CheckoutAcceptRequest request) {
        GyOccupant occupant = occupantRepository.findById(request.getOccupantId())
                .orElseThrow(() -> new BusinessException("入住记录不存在"));
        // 验收条件：已办理退住（实际退住时间已记录），状态为在住或已退住
        if (!STATUS_ACTIVE.equalsIgnoreCase(occupant.getOccupantStatus())
                && !STATUS_RESIGNED.equalsIgnoreCase(occupant.getOccupantStatus())) {
            throw new BusinessException("当前状态不可验收");
        }
        if (occupant.getActualLeaveTime() == null) {
            throw new BusinessException("请先办理退住登记");
        }
        OffsetDateTime now = OffsetDateTime.now();
        occupant.setCheckoutTime(toOffsetDateTime(request.getCheckoutTime()));
        occupant.setRoomCondition(request.getRoomCondition());
        occupant.setFacilityCheckResult(serializeFacilityCheck(request.getFacilityCheckResult()));
        occupant.setSettlementAmount(request.getSettlementAmount());
        occupant.setSettlementDetail(request.getSettlementDetail());
        occupant.setCheckoutPhotos(request.getCheckoutPhotos() == null ? null
                : String.join(",", request.getCheckoutPhotos()));
        occupant.setRemark(request.getRemark());
        // 记录验收人信息
        occupant.setAcceptUserId(resolveCurrentUserId());
        occupant.setAcceptUserName(currentUserName());
        occupant.setAcceptTime(now);
        occupant.setUpdateTime(now);
        occupantRepository.save(occupant);

        // 房间恢复空闲（幂等：仅当房间仍占用时释放）
        if (occupant.getRoomId() != null) {
            roomRepository.findById(occupant.getRoomId()).ifPresent(room -> {
                if (!ROOM_STATUS_IDLE.equalsIgnoreCase(room.getRoomStatus())
                        || room.getCurrentOccupantId() != null) {
                    room.setRoomStatus(ROOM_STATUS_IDLE);
                    room.setCurrentOccupantId(null);
                    room.setCurrentOccupantName(null);
                    room.setCurrentUnitId(null);
                    room.setUpdateTime(now);
                    roomRepository.save(room);
                }
            });
        }
    }

    /**
     * 资产核对结果 Map 序列化为 JSON 存储
     */
    private String serializeFacilityCheck(Map<String, String> facilityCheckResult) {
        if (facilityCheckResult == null || facilityCheckResult.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(facilityCheckResult);
        } catch (JsonProcessingException e) {
            throw new BusinessException("资产核对结果格式错误");
        }
    }

    /**
     * 为人才公寓入住人分配房间（审批通过后调用）
     */
    private void assignRoomToOccupant(GyOccupant occupant, Long roomId) {
        GyRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException("房间不存在"));
        if (!ROOM_STATUS_IDLE.equalsIgnoreCase(room.getRoomStatus())) {
            throw new BusinessException("房间当前状态不可分配");
        }
        if (!"talent_apartment".equalsIgnoreCase(room.getRoomType())) {
            throw new BusinessException("仅人才公寓房间可供分配");
        }
        occupant.setRoomId(room.getId());
        occupant.setRoomNo(room.getRoomNo());
        updateRoomOccupancy(room, occupant, true);
    }

    /**
     * 更新房间入住占用状态
     */
    private void updateRoomOccupancy(GyRoom room, GyOccupant occupant, boolean occupied) {
        if (occupied) {
            room.setRoomStatus(ROOM_STATUS_OCCUPIED);
            room.setCurrentOccupantId(occupant.getId());
            room.setCurrentOccupantName(occupant.getOccupantName());
            room.setCurrentUnitId(occupant.getUnitId());
        } else {
            room.setRoomStatus(ROOM_STATUS_IDLE);
            room.setCurrentOccupantId(null);
            room.setCurrentOccupantName(null);
            room.setCurrentUnitId(null);
        }
        room.setUpdateTime(OffsetDateTime.now());
        roomRepository.save(room);
    }

    /**
     * 入住记录转 VO，补充房间信息与中文名
     */
    private OccupantVO toVO(GyOccupant occupant) {
        OccupantVO vo = new OccupantVO();
        BeanUtils.copyProperties(occupant, vo);
        vo.setStatusLabel(label(STATUS_LABEL_MAP, occupant.getOccupantStatus()));
        vo.setAssignMethodLabel(label(ASSIGN_LABEL_MAP, occupant.getAssignMethod()));
        if (occupant.getRoomId() != null) {
            roomRepository.findById(occupant.getRoomId()).ifPresent(room -> {
                vo.setBuilding(room.getBuilding());
                vo.setRoomTypeLabel(label(ROOM_TYPE_LABEL_MAP, room.getRoomType()));
            });
        }
        if (!StringUtils.hasText(vo.getUnitName())) {
            vo.setUnitName(resolveUnitName(occupant.getUnitId()));
        }
        return vo;
    }

    private String label(Map<String, String> map, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return map.getOrDefault(value.toLowerCase(), value);
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
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
