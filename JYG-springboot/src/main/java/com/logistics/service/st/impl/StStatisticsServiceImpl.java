package com.logistics.service.st.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.st.ConsumeStatisticsVO;
import com.logistics.dto.st.DailyConsumeItem;
import com.logistics.dto.st.MaterialStatItem;
import com.logistics.dto.st.MonthlyPurchaseItem;
import com.logistics.dto.st.PurchaseStatisticsVO;
import com.logistics.dto.st.StatisticsQuery;
import com.logistics.dto.st.WastePageQuery;
import com.logistics.dto.st.WasteRecordRequest;
import com.logistics.dto.st.WasteStatisticsVO;
import com.logistics.dto.st.WasteTrendItem;
import com.logistics.dto.st.WasteVO;
import com.logistics.entity.PayTransaction;
import com.logistics.entity.StKitchenWaste;
import com.logistics.entity.StMealReservation;
import com.logistics.entity.StPurchaseDetail;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.entity.SysUser;
import com.logistics.repository.PayTransactionRepository;
import com.logistics.repository.StKitchenWasteRepository;
import com.logistics.repository.StMealReservationRepository;
import com.logistics.repository.StPurchaseDetailRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.st.StStatisticsService;
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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食堂餐余处理与统计分析服务实现
 */
@Service
@RequiredArgsConstructor
public class StStatisticsServiceImpl implements StStatisticsService {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    /** 统计周期 */
    private static final String PERIOD_DAY = "DAY";
    private static final String PERIOD_WEEK = "WEEK";
    private static final String PERIOD_MONTH = "MONTH";
    private static final String PERIOD_YEAR = "YEAR";

    /** 采购单排除状态：草稿、驳回不计入采购统计 */
    private static final String PURCHASE_STATUS_DRAFT = "DRAFT";
    private static final String PURCHASE_STATUS_REJECTED = "REJECTED";

    /** 消费交易类型 */
    private static final String TX_CONSUME = "CONSUME";
    private static final String PAY_STATUS_SUCCESS = "SUCCESS";

    /** 餐次中文名 */
    private static final Map<String, String> MEAL_TYPE_LABEL_MAP = new HashMap<>();

    /** 餐余类型中文名 */
    private static final Map<String, String> WASTE_TYPE_LABEL_MAP = new HashMap<>();

    /** 处理方式中文名 */
    private static final Map<String, String> DISPOSAL_LABEL_MAP = new HashMap<>();

    static {
        MEAL_TYPE_LABEL_MAP.put("BREAKFAST", "早餐");
        MEAL_TYPE_LABEL_MAP.put("LUNCH", "午餐");
        MEAL_TYPE_LABEL_MAP.put("DINNER", "晚餐");
        WASTE_TYPE_LABEL_MAP.put("FOOD", "食物");
        WASTE_TYPE_LABEL_MAP.put("PACKAGING", "包装");
        WASTE_TYPE_LABEL_MAP.put("OTHER", "其他");
        DISPOSAL_LABEL_MAP.put("COMPOST", "堆肥");
        DISPOSAL_LABEL_MAP.put("FEED", "饲料");
        DISPOSAL_LABEL_MAP.put("WASTE", "废弃物");
    }

    /** 月份格式：yyyy-MM */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final StKitchenWasteRepository kitchenWasteRepository;
    private final PayTransactionRepository payTransactionRepository;
    private final StMealReservationRepository mealReservationRepository;
    private final StPurchaseOrderRepository purchaseOrderRepository;
    private final StPurchaseDetailRepository purchaseDetailRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public WasteVO recordWaste(WasteRecordRequest request) {
        if (request.getWasteWeight() == null || request.getWasteWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("餐余重量必须大于0");
        }
        if (request.getRecordDate() == null) {
            throw new BusinessException("记录日期不能为空");
        }
        OffsetDateTime now = OffsetDateTime.now();
        StKitchenWaste waste = new StKitchenWaste();
        waste.setRecordDate(request.getRecordDate());
        waste.setMealType(request.getMealType());
        waste.setWasteWeight(request.getWasteWeight());
        waste.setWasteType(request.getWasteType());
        waste.setDisposalMethod(request.getDisposalMethod());
        waste.setDisposalPerson(request.getDisposalPerson());
        waste.setRemark(request.getRemark());
        waste.setCreateBy(resolveCurrentUserId());
        waste.setCreateTime(now);
        waste.setUpdateTime(now);
        waste.setIsDeleted(false);
        return toVO(kitchenWasteRepository.save(waste));
    }

    @Override
    public PageResult<WasteVO> queryWastePage(WastePageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<StKitchenWaste> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            if (query.getRecordDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), query.getRecordDateStart()));
            }
            if (query.getRecordDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), query.getRecordDateEnd()));
            }
            if (StringUtils.hasText(query.getMealType())) {
                predicates.add(cb.equal(cb.lower(root.get("mealType")), query.getMealType().toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StKitchenWaste> result = kitchenWasteRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "recordDate")));
        List<WasteVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public WasteStatisticsVO getWasteStatistics(StatisticsQuery query) {
        validateQuery(query);
        Specification<StKitchenWaste> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), query.getStartDate()));
            predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), query.getEndDate()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<StKitchenWaste> records = kitchenWasteRepository.findAll(spec);
        BigDecimal totalWeight = records.stream()
                .map(StKitchenWaste::getWasteWeight)
                .filter(w -> w != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long days = java.time.temporal.ChronoUnit.DAYS.between(query.getStartDate(), query.getEndDate()) + 1;
        BigDecimal avg = days > 0 ? totalWeight.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 趋势：按日期分组
        Map<LocalDate, BigDecimal> trendMap = new LinkedHashMap<>();
        for (StKitchenWaste r : records) {
            trendMap.merge(r.getRecordDate(), r.getWasteWeight() == null ? BigDecimal.ZERO : r.getWasteWeight(), BigDecimal::add);
        }
        List<WasteTrendItem> trendData = trendMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    WasteTrendItem item = new WasteTrendItem();
                    item.setDate(e.getKey());
                    item.setTotalWeight(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 按餐次统计
        Map<String, BigDecimal> byMealType = new LinkedHashMap<>();
        for (StKitchenWaste r : records) {
            String mealType = StringUtils.hasText(r.getMealType()) ? r.getMealType().toUpperCase() : "UNKNOWN";
            byMealType.merge(mealType, r.getWasteWeight() == null ? BigDecimal.ZERO : r.getWasteWeight(), BigDecimal::add);
        }

        WasteStatisticsVO vo = new WasteStatisticsVO();
        vo.setTotalWeight(totalWeight);
        vo.setAvgWeightPerDay(avg);
        vo.setTrendData(trendData);
        vo.setByMealType(byMealType);
        return vo;
    }

    @Override
    public ConsumeStatisticsVO getConsumeStatistics(StatisticsQuery query) {
        validateQuery(query);
        LocalDateTime start = query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate().atTime(23, 59, 59);

        Specification<PayTransaction> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(cb.lower(root.get("transactionType")), TX_CONSUME.toLowerCase()));
            predicates.add(cb.equal(cb.lower(root.get("payStatus")), PAY_STATUS_SUCCESS.toLowerCase()));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<PayTransaction> transactions = payTransactionRepository.findAll(spec);

        BigDecimal totalAmount = transactions.stream()
                .map(t -> t.getAmount() == null ? BigDecimal.ZERO : t.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 每日消费趋势
        Map<LocalDate, DailyConsumeItem> dailyMap = new LinkedHashMap<>();
        for (PayTransaction t : transactions) {
            LocalDate date = t.getCreateTime().toLocalDate();
            DailyConsumeItem item = dailyMap.computeIfAbsent(date, d -> {
                DailyConsumeItem i = new DailyConsumeItem();
                i.setDate(d);
                i.setTotalAmount(BigDecimal.ZERO);
                i.setCount(0);
                return i;
            });
            item.setTotalAmount(item.getTotalAmount().add(t.getAmount() == null ? BigDecimal.ZERO : t.getAmount().abs()));
            item.setCount(item.getCount() + 1);
        }
        List<DailyConsumeItem> dailyTrend = dailyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        // 预约总人数
        int totalReservations = countReservations(query.getStartDate(), query.getEndDate());

        ConsumeStatisticsVO vo = new ConsumeStatisticsVO();
        vo.setTotalAmount(totalAmount);
        vo.setTotalCount(transactions.size());
        vo.setTotalReservations(totalReservations);
        vo.setAvgAmountPerPerson(totalReservations > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalReservations), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        vo.setDailyTrend(dailyTrend);
        return vo;
    }

    @Override
    public PurchaseStatisticsVO getPurchaseStatistics(StatisticsQuery query) {
        validateQuery(query);
        LocalDateTime start = query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate().atTime(23, 59, 59);

        Specification<StPurchaseOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.notEqual(cb.lower(root.get("orderStatus")), PURCHASE_STATUS_DRAFT.toLowerCase()));
            predicates.add(cb.notEqual(cb.lower(root.get("orderStatus")), PURCHASE_STATUS_REJECTED.toLowerCase()));
            predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), start.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), end.atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<StPurchaseOrder> orders = purchaseOrderRepository.findAll(spec);

        BigDecimal totalPurchaseAmount = orders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 月度趋势
        Map<String, BigDecimal> monthlyMap = new LinkedHashMap<>();
        for (StPurchaseOrder o : orders) {
            if (o.getCreateTime() == null) continue;
            String month = o.getCreateTime().format(MONTH_FORMATTER);
            monthlyMap.merge(month, o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount(), BigDecimal::add);
        }
        List<MonthlyPurchaseItem> monthlyTrend = monthlyMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    MonthlyPurchaseItem item = new MonthlyPurchaseItem();
                    item.setMonth(e.getKey());
                    item.setTotalAmount(e.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 物资采购金额 Top5
        List<Long> orderIds = orders.stream().map(StPurchaseOrder::getId).collect(Collectors.toList());
        Map<Long, MaterialStatItem> materialMap = new LinkedHashMap<>();
        if (!orderIds.isEmpty()) {
            List<StPurchaseDetail> details = purchaseDetailRepository.findByPurchaseOrderIdIn(orderIds);
            for (StPurchaseDetail d : details) {
                if (d.getMaterialId() == null) continue;
                MaterialStatItem item = materialMap.computeIfAbsent(d.getMaterialId(), k -> {
                    MaterialStatItem i = new MaterialStatItem();
                    i.setMaterialId(k);
                    i.setMaterialName(d.getMaterialName());
                    i.setTotalAmount(BigDecimal.ZERO);
                    return i;
                });
                item.setTotalAmount(item.getTotalAmount().add(d.getSubtotal() == null ? BigDecimal.ZERO : d.getSubtotal()));
                if (item.getMaterialName() == null) {
                    item.setMaterialName(d.getMaterialName());
                }
            }
        }
        List<MaterialStatItem> topMaterials = materialMap.values().stream()
                .sorted(Comparator.comparing(MaterialStatItem::getTotalAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        PurchaseStatisticsVO vo = new PurchaseStatisticsVO();
        vo.setTotalPurchaseAmount(totalPurchaseAmount);
        vo.setTopMaterials(topMaterials);
        vo.setMonthlyTrend(monthlyTrend);
        return vo;
    }

    /**
     * 统计区间内未取消的预约总人数
     */
    private int countReservations(LocalDate startDate, LocalDate endDate) {
        Specification<StMealReservation> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isCancelled")));
            predicates.add(cb.greaterThanOrEqualTo(root.get("mealDate"), startDate));
            predicates.add(cb.lessThanOrEqualTo(root.get("mealDate"), endDate));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return mealReservationRepository.findAll(spec).stream()
                .mapToInt(r -> r.getMealCount() == null ? 0 : r.getMealCount())
                .sum();
    }

    private void validateQuery(StatisticsQuery query) {
        if (query.getStartDate() == null || query.getEndDate() == null) {
            throw new BusinessException("统计日期不能为空");
        }
        if (query.getStartDate().isAfter(query.getEndDate())) {
            throw new BusinessException("起始日期不能晚于结束日期");
        }
        String period = query.getPeriodType() == null ? null : query.getPeriodType().toUpperCase();
        if (!PERIOD_DAY.equals(period) && !PERIOD_WEEK.equals(period)
                && !PERIOD_MONTH.equals(period) && !PERIOD_YEAR.equals(period)) {
            throw new BusinessException("统计周期无效");
        }
    }

    private WasteVO toVO(StKitchenWaste waste) {
        WasteVO vo = new WasteVO();
        BeanUtils.copyProperties(waste, vo);
        vo.setMealTypeLabel(label(MEAL_TYPE_LABEL_MAP, waste.getMealType()));
        vo.setWasteTypeLabel(label(WASTE_TYPE_LABEL_MAP, waste.getWasteType()));
        vo.setDisposalMethodLabel(label(DISPOSAL_LABEL_MAP, waste.getDisposalMethod()));
        return vo;
    }

    private String label(Map<String, String> map, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return map.getOrDefault(value.toUpperCase(), value);
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
                return sysUserRepository.findByUsername(authentication.getName())
                        .map(SysUser::getId)
                        .orElse(DEFAULT_USER_ID);
            }
        }
        return DEFAULT_USER_ID;
    }
}
