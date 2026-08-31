package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.dto.dashboard.DashboardStatisticsVO;
import com.logistics.dto.dashboard.LeadershipDashboardVO;
import com.logistics.dto.dashboard.MessageVO;
import com.logistics.dto.dashboard.TodoVO;
import com.logistics.dto.dashboard.TrendVO;
import com.logistics.entity.ClApplyOrder;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.GcBorrowOrder;
import com.logistics.entity.GcReturnOrder;
import com.logistics.entity.GcTransferOrder;
import com.logistics.entity.ClRepairOrder;
import com.logistics.entity.GyCleaningOrder;
import com.logistics.entity.GyRepairOrder;
import com.logistics.entity.GyOccupant;
import com.logistics.entity.GyRoom;
import com.logistics.entity.StMealReservation;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.repository.ClApplyOrderRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.GcBorrowOrderRepository;
import com.logistics.repository.GcReturnOrderRepository;
import com.logistics.repository.GcTransferOrderRepository;
import com.logistics.repository.ClRepairOrderRepository;
import com.logistics.repository.GyCleaningOrderRepository;
import com.logistics.repository.GyRepairOrderRepository;
import com.logistics.repository.GyOccupantRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.repository.StMealReservationRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.service.DashboardService;
import com.logistics.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页数据聚合服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** 资产状态：在仓 */
    private static final String ASSET_IN_STOCK = "IN_STOCK";

    /** 资产状态：已借用 */
    private static final String ASSET_BORROWED = "已借用";

    /** 用车申请状态：待审批 */
    private static final String APPLY_PENDING = "PENDING";

    /** 车辆状态：出车中 */
    private static final String VEHICLE_ON_DUTY = "ON_DUTY";

    /** 房间状态：空闲 */
    private static final String ROOM_IDLE = "IDLE";

    /** 借用单状态：待审批 */
    private static final String BORROW_PENDING = "PENDING";

    /** 入住状态：待审批 */
    private static final String OCCUPANT_PENDING = "PENDING";

    /** 采购完成状态 */
    private static final String PURCHASE_COMPLETED = "COMPLETED";
    private static final String PURCHASE_RECEIVED = "RECEIVED";

    /** 趋势天数 */
    private static final int TREND_DAYS = 7;

    /** 日期格式 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d");

    private final GcAssetCardRepository assetCardRepository;
    private final ClApplyOrderRepository applyOrderRepository;
    private final ClVehicleArchiveRepository vehicleArchiveRepository;
    private final GyRoomRepository roomRepository;
    private final StMealReservationRepository mealReservationRepository;
    private final StPurchaseOrderRepository purchaseOrderRepository;
    private final GcBorrowOrderRepository borrowOrderRepository;
    private final GcReturnOrderRepository returnOrderRepository;
    private final GyOccupantRepository occupantRepository;
    private final GcTransferOrderRepository transferOrderRepository;
    private final ClRepairOrderRepository clRepairOrderRepository;
    private final GyRepairOrderRepository gyRepairOrderRepository;
    private final GyCleaningOrderRepository gyCleaningOrderRepository;
    private final MessageService messageService;

    @Override
    public DashboardStatisticsVO getStatistics(Long userId) {
        DashboardStatisticsVO vo = new DashboardStatisticsVO();
        // 资产
        vo.setTotalAssets(count(assetCardRepository, null));
        vo.setInStockAssets(count(assetCardRepository, (root, cq, cb) ->
                cb.equal(root.get("assetStatus"), ASSET_IN_STOCK)));
        vo.setBorrowedAssets(count(assetCardRepository, (root, cq, cb) ->
                cb.equal(root.get("assetStatus"), ASSET_BORROWED)));
        // 用车审批
        vo.setPendingApprovals(count(applyOrderRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("applyStatus")), APPLY_PENDING)));
        // 车辆
        vo.setTotalVehicles(count(vehicleArchiveRepository, null));
        vo.setOnDutyVehicles(count(vehicleArchiveRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("vehicleStatus")), VEHICLE_ON_DUTY)));
        // 房间
        vo.setTotalRooms(count(roomRepository, null));
        vo.setIdleRooms(count(roomRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("roomStatus")), ROOM_IDLE)));
        // 今日预约人数
        vo.setTodayReservations(mealReservationRepository.findByMealDateAndIsCancelled(LocalDate.now(), false)
                .stream().mapToInt(r -> r.getMealCount() == null ? 0 : r.getMealCount()).sum());
        // 本月采购金额（完成/已收货订单）
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        OffsetDateTime start = monthStart.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        List<StPurchaseOrder> monthOrders = purchaseOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.or(
                    cb.equal(cb.upper(root.get("orderStatus")), PURCHASE_COMPLETED),
                    cb.equal(cb.upper(root.get("orderStatus")), PURCHASE_RECEIVED)));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start));
            predicates.add(cb.lessThan(root.get("createTime"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        vo.setMonthlyPurchaseAmount(monthOrders.stream()
                .map(StPurchaseOrder::getTotalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return vo;
    }

    @Override
    public List<TodoVO> getTodos(Long userId) {
        List<TodoVO> todos = new ArrayList<>();
        // 公物仓-借用审批
        for (GcBorrowOrder o : borrowOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), BORROW_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "借用申请 " + o.getOrderNo() + nameSuffix(o.getApplicantName()), "公物仓", o.getCreateTime());
        }
        // 公物仓-调剂审批
        for (GcTransferOrder o : transferOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "调剂申请 " + o.getOrderNo(), "公物仓", o.getCreateTime());
        }
        // 用车-用车申请审批
        for (ClApplyOrder o : applyOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("applyStatus")), APPLY_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "用车申请 " + o.getApplyNo() + nameSuffix(o.getApplicantName()), "用车", o.getCreateTime());
        }
        // 用车-维修保养审批
        for (ClRepairOrder o : clRepairOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "车辆维修 " + o.getRepairNo() + nameSuffix(o.getPlateNumber()), "用车", o.getCreateTime());
        }
        // 公寓-入住申请
        for (GyOccupant o : occupantRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("occupantStatus")), OCCUPANT_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "公寓入住申请" + nameSuffix(o.getOccupantName()), "公寓", o.getCreateTime());
        }
        // 公寓-维修审批
        for (GyRepairOrder o : gyRepairOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "公寓维修 " + o.getRepairNo() + nameSuffix(o.getRoomNo()), "公寓", o.getCreateTime());
        }
        // 公寓-保洁审批
        for (GyCleaningOrder o : gyCleaningOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "保洁申请 " + o.getCleaningNo() + nameSuffix(o.getRoomNo()), "公寓", o.getCreateTime());
        }
        // 食堂-采购申请
        for (StPurchaseOrder o : purchaseOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
            return cb.and(predicates.toArray(new Predicate[0]));
        })) {
            addTodo(todos, o.getId(), "采购申请 " + o.getOrderNo(), "食堂", o.getCreateTime());
        }
        // 按时间倒序
        todos.sort(Comparator.comparing(TodoVO::getTime, Comparator.nullsLast(String::compareTo)).reversed());
        return todos;
    }

    private void addTodo(List<TodoVO> todos, Long id, String title, String module, OffsetDateTime time) {
        TodoVO todo = new TodoVO();
        todo.setId(id);
        todo.setTitle(title);
        todo.setModule(module);
        todo.setTime(time == null ? null : formatTime(time));
        todo.setStatus("待审批");
        todos.add(todo);
    }

    private String nameSuffix(String name) {
        return StringUtils.hasText(name) ? "（" + name + "）" : "";
    }

    @Override
    public List<MessageVO> getMessages(Long userId) {
        return messageService.getLatest(userId, 5).stream().map(m -> {
            MessageVO vo = new MessageVO();
            vo.setId(m.getId());
            vo.setTitle(m.getTitle());
            vo.setType(m.getMessageTypeLabel());
            vo.setTime(m.getCreateTime() == null ? null : formatTime(m.getCreateTime()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TrendVO getTrend() {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(TREND_DAYS - 1);
        OffsetDateTime startTime = start.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();

        // 近7天日期序列
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            dates.add(start.plusDays(i).format(DATE_FORMATTER));
        }

        // 入仓趋势（gc_asset_card 按 create_time 分组）
        List<GcAssetCard> assets = assetCardRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        Map<LocalDate, Long> inStockMap = groupByDate(assets, GcAssetCard::getCreateTime);

        // 借用趋势（gc_borrow_order 按 create_time 分组）
        List<GcBorrowOrder> borrows = borrowOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startTime));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        Map<LocalDate, Long> borrowMap = groupByDate(borrows, GcBorrowOrder::getCreateTime);

        TrendVO vo = new TrendVO();
        vo.setDates(dates);
        List<Long> inStockList = new ArrayList<>();
        List<Long> borrowList = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            inStockList.add(inStockMap.getOrDefault(start.plusDays(i), 0L));
            borrowList.add(borrowMap.getOrDefault(start.plusDays(i), 0L));
        }
        vo.setInStock(inStockList);
        vo.setBorrowed(borrowList);
        return vo;
    }

    @Override
    public LeadershipDashboardVO getLeadershipData() {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate lastMonthStart = monthStart.minusMonths(1);
        LocalDate lastMonthEnd = monthStart.minusDays(1);
        OffsetDateTime curStart = monthStart.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime curEnd = now.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime lastStart = lastMonthStart.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime lastEnd = monthStart.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();

        LeadershipDashboardVO vo = new LeadershipDashboardVO();
        Map<String, LeadershipDashboardVO.Kpi> kpis = new LinkedHashMap<>();

        // 1. 总资产（公物仓）
        long totalAssets = count(assetCardRepository, null);
        long assetsCur = countBetween(assetCardRepository, curStart, curEnd);
        long assetsLast = countBetween(assetCardRepository, lastStart, lastEnd);
        kpis.put("totalAssets", kpi(totalAssets, assetsCur, assetsLast));

        // 2. 在仓资产
        long inStockAssets = count(assetCardRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("assetStatus")), ASSET_IN_STOCK));
        kpis.put("inStockAssets", kpi(inStockAssets, assetsCur, assetsLast));

        // 3. 本月用车次数（APPROVED）
        long carUsesCur = count(applyOrderRepository, (root, cq, cb) -> cb.and(
                cb.equal(cb.upper(root.get("applyStatus")), "APPROVED"),
                cb.between(root.get("createTime"), curStart, curEnd)));
        long carUsesLast = count(applyOrderRepository, (root, cq, cb) -> cb.and(
                cb.equal(cb.upper(root.get("applyStatus")), "APPROVED"),
                cb.between(root.get("createTime"), lastStart, lastEnd)));
        kpis.put("monthCarUses", kpi(carUsesCur, carUsesCur, carUsesLast));

        // 4. 公寓入住率
        long totalRooms = count(roomRepository, null);
        long occupiedRooms = count(roomRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("roomStatus")), "OCCUPIED"));
        double occupancyRate = totalRooms == 0 ? 0 : Math.round(occupiedRooms * 1000.0 / totalRooms) / 10.0;
        long occupantCur = countBetween(occupantRepository, curStart, curEnd);
        long occupantLast = countBetween(occupantRepository, lastStart, lastEnd);
        kpis.put("occupancyRate", kpi(occupancyRate, occupantCur, occupantLast));

        // 5. 本月食堂预约人次
        long mealCur = mealReservationRepository.count((root, cq, cb) -> cb.and(
                cb.between(root.get("mealDate"), monthStart, now),
                cb.isFalse(root.get("isCancelled"))));
        long mealLast = mealReservationRepository.count((root, cq, cb) -> cb.and(
                cb.between(root.get("mealDate"), lastMonthStart, lastMonthEnd),
                cb.isFalse(root.get("isCancelled"))));
        kpis.put("monthMealReserves", kpi(mealCur, mealCur, mealLast));

        // 6. 待审批总数
        long borrowPending = count(borrowOrderRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("orderStatus")), BORROW_PENDING));
        long applyPending = count(applyOrderRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("applyStatus")), APPLY_PENDING));
        long purchasePending = count(purchaseOrderRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("orderStatus")), "PENDING"));
        long occupantPending = count(occupantRepository, (root, cq, cb) ->
                cb.equal(cb.upper(root.get("occupantStatus")), OCCUPANT_PENDING));
        long pendingTotal = borrowPending + applyPending + purchasePending + occupantPending;
        kpis.put("pendingApprovals", kpi(pendingTotal, pendingTotal, pendingTotal));

        vo.setKpis(kpis);

        // 中部：近 7 天趋势
        LocalDate trendStart = now.minusDays(TREND_DAYS - 1);
        OffsetDateTime trendStartTime = trendStart.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            dates.add(trendStart.plusDays(i).format(DATE_FORMATTER));
        }
        List<GcAssetCard> assets = assetCardRepository.findAll((root, cq, cb) -> cb.and(
                cb.isFalse(root.get("isDeleted")),
                cb.greaterThanOrEqualTo(root.get("createTime"), trendStartTime)));
        Map<LocalDate, Long> gcDayMap = groupByDate(assets, GcAssetCard::getCreateTime);
        List<ClApplyOrder> applies = applyOrderRepository.findAll((root, cq, cb) -> cb.and(
                cb.isFalse(root.get("isDeleted")),
                cb.greaterThanOrEqualTo(root.get("createTime"), trendStartTime)));
        Map<LocalDate, Long> clDayMap = groupByDate(applies, ClApplyOrder::getCreateTime);
        List<StMealReservation> meals = mealReservationRepository.findAll((root, cq, cb) -> cb.and(
                cb.between(root.get("mealDate"), trendStart, now),
                cb.isFalse(root.get("isCancelled"))));
        Map<LocalDate, Long> stDayMap = groupByDateLocal(meals, StMealReservation::getMealDate);

        LeadershipDashboardVO.TrendData trend = new LeadershipDashboardVO.TrendData();
        trend.setDates(dates);
        List<Number> gcSeries = new ArrayList<>();
        List<Number> clSeries = new ArrayList<>();
        List<Number> stSeries = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate d = trendStart.plusDays(i);
            gcSeries.add(gcDayMap.getOrDefault(d, 0L));
            clSeries.add(clDayMap.getOrDefault(d, 0L));
            stSeries.add(stDayMap.getOrDefault(d, 0L));
        }
        trend.setGc(gcSeries);
        trend.setCl(clSeries);
        trend.setSt(stSeries);
        vo.setTrends(trend);

        // 中部：本月业务分布
        Map<String, Number> dist = new LinkedHashMap<>();
        dist.put("gc", countBetween(borrowOrderRepository, curStart, curEnd));
        dist.put("cl", countBetween(applyOrderRepository, curStart, curEnd));
        dist.put("gy", occupantCur);
        dist.put("st", mealCur);
        vo.setDistribution(dist);

        // 底部：本月核心数据明细
        Map<String, LeadershipDashboardVO.ModuleDetail> details = new LinkedHashMap<>();
        LeadershipDashboardVO.ModuleDetail gcDetail = new LeadershipDashboardVO.ModuleDetail();
        gcDetail.setMonthNew(assetsCur);
        gcDetail.setMonthDone(countBetween(returnOrderRepository, curStart, curEnd));
        double gcChange = changePct(assetsCur, assetsLast);
        gcDetail.setLastMonth(gcChange);
        gcDetail.setYoy(gcChange);
        gcDetail.setStatus(gcChange < -50 ? "abnormal" : "normal");

        LeadershipDashboardVO.ModuleDetail clDetail = new LeadershipDashboardVO.ModuleDetail();
        double clApplyCur = countBetween(applyOrderRepository, curStart, curEnd);
        double clApplyLast = countBetween(applyOrderRepository, lastStart, lastEnd);
        clDetail.setMonthNew(clApplyCur);
        clDetail.setMonthDone(carUsesCur);
        double clChange = changePct(clApplyCur, clApplyLast);
        clDetail.setLastMonth(clChange);
        clDetail.setYoy(clChange);
        clDetail.setStatus(clChange < -50 ? "abnormal" : "normal");

        LeadershipDashboardVO.ModuleDetail gyDetail = new LeadershipDashboardVO.ModuleDetail();
        gyDetail.setMonthNew(occupantCur);
        gyDetail.setMonthDone(count(occupantRepository, (root, cq, cb) -> cb.and(
                cb.between(root.get("createTime"), curStart, curEnd),
                cb.equal(cb.upper(root.get("occupantStatus")), "ACTIVE"))));
        double gyChange = changePct(occupantCur, occupantLast);
        gyDetail.setLastMonth(gyChange);
        gyDetail.setYoy(gyChange);
        gyDetail.setStatus(gyChange < -50 ? "abnormal" : "normal");

        LeadershipDashboardVO.ModuleDetail stDetail = new LeadershipDashboardVO.ModuleDetail();
        double monthPurchase = purchaseOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.or(
                    cb.equal(cb.upper(root.get("orderStatus")), PURCHASE_COMPLETED),
                    cb.equal(cb.upper(root.get("orderStatus")), PURCHASE_RECEIVED)));
            predicates.add(cb.between(root.get("createTime"), curStart, curEnd));
            return cb.and(predicates.toArray(new Predicate[0]));
        }).stream().map(StPurchaseOrder::getTotalAmount).filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
        stDetail.setMonthNew(monthPurchase);
        stDetail.setMonthDone(mealCur);
        double stChange = changePct(mealCur, mealLast);
        stDetail.setLastMonth(stChange);
        stDetail.setYoy(stChange);
        stDetail.setStatus(stChange < -50 ? "abnormal" : "normal");

        details.put("gc", gcDetail);
        details.put("cl", clDetail);
        details.put("gy", gyDetail);
        details.put("st", stDetail);
        vo.setDetails(details);

        return vo;
    }

    /**
     * 按日期分组统计
     */
    private <T> Map<LocalDate, Long> groupByDate(List<T> list, java.util.function.Function<T, OffsetDateTime> timeGetter) {
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        for (T item : list) {
            OffsetDateTime time = timeGetter.apply(item);
            if (time == null) continue;
            LocalDate date = time.toLocalDate();
            map.merge(date, 1L, Long::sum);
        }
        return map;
    }

    private <T> long count(org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
                           Specification<T> extraSpec) {
        if (extraSpec == null) {
            if (repo instanceof org.springframework.data.jpa.repository.JpaSpecificationExecutor) {
                return ((org.springframework.data.jpa.repository.JpaSpecificationExecutor<T>) repo)
                        .count((root, cq, cb) -> cb.isFalse(root.get("isDeleted")));
            }
            return repo.count();
        }
        return ((org.springframework.data.jpa.repository.JpaSpecificationExecutor<T>) repo)
                .count((root, cq, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isFalse(root.get("isDeleted")));
                    predicates.add(extraSpec.toPredicate(root, cq, cb));
                    return cb.and(predicates.toArray(new Predicate[0]));
                });
    }

    /**
     * 统计指定时间区间内创建（createTime）的记录数
     */
    private <T> long countBetween(org.springframework.data.jpa.repository.JpaRepository<T, Long> repo,
                                  OffsetDateTime start, OffsetDateTime end) {
        return count(repo, (root, cq, cb) -> cb.between(root.get("createTime"), start, end));
    }

    /**
     * 按 LocalDate 字段分组统计（用于预约等按业务日期统计）
     */
    private <T> Map<LocalDate, Long> groupByDateLocal(List<T> list,
                                                      java.util.function.Function<T, LocalDate> dateGetter) {
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        for (T item : list) {
            LocalDate date = dateGetter.apply(item);
            if (date == null) continue;
            map.merge(date, 1L, Long::sum);
        }
        return map;
    }

    /**
     * 计算环比变化百分比
     */
    private double changePct(double cur, double last) {
        double change = 0;
        if (last != 0) {
            change = (cur - last) / last * 100;
        } else if (cur > 0) {
            change = 100;
        }
        return Math.round(change * 10) / 10.0;
    }

    /**
     * 构造 KPI 指标（value 为展示值，change 依据本月/上月计数计算）
     */
    private LeadershipDashboardVO.Kpi kpi(double value, double curCount, double lastCount) {
        double change = changePct(curCount, lastCount);
        String trend = change > 0.05 ? "up" : (change < -0.05 ? "down" : "flat");
        return new LeadershipDashboardVO.Kpi(value, change, trend);
    }

    private String formatTime(OffsetDateTime time) {
        return time.atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
