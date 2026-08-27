package com.logistics.service.cl.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.cl.VehiclePageQuery;
import com.logistics.dto.cl.VehicleSaveRequest;
import com.logistics.dto.cl.VehicleVO;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.SysUnit;
import com.logistics.repository.ClDispatchOrderRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.service.cl.ClVehicleService;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公务用车车辆档案服务实现
 */
@Service
@RequiredArgsConstructor
public class ClVehicleServiceImpl implements ClVehicleService {

    /** 车辆状态：可用 */
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    /** 车辆类型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();
    /** 车辆状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        TYPE_LABEL_MAP.put("SEDAN", "轿车");
        TYPE_LABEL_MAP.put("SUV", "SUV");
        TYPE_LABEL_MAP.put("MPV", "MPV");
        TYPE_LABEL_MAP.put("BUS", "客车");
        STATUS_LABEL_MAP.put("AVAILABLE", "可用");
        STATUS_LABEL_MAP.put("ON_DUTY", "出车中");
        STATUS_LABEL_MAP.put("REPAIRING", "维修中");
        STATUS_LABEL_MAP.put("MAINTAINING", "保养中");
        STATUS_LABEL_MAP.put("WAIT_SCRAP", "待报废");
        STATUS_LABEL_MAP.put("SCRAPPED", "已报废");
    }

    private final ClVehicleArchiveRepository vehicleArchiveRepository;
    private final ClDispatchOrderRepository dispatchOrderRepository;
    private final SysUnitRepository sysUnitRepository;

    @Override
    @Transactional
    public VehicleVO save(VehicleSaveRequest request) {
        // 业务校验
        if (request.getPurchasePrice() != null && request.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("购置价格不能小于0");
        }
        if (request.getSeatCount() == null || request.getSeatCount() <= 0) {
            throw new BusinessException("座位数必须大于0");
        }

        ClVehicleArchive vehicle;
        OffsetDateTime now = OffsetDateTime.now();
        if (request.getId() != null) {
            // 编辑
            vehicle = vehicleArchiveRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException("车辆不存在"));
        } else {
            // 新增
            vehicle = new ClVehicleArchive();
            vehicle.setVehicleStatus(STATUS_AVAILABLE);
            vehicle.setCurrentMileage(BigDecimal.ZERO);
            vehicle.setIsEstablishment(true);
            vehicle.setIsDeleted(false);
            vehicle.setCreateTime(now);
        }

        // 车牌号唯一性校验（编辑时排除自身）
        vehicleArchiveRepository.findByPlateNumber(request.getPlateNumber().trim())
                .filter(existing -> !existing.getIsDeleted()
                        && (request.getId() == null || !existing.getId().equals(request.getId())))
                .ifPresent(existing -> {
                    throw new BusinessException("车牌号已存在");
                });

        vehicle.setPlateNumber(request.getPlateNumber().trim());
        vehicle.setBrandModel(request.getBrandModel());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setVehicleTypeName(TYPE_LABEL_MAP.getOrDefault(request.getVehicleType(), request.getVehicleType()));
        vehicle.setEngineNo(request.getEngineNo());
        vehicle.setFrameNo(request.getFrameNo());
        vehicle.setSeatCount(request.getSeatCount());
        vehicle.setDisplacement(request.getDisplacement());
        vehicle.setColor(request.getColor());
        vehicle.setPurchaseDate(request.getPurchaseDate());
        vehicle.setPurchasePrice(request.getPurchasePrice());
        vehicle.setUnitId(request.getUnitId());
        vehicle.setUnitName(resolveUnitName(request.getUnitId()));
        vehicle.setRemark(request.getRemark());
        vehicle.setUpdateTime(now);

        ClVehicleArchive saved = vehicleArchiveRepository.save(vehicle);
        return toVO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ClVehicleArchive vehicle = vehicleArchiveRepository.findById(id)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        // 存在进行中的调度单不可删除
        boolean hasActiveDispatch = dispatchOrderRepository.existsByVehicleIdAndDispatchStatusIn(
                id, Arrays.asList("WAITING", "ONGOING"));
        if (hasActiveDispatch) {
            throw new BusinessException("该车辆存在进行中的调度单，不可删除");
        }
        vehicle.setIsDeleted(true);
        vehicle.setUpdateTime(OffsetDateTime.now());
        vehicleArchiveRepository.save(vehicle);
    }

    @Override
    public PageResult<VehicleVO> queryPage(VehiclePageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<ClVehicleArchive> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getPlateNumber())) {
                predicates.add(cb.like(root.get("plateNumber"), "%" + query.getPlateNumber().trim() + "%"));
            }
            if (StringUtils.hasText(query.getVehicleType())) {
                predicates.add(cb.equal(root.get("vehicleType"), query.getVehicleType()));
            }
            if (StringUtils.hasText(query.getVehicleStatus())) {
                predicates.add(cb.equal(root.get("vehicleStatus"), query.getVehicleStatus()));
            }
            if (query.getUnitId() != null) {
                predicates.add(cb.equal(root.get("unitId"), query.getUnitId()));
            }
            if (query.getPurchaseDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), query.getPurchaseDateStart()));
            }
            if (query.getPurchaseDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), query.getPurchaseDateEnd()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ClVehicleArchive> result = vehicleArchiveRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<VehicleVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public VehicleVO getDetail(Long id) {
        ClVehicleArchive vehicle = vehicleArchiveRepository.findById(id)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        return toVO(vehicle);
    }

    @Override
    public List<VehicleVO> getAvailableVehicles() {
        Specification<ClVehicleArchive> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            predicates.add(cb.equal(root.get("vehicleStatus"), STATUS_AVAILABLE));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return vehicleArchiveRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "plateNumber"))
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 车辆转 VO，补充单位名称与类型/状态中文名
     */
    private VehicleVO toVO(ClVehicleArchive vehicle) {
        VehicleVO vo = new VehicleVO();
        BeanUtils.copyProperties(vehicle, vo);
        if (!StringUtils.hasText(vo.getUnitName())) {
            vo.setUnitName(resolveUnitName(vehicle.getUnitId()));
        }
        vo.setVehicleTypeLabel(TYPE_LABEL_MAP.getOrDefault(vehicle.getVehicleType(), vehicle.getVehicleType()));
        vo.setVehicleStatusLabel(STATUS_LABEL_MAP.getOrDefault(vehicle.getVehicleStatus(), vehicle.getVehicleStatus()));
        return vo;
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
    }
}
