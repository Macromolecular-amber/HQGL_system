package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.dto.dashboard.DashboardStatisticsVO;
import com.logistics.dto.dashboard.MessageVO;
import com.logistics.dto.dashboard.TodoVO;
import com.logistics.dto.dashboard.TrendVO;
import com.logistics.entity.ClApplyOrder;
import com.logistics.entity.ClVehicleArchive;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.GcBorrowOrder;
import com.logistics.entity.GyOccupant;
import com.logistics.entity.GyRoom;
import com.logistics.entity.StMealReservation;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.repository.ClApplyOrderRepository;
import com.logistics.repository.ClVehicleArchiveRepository;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.GcBorrowOrderRepository;
import com.logistics.repository.GyOccupantRepository;
import com.logistics.repository.GyRoomRepository;
import com.logistics.repository.StMealReservationRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.service.DashboardService;
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
    private final GyOccupantRepository occupantRepository;

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
        // 借用审批
        List<GcBorrowOrder> borrowOrders = borrowOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), BORROW_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        for (GcBorrowOrder o : borrowOrders) {
            TodoVO todo = new TodoVO();
            todo.setId(o.getId());
            todo.setTitle("借用申请 " + o.getOrderNo() + "（" + (StringUtils.hasText(o.getApplicantName()) ? o.getApplicantName() : "") + "）");
            todo.setModule("公物仓");
            todo.setTime(o.getCreateTime() == null ? null : formatTime(o.getCreateTime()));
            todo.setStatus("待审批");
            todos.add(todo);
        }
        // 用车申请审批
        List<ClApplyOrder> applyOrders = applyOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("applyStatus")), APPLY_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        for (ClApplyOrder o : applyOrders) {
            TodoVO todo = new TodoVO();
            todo.setId(o.getId());
            todo.setTitle("用车申请 " + o.getApplyNo() + "（" + (StringUtils.hasText(o.getApplicantName()) ? o.getApplicantName() : "") + "）");
            todo.setModule("用车");
            todo.setTime(o.getCreateTime() == null ? null : formatTime(o.getCreateTime()));
            todo.setStatus("待审批");
            todos.add(todo);
        }
        // 采购申请（草稿待审批）
        List<StPurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("orderStatus")), "DRAFT"));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        for (StPurchaseOrder o : purchaseOrders) {
            TodoVO todo = new TodoVO();
            todo.setId(o.getId());
            todo.setTitle("采购申请 " + o.getOrderNo());
            todo.setModule("食堂");
            todo.setTime(o.getCreateTime() == null ? null : formatTime(o.getCreateTime()));
            todo.setStatus("待审批");
            todos.add(todo);
        }
        // 公寓入住申请
        List<GyOccupant> occupants = occupantRepository.findAll((root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.equal(cb.upper(root.get("occupantStatus")), OCCUPANT_PENDING));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        for (GyOccupant o : occupants) {
            TodoVO todo = new TodoVO();
            todo.setId(o.getId());
            todo.setTitle("公寓入住申请（" + (StringUtils.hasText(o.getOccupantName()) ? o.getOccupantName() : "") + "）");
            todo.setModule("公寓");
            todo.setTime(o.getCreateTime() == null ? null : formatTime(o.getCreateTime()));
            todo.setStatus("待审批");
            todos.add(todo);
        }
        // 按时间倒序
        todos.sort(Comparator.comparing(TodoVO::getTime, Comparator.nullsLast(String::compareTo)).reversed());
        return todos;
    }

    @Override
    public List<MessageVO> getMessages(Long userId) {
        // sys_message 消息表尚未建立，先返回空列表，后续对接消息中心
        return new ArrayList<>();
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

    private String formatTime(OffsetDateTime time) {
        return time.atZoneSameInstant(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
