package com.logistics.service.cl.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.cl.DispatchPageQuery;
import com.logistics.dto.cl.DispatchRequest;
import com.logistics.dto.cl.DispatchVO;
import com.logistics.dto.cl.ReturnRequest;
import com.logistics.entity.ClApplyOrder;
import com.logistics.entity.ClDispatchOrder;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.SysRole;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.entity.SysUserRole;
import com.logistics.repository.ClApplyOrderRepository;
import com.logistics.repository.ClDispatchOrderRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.SysRoleRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.repository.SysUserRoleRepository;
import com.logistics.service.cl.ClDispatchService;
import com.logistics.service.cl.ClTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
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
 * 公务用车车辆调度服务实现
 */
@Service
@RequiredArgsConstructor
public class ClDispatchServiceImpl implements ClDispatchService {

    /** 申请状态：已通过（可派单） */
    private static final String APPLY_APPROVED = "APPROVED";
    /** 申请状态：已派车 */
    private static final String APPLY_DISPATCHED = "DISPATCHED";
    /** 车辆状态：可用 */
    private static final String VEHICLE_AVAILABLE = "AVAILABLE";
    /** 车辆状态：出车中 */
    private static final String VEHICLE_ON_DUTY = "ON_DUTY";
    /** 调度状态：待出车 */
    private static final String STATUS_WAITING = "WAITING";
    /** 调度状态：执行中 */
    private static final String STATUS_ONGOING = "ONGOING";
    /** 调度状态：已归还 */
    private static final String STATUS_RETURNED = "RETURNED";
    /** 驾驶员角色编码 */
    private static final String ROLE_DRIVER = "DRIVER";
    /** 车型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();
    /** 调度状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        TYPE_LABEL_MAP.put("SEDAN", "轿车");
        TYPE_LABEL_MAP.put("SUV", "SUV");
        TYPE_LABEL_MAP.put("MPV", "MPV");
        TYPE_LABEL_MAP.put("BUS", "客车");
        STATUS_LABEL_MAP.put("WAITING", "待出车");
        STATUS_LABEL_MAP.put("ONGOING", "出车中");
        STATUS_LABEL_MAP.put("RETURNED", "已归还");
        STATUS_LABEL_MAP.put("CANCELLED", "已取消");
    }

    private final ClDispatchOrderRepository dispatchOrderRepository;
    private final ClApplyOrderRepository applyOrderRepository;
    private final ClVehicleArchiveRepository vehicleArchiveRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUnitRepository sysUnitRepository;
    private final ClTrackService clTrackService;

    @Override
    @Transactional
    public DispatchVO dispatch(DispatchRequest request) {
        // 校验申请：已通过
        ClApplyOrder apply = applyOrderRepository.findById(request.getApplyId())
                .orElseThrow(() -> new BusinessException("用车申请不存在"));
        if (!APPLY_APPROVED.equals(apply.getApplyStatus())) {
            throw new BusinessException("用车申请当前状态不可派单");
        }
        // 校验车辆：可用
        ClVehicleArchive vehicle = vehicleArchiveRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        if (!VEHICLE_AVAILABLE.equals(vehicle.getVehicleStatus())) {
            throw new BusinessException("车辆当前状态不可派单");
        }
        // 校验驾驶员：角色包含 DRIVER
        SysUser driver = sysUserRepository.findById(request.getDriverId())
                .orElseThrow(() -> new BusinessException("驾驶员不存在"));
        if (!isDriver(driver.getId())) {
            throw new BusinessException("所选人员不是驾驶员");
        }
        // 时间校验
        if (!request.getScheduledStart().isBefore(request.getScheduledEnd())) {
            throw new BusinessException("计划开始时间必须早于结束时间");
        }

        OffsetDateTime now = OffsetDateTime.now();
        ClDispatchOrder order = new ClDispatchOrder();
        order.setDispatchNo(generateDispatchNo());
        order.setApplyId(request.getApplyId());
        order.setVehicleId(request.getVehicleId());
        order.setPlateNumber(vehicle.getPlateNumber());
        order.setDriverId(request.getDriverId());
        order.setDriverName(driver.getRealName());
        order.setDriverPhone(driver.getPhone());
        order.setScheduledStart(toOffsetDateTime(request.getScheduledStart()));
        order.setScheduledEnd(toOffsetDateTime(request.getScheduledEnd()));
        order.setDispatchStatus(STATUS_WAITING);
        order.setRemark(request.getRemark());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(false);
        ClDispatchOrder saved = dispatchOrderRepository.save(order);

        // 更新车辆状态为出车中
        vehicle.setVehicleStatus(VEHICLE_ON_DUTY);
        vehicle.setUpdateTime(now);
        vehicleArchiveRepository.save(vehicle);
        // 更新申请状态为已派车
        apply.setApplyStatus(APPLY_DISPATCHED);
        apply.setUpdateTime(now);
        applyOrderRepository.save(apply);

        return toVO(saved);
    }

    @Override
    @Transactional
    public DispatchVO startTrip(Long dispatchId) {
        ClDispatchOrder order = dispatchOrderRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException("调度单不存在"));
        if (!STATUS_WAITING.equals(order.getDispatchStatus())) {
            throw new BusinessException("仅待出车状态的派单可开始出车");
        }
        OffsetDateTime now = OffsetDateTime.now();
        order.setDispatchStatus(STATUS_ONGOING);
        order.setActualStart(now);
        order.setUpdateTime(now);
        dispatchOrderRepository.save(order);
        // 状态变为出车中（ONGOING）后自动生成模拟轨迹
        clTrackService.simulateTrack(dispatchId);
        return toVO(order);
    }

    @Override
    @Transactional
    public void returnVehicle(ReturnRequest request) {
        ClDispatchOrder order = dispatchOrderRepository.findById(request.getDispatchId())
                .orElseThrow(() -> new BusinessException("调度单不存在"));
        if (!STATUS_WAITING.equals(order.getDispatchStatus())
                && !STATUS_ONGOING.equals(order.getDispatchStatus())) {
            throw new BusinessException("当前状态不可归还");
        }
        OffsetDateTime now = OffsetDateTime.now();
        order.setDispatchStatus(STATUS_RETURNED);
        order.setActualEnd(toOffsetDateTime(request.getActualEnd()));
        order.setActualMileage(request.getActualMileage());
        order.setRemark(request.getRemark());
        order.setUpdateTime(now);
        dispatchOrderRepository.save(order);

        // 车辆恢复可用
        vehicleArchiveRepository.findById(order.getVehicleId()).ifPresent(vehicle -> {
            vehicle.setVehicleStatus(VEHICLE_AVAILABLE);
            vehicle.setCurrentMileage(request.getActualMileage());
            vehicle.setUpdateTime(now);
            vehicleArchiveRepository.save(vehicle);
        });
    }

    @Override
    public PageResult<DispatchVO> queryPage(DispatchPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<ClDispatchOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getDispatchNo())) {
                predicates.add(cb.like(root.get("dispatchNo"), "%" + query.getDispatchNo().trim() + "%"));
            }
            if (StringUtils.hasText(query.getDispatchStatus())) {
                predicates.add(cb.equal(root.get("dispatchStatus"), query.getDispatchStatus()));
            }
            if (query.getVehicleId() != null) {
                predicates.add(cb.equal(root.get("vehicleId"), query.getVehicleId()));
            }
            if (query.getDriverId() != null) {
                predicates.add(cb.equal(root.get("driverId"), query.getDriverId()));
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

        Page<ClDispatchOrder> result = dispatchOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<DispatchVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public DispatchVO getDetail(Long id) {
        ClDispatchOrder order = dispatchOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("调度单不存在"));
        return toVO(order);
    }

    @Override
    public List<DispatchVO> getByApplyId(Long applyId) {
        return dispatchOrderRepository.findByApplyId(applyId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 生成派单编号：DP + 年月 + 4位序号，如 DP2026080001
     */
    private String generateDispatchNo() {
        String prefix = "DP" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = dispatchOrderRepository.countByDispatchNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 调度单转 VO，补充申请信息、车型与状态中文名
     */
    private DispatchVO toVO(ClDispatchOrder order) {
        DispatchVO vo = new DispatchVO();
        BeanUtils.copyProperties(order, vo);
        // 补充申请信息
        applyOrderRepository.findById(order.getApplyId()).ifPresent(apply -> {
            vo.setApplyNo(apply.getApplyNo());
            vo.setApplicantName(apply.getApplicantName());
            vo.setApplicantUnitName(resolveUnitName(apply.getApplicantUnitId()));
            vo.setPurpose(apply.getPurpose());
            vo.setDestination(apply.getDestination());
        });
        // 补充车型中文名
        vehicleArchiveRepository.findById(order.getVehicleId()).ifPresent(vehicle ->
                vo.setVehicleTypeLabel(TYPE_LABEL_MAP.getOrDefault(vehicle.getVehicleType(), vehicle.getVehicleType())));
        vo.setStatusLabel(STATUS_LABEL_MAP.getOrDefault(order.getDispatchStatus(), order.getDispatchStatus()));
        return vo;
    }

    /**
     * 判断用户是否具备驾驶员角色
     */
    private boolean isDriver(Long userId) {
        SysRole driverRole = sysRoleRepository.findByRoleCode(ROLE_DRIVER).orElse(null);
        if (driverRole == null) {
            return false;
        }
        return sysUserRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> driverRole.getId().equals(ur.getRoleId()));
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
    }
}
