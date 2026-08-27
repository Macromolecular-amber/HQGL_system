package com.logistics.service.cl.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.cl.CostAuditRequest;
import com.logistics.dto.cl.CostPageQuery;
import com.logistics.dto.cl.CostSaveRequest;
import com.logistics.dto.cl.CostVO;
import com.logistics.dto.cl.VehicleCostSummaryVO;
import com.logistics.entity.ClCostDetail;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.SysUser;
import com.logistics.repository.ClCostDetailRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.cl.ClCostService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 费用核算与单车台账服务实现
 */
@Service
@RequiredArgsConstructor
public class ClCostServiceImpl implements ClCostService {

    /** 审批状态：待审批 */
    private static final String STATUS_PENDING = "PENDING";
    /** 审批状态：已通过 */
    private static final String STATUS_APPROVED = "APPROVED";
    /** 审批状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 审批结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 审批结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";
    /** 费用类型：加油 */
    private static final String TYPE_FUEL = "FUEL";
    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;
    /** 费用类型中文名 */
    private static final Map<String, String> COST_TYPE_LABEL_MAP = new HashMap<>();
    /** 审批状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();
    /** 车型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();

    static {
        COST_TYPE_LABEL_MAP.put("FUEL", "加油");
        COST_TYPE_LABEL_MAP.put("REPAIR", "维修");
        COST_TYPE_LABEL_MAP.put("INSURANCE", "保险");
        COST_TYPE_LABEL_MAP.put("TOLL", "过路费");
        COST_TYPE_LABEL_MAP.put("ETC", "ETC");
        COST_TYPE_LABEL_MAP.put("PARKING", "停车");
        COST_TYPE_LABEL_MAP.put("OTHER", "其他");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "已通过");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        TYPE_LABEL_MAP.put("SEDAN", "轿车");
        TYPE_LABEL_MAP.put("SUV", "SUV");
        TYPE_LABEL_MAP.put("MPV", "MPV");
        TYPE_LABEL_MAP.put("BUS", "客车");
    }

    private final ClCostDetailRepository costDetailRepository;
    private final ClVehicleArchiveRepository vehicleArchiveRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public CostVO saveCost(CostSaveRequest request) {
        validateCostRequest(request);

        OffsetDateTime now = OffsetDateTime.now();
        ClCostDetail cost = new ClCostDetail();
        cost.setVehicleId(request.getVehicleId());
        cost.setCostType(request.getCostType());
        cost.setCostAmount(request.getCostAmount());
        cost.setCostTime(toOffsetDateTime(request.getCostTime()));
        cost.setCostDesc(request.getCostDesc());
        cost.setBizOrderNo(request.getBizOrderNo());
        cost.setApprovalStatus(STATUS_PENDING);
        cost.setCurrentMileage(request.getCurrentMileage());
        cost.setFuelQuantity(request.getFuelQuantity());
        cost.setAttachmentUrls(request.getAttachmentUrls() == null ? null
                : String.join(",", request.getAttachmentUrls()));
        cost.setCreateBy(resolveCurrentUserId());
        cost.setCreateTime(now);
        cost.setUpdateTime(now);
        cost.setIsDeleted(false);
        return toVO(costDetailRepository.save(cost));
    }

    /**
     * 费用登记/编辑公共校验
     */
    private void validateCostRequest(CostSaveRequest request) {
        if (request.getCostAmount() == null || request.getCostAmount().signum() < 0) {
            throw new BusinessException("费用金额不能小于0");
        }
        if (!COST_TYPE_LABEL_MAP.containsKey(request.getCostType())) {
            throw new BusinessException("费用类型无效");
        }
        // 加油类型必填里程与加油量
        if (TYPE_FUEL.equals(request.getCostType())
                && (request.getCurrentMileage() == null || request.getFuelQuantity() == null)) {
            throw new BusinessException("加油费用必须填写加油里程和加油量");
        }
    }

    @Override
    @Transactional
    public CostVO updateCost(Long id, CostSaveRequest request) {
        ClCostDetail cost = costDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用记录不存在"));
        if (!STATUS_PENDING.equals(cost.getApprovalStatus())) {
            throw new BusinessException("仅待审批状态的费用可编辑");
        }
        validateCostRequest(request);
        cost.setVehicleId(request.getVehicleId());
        cost.setCostType(request.getCostType());
        cost.setCostAmount(request.getCostAmount());
        cost.setCostTime(toOffsetDateTime(request.getCostTime()));
        cost.setCostDesc(request.getCostDesc());
        cost.setBizOrderNo(request.getBizOrderNo());
        cost.setCurrentMileage(request.getCurrentMileage());
        cost.setFuelQuantity(request.getFuelQuantity());
        cost.setAttachmentUrls(request.getAttachmentUrls() == null ? null
                : String.join(",", request.getAttachmentUrls()));
        cost.setUpdateTime(OffsetDateTime.now());
        return toVO(costDetailRepository.save(cost));
    }

    @Override
    @Transactional
    public void auditCost(CostAuditRequest request) {
        ClCostDetail cost = costDetailRepository.findById(request.getCostId())
                .orElseThrow(() -> new BusinessException("费用记录不存在"));
        if (!STATUS_PENDING.equals(cost.getApprovalStatus())) {
            throw new BusinessException("当前状态不可审批");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (AUDIT_PASS.equals(request.getAuditResult())) {
            cost.setApprovalStatus(STATUS_APPROVED);
            cost.setApprovalUserId(resolveCurrentUserId());
            cost.setApprovalTime(now);
        } else if (AUDIT_REJECT.equals(request.getAuditResult())) {
            cost.setApprovalStatus(STATUS_REJECTED);
            cost.setApprovalRemark(request.getAuditRemark());
            cost.setApprovalUserId(resolveCurrentUserId());
            cost.setApprovalTime(now);
        } else {
            throw new BusinessException("审批结果无效，只能为 PASS 或 REJECT");
        }
        cost.setUpdateTime(now);
        costDetailRepository.save(cost);
    }

    @Override
    public PageResult<CostVO> queryPage(CostPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<ClCostDetail> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (query.getVehicleId() != null) {
                predicates.add(cb.equal(root.get("vehicleId"), query.getVehicleId()));
            }
            if (StringUtils.hasText(query.getCostType())) {
                predicates.add(cb.equal(root.get("costType"), query.getCostType()));
            }
            if (StringUtils.hasText(query.getApprovalStatus())) {
                predicates.add(cb.equal(root.get("approvalStatus"), query.getApprovalStatus()));
            }
            if (query.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("costTime"),
                        query.getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("costTime"),
                        query.getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ClCostDetail> result = costDetailRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "costTime")));
        List<CostVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public CostVO getDetail(Long id) {
        ClCostDetail cost = costDetailRepository.findById(id)
                .orElseThrow(() -> new BusinessException("费用记录不存在"));
        return toVO(cost);
    }

    @Override
    public VehicleCostSummaryVO getVehicleSummary(Long vehicleId, String yearMonth) {
        LocalDate monthStart = parseMonthStart(yearMonth);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        List<ClCostDetail> records = costDetailRepository
                .findByVehicleIdAndApprovalStatusAndCostTimeBetweenOrderByCostTimeAsc(vehicleId,
                        STATUS_APPROVED, toOffsetDateTime(monthStart.atStartOfDay()),
                        toOffsetDateTime(monthEnd.atTime(LocalTime.MAX)));
        VehicleCostSummaryVO vo = buildSummary(vehicleId, monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")), records);
        vo.setAvgFuelConsumption(calculateFuelConsumption(vehicleId,
                monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX)));
        return vo;
    }

    @Override
    public List<VehicleCostSummaryVO> getAllVehicleSummary(String yearMonth) {
        LocalDate monthStart = parseMonthStart(yearMonth);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        List<ClCostDetail> records = costDetailRepository
                .findByApprovalStatusAndCostTimeBetween(STATUS_APPROVED,
                        toOffsetDateTime(monthStart.atStartOfDay()),
                        toOffsetDateTime(monthEnd.atTime(LocalTime.MAX)));
        String period = monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<Long, List<ClCostDetail>> grouped = records.stream()
                .collect(Collectors.groupingBy(ClCostDetail::getVehicleId, LinkedHashMap::new, Collectors.toList()));
        List<VehicleCostSummaryVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<ClCostDetail>> entry : grouped.entrySet()) {
            VehicleCostSummaryVO vo = buildSummary(entry.getKey(), period, entry.getValue());
            vo.setAvgFuelConsumption(calculateFuelConsumption(entry.getKey(),
                    monthStart.atStartOfDay(), monthEnd.atTime(LocalTime.MAX)));
            result.add(vo);
        }
        return result;
    }

    @Override
    public BigDecimal calculateFuelConsumption(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        List<ClCostDetail> fuelRecords = costDetailRepository
                .findByVehicleIdAndCostTypeAndApprovalStatusAndCostTimeBetweenOrderByCostTimeAsc(vehicleId,
                        TYPE_FUEL, STATUS_APPROVED, toOffsetDateTime(start), toOffsetDateTime(end));
        if (fuelRecords.size() < 2) {
            return null;
        }
        // 总加油量
        BigDecimal totalFuel = fuelRecords.stream()
                .map(ClCostDetail::getFuelQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        // 总行驶里程：最后一条记录里程 - 第一条记录里程
        ClCostDetail first = fuelRecords.get(0);
        ClCostDetail last = fuelRecords.get(fuelRecords.size() - 1);
        if (first.getCurrentMileage() == null || last.getCurrentMileage() == null) {
            return null;
        }
        BigDecimal totalMileage = last.getCurrentMileage().subtract(first.getCurrentMileage());
        if (totalMileage.signum() <= 0) {
            return null;
        }
        return totalFuel.divide(totalMileage, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    /**
     * 按费用类型分组汇总车辆月度费用
     */
    private VehicleCostSummaryVO buildSummary(Long vehicleId, String period, List<ClCostDetail> records) {
        VehicleCostSummaryVO vo = new VehicleCostSummaryVO();
        vo.setVehicleId(vehicleId);
        vo.setPeriod(period);
        vo.setPlateNumber(vehicleArchiveRepository.findById(vehicleId)
                .map(ClVehicleArchive::getPlateNumber).orElse(null));
        BigDecimal fuel = sumByType(records, "FUEL");
        BigDecimal repair = sumByType(records, "REPAIR");
        BigDecimal insurance = sumByType(records, "INSURANCE");
        BigDecimal toll = sumByType(records, "TOLL");
        BigDecimal etc = sumByType(records, "ETC");
        BigDecimal parking = sumByType(records, "PARKING");
        BigDecimal other = sumByType(records, "OTHER");
        vo.setTotalFuelCost(fuel);
        vo.setTotalRepairCost(repair);
        vo.setTotalInsuranceCost(insurance);
        vo.setTotalTollCost(toll);
        vo.setTotalEtcCost(etc);
        vo.setTotalParkingCost(parking);
        vo.setTotalOtherCost(other);
        vo.setTotalCost(fuel.add(repair).add(insurance).add(toll).add(etc).add(parking).add(other));
        return vo;
    }

    private BigDecimal sumByType(List<ClCostDetail> records, String type) {
        return records.stream()
                .filter(r -> type.equals(r.getCostType()) && r.getCostAmount() != null)
                .map(ClCostDetail::getCostAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 费用转 VO，补充车牌号与中文名
     */
    private CostVO toVO(ClCostDetail cost) {
        CostVO vo = new CostVO();
        BeanUtils.copyProperties(cost, vo);
        vo.setPlateNumber(vehicleArchiveRepository.findById(cost.getVehicleId())
                .map(ClVehicleArchive::getPlateNumber).orElse(null));
        vo.setCostTypeLabel(COST_TYPE_LABEL_MAP.getOrDefault(cost.getCostType(), cost.getCostType()));
        vo.setApprovalStatusLabel(STATUS_LABEL_MAP.getOrDefault(cost.getApprovalStatus(), cost.getApprovalStatus()));
        vehicleArchiveRepository.findById(cost.getVehicleId()).ifPresent(v ->
                vo.setVehicleTypeLabel(TYPE_LABEL_MAP.getOrDefault(v.getVehicleType(), v.getVehicleType())));
        return vo;
    }

    /**
     * 解析 yyyy-MM 为当月第一天
     */
    private LocalDate parseMonthStart(String yearMonth) {
        if (!StringUtils.hasText(yearMonth) || !yearMonth.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new BusinessException("年月格式应为 yyyy-MM");
        }
        return LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
}
