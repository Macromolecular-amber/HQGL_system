package com.logistics.service.cl.impl;

import com.logistics.common.BusinessException;
import com.logistics.dto.cl.TrackPointDTO;
import com.logistics.dto.cl.TrackQuery;
import com.logistics.dto.cl.TrackVO;
import com.logistics.dto.cl.VehicleLocationVO;
import com.logistics.entity.ClApplyOrder;
import com.logistics.entity.ClDispatchOrder;
import com.logistics.entity.ClTrackPoint;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.repository.ClApplyOrderRepository;
import com.logistics.repository.ClDispatchOrderRepository;
import com.logistics.repository.ClTrackPointRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.service.cl.ClTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 车辆运行监管与轨迹监控服务实现
 */
@Service
@RequiredArgsConstructor
public class ClTrackServiceImpl implements ClTrackService {

    /** 起点坐标：嘉峪关市政府 */
    private static final double START_LNG = 98.2891;
    private static final double START_LAT = 39.7732;
    /** 派单状态：出车中 */
    private static final String STATUS_ONGOING = "ONGOING";
    /** 模拟终点经纬度范围（嘉峪关市周边） */
    private static final double MIN_LNG = 97.5;
    private static final double MAX_LNG = 99.0;
    private static final double MIN_LAT = 39.4;
    private static final double MAX_LAT = 40.3;

    private final ClTrackPointRepository trackPointRepository;
    private final ClDispatchOrderRepository dispatchOrderRepository;
    private final ClApplyOrderRepository applyOrderRepository;
    private final ClVehicleArchiveRepository vehicleArchiveRepository;

    @Override
    @Transactional
    public void simulateTrack(Long dispatchId) {
        ClDispatchOrder order = dispatchOrderRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException("派单不存在"));
        // 重新生成前清理该派单已有轨迹
        trackPointRepository.deleteByDispatchId(dispatchId);

        // 终点：根据目的地模拟地理编码
        String destination = applyOrderRepository.findById(order.getApplyId())
                .map(ClApplyOrder::getDestination).orElse(null);
        double[] end = geoCode(destination);

        Random random = new Random();
        int count = 20 + random.nextInt(11); // 20-30 个点
        LocalDateTime base = order.getScheduledStart().toLocalDateTime();
        List<ClTrackPoint> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double t = (i + 1) / (double) count;
            ClTrackPoint p = new ClTrackPoint();
            p.setDispatchId(dispatchId);
            p.setVehicleId(order.getVehicleId());
            p.setLng(round(START_LNG + (end[0] - START_LNG) * t + jitter(random), 7));
            p.setLat(round(START_LAT + (end[1] - START_LAT) * t + jitter(random), 7));
            p.setSpeed(BigDecimal.valueOf(30 + random.nextInt(51))); // 30-80 km/h
            p.setDirection(random.nextInt(360));
            // 每点间隔 2-3 分钟
            int interval = 2 + random.nextInt(2);
            p.setTrackTime(base.plusMinutes((long) interval * (i + 1))
                    .atZone(ZoneId.systemDefault()).toOffsetDateTime());
            points.add(p);
        }
        trackPointRepository.saveAll(points);
    }

    @Override
    public VehicleLocationVO getCurrentLocation(Long vehicleId) {
        ClTrackPoint latest = trackPointRepository.findFirstByVehicleIdOrderByTrackTimeDesc(vehicleId)
                .orElse(null);
        // 优先取该车辆当前出车中的派单
        ClDispatchOrder order = dispatchOrderRepository
                .findByVehicleIdAndDispatchStatus(vehicleId, STATUS_ONGOING)
                .stream().findFirst().orElse(null);
        return toLocationVO(order, latest);
    }

    @Override
    public List<VehicleLocationVO> getAllCurrentLocations() {
        List<ClDispatchOrder> ongoingOrders = dispatchOrderRepository.findByDispatchStatus(STATUS_ONGOING);
        List<VehicleLocationVO> result = new ArrayList<>();
        for (ClDispatchOrder order : ongoingOrders) {
            if (Boolean.TRUE.equals(order.getIsDeleted())) {
                continue;
            }
            ClTrackPoint latest = trackPointRepository
                    .findFirstByVehicleIdOrderByTrackTimeDesc(order.getVehicleId()).orElse(null);
            result.add(toLocationVO(order, latest));
        }
        return result;
    }

    @Override
    public TrackVO getTrackHistory(TrackQuery query) {
        if (!query.getStartTime().isBefore(query.getEndTime())) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        OffsetDateTime start = query.getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = query.getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        List<ClTrackPoint> points = trackPointRepository
                .findByVehicleIdAndTrackTimeBetweenOrderByTrackTimeAsc(query.getVehicleId(), start, end);

        TrackVO vo = new TrackVO();
        vo.setVehicleId(query.getVehicleId());
        vo.setPlateNumber(vehicleArchiveRepository.findById(query.getVehicleId())
                .map(ClVehicleArchive::getPlateNumber).orElse(null));
        vo.setPoints(points.stream().map(this::toPointDTO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional
    public void batchSaveTrackPoints(List<ClTrackPoint> points) {
        if (points == null || points.isEmpty()) {
            return;
        }
        trackPointRepository.saveAll(points);
    }

    /**
     * 组装车辆实时位置 VO（无轨迹点时坐标/速度等为 null）
     */
    private VehicleLocationVO toLocationVO(ClDispatchOrder order, ClTrackPoint latest) {
        VehicleLocationVO vo = new VehicleLocationVO();
        if (order != null) {
            vo.setVehicleId(order.getVehicleId());
            vo.setPlateNumber(order.getPlateNumber());
            vo.setDriverName(order.getDriverName());
            vo.setDispatchId(order.getId());
            vo.setStatus(order.getDispatchStatus());
            vo.setDestination(applyOrderRepository.findById(order.getApplyId())
                    .map(ClApplyOrder::getDestination).orElse(null));
            // 车型取自车辆档案
            vehicleArchiveRepository.findById(order.getVehicleId()).ifPresent(v ->
                    vo.setVehicleType(StringUtils.hasText(v.getVehicleTypeName())
                            ? v.getVehicleTypeName() : v.getVehicleType()));
        }
        if (latest != null) {
            vo.setLng(latest.getLng());
            vo.setLat(latest.getLat());
            vo.setSpeed(latest.getSpeed());
            vo.setDirection(latest.getDirection());
            vo.setLastUpdateTime(latest.getTrackTime().toLocalDateTime());
        }
        return vo;
    }

    /**
     * 轨迹点实体转 DTO
     */
    private TrackPointDTO toPointDTO(ClTrackPoint p) {
        TrackPointDTO dto = new TrackPointDTO();
        dto.setLng(p.getLng());
        dto.setLat(p.getLat());
        dto.setSpeed(p.getSpeed());
        dto.setDirection(p.getDirection());
        dto.setTrackTime(p.getTrackTime().toLocalDateTime());
        return dto;
    }

    /**
     * 模拟地理编码：根据目的地字符串确定性生成嘉峪关市周边的坐标。
     * 后续对接真实地理编码服务时可替换此方法。
     */
    private double[] geoCode(String destination) {
        if (!StringUtils.hasText(destination)) {
            return new double[]{(MIN_LNG + MAX_LNG) / 2, (MIN_LAT + MAX_LAT) / 2};
        }
        int hash = destination.hashCode();
        double lng = MIN_LNG + Math.abs(hash % 1500) / 1000.0;
        double lat = MIN_LAT + Math.abs((hash >> 8) % 900) / 1000.0;
        return new double[]{lng, lat};
    }

    /**
     * 轨迹点随机抖动，模拟真实行驶路径
     */
    private double jitter(Random random) {
        return (random.nextDouble() - 0.5) * 0.005;
    }

    private BigDecimal round(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }
}
