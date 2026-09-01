package com.logistics.service.st.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.st.MealCancelRequest;
import com.logistics.dto.st.MealPageQuery;
import com.logistics.dto.st.MealReservationVO;
import com.logistics.dto.st.MealReserveRequest;
import com.logistics.dto.st.MealStatisticsQuery;
import com.logistics.dto.st.MealStatisticsVO;
import com.logistics.dto.st.UnitMealStatVO;
import com.logistics.entity.StMealReservation;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.StMealReservationRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.st.StMealService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食堂预约订餐管理服务实现
 */
@Service
@RequiredArgsConstructor
public class StMealServiceImpl implements StMealService {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    /** 餐次默认开餐时间（用于计算可取消截止时间） */
    private static final Map<String, LocalTime> MEAL_TIME_MAP = new HashMap<>();

    /** 餐次中文名 */
    private static final Map<String, String> MEAL_TYPE_LABEL_MAP = new HashMap<>();

    static {
        MEAL_TIME_MAP.put("BREAKFAST", LocalTime.of(7, 0));
        MEAL_TIME_MAP.put("LUNCH", LocalTime.of(12, 0));
        MEAL_TIME_MAP.put("DINNER", LocalTime.of(18, 0));
        MEAL_TYPE_LABEL_MAP.put("BREAKFAST", "早餐");
        MEAL_TYPE_LABEL_MAP.put("LUNCH", "午餐");
        MEAL_TYPE_LABEL_MAP.put("DINNER", "晚餐");
    }

    /** 可取消提前量：餐前1小时 */
    private static final int CANCEL_LEAD_HOURS = 1;

    private final StMealReservationRepository mealReservationRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUnitRepository sysUnitRepository;

    @Override
    @Transactional
    public MealReservationVO reserve(MealReserveRequest request) {
        // 用餐日期不能早于今天
        LocalDate mealDate = request.getMealDate();
        if (mealDate == null) {
            throw new BusinessException("用餐日期不能为空");
        }
        if (mealDate.isBefore(LocalDate.now())) {
            throw new BusinessException("用餐日期不能早于今天");
        }
        // 餐次校验
        String mealType = request.getMealType() == null ? null : request.getMealType().toUpperCase();
        if (!MEAL_TYPE_LABEL_MAP.containsKey(mealType)) {
            throw new BusinessException("餐次无效");
        }
        // 人数校验
        int mealCount = request.getMealCount() == null ? 1 : request.getMealCount();
        if (mealCount < 1) {
            throw new BusinessException("预约人数不能小于1");
        }
        // 用户信息
        SysUser user = resolveCurrentUser();
        Long userId = user == null ? DEFAULT_USER_ID : user.getId();
        String userName = user == null ? null : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        // 幂等校验：当天同一人同一餐次不可重复预约
        List<StMealReservation> exists = mealReservationRepository
                .findByUserIdAndMealDateAndMealTypeAndIsCancelled(userId, mealDate, mealType, false);
        if (!exists.isEmpty()) {
            throw new BusinessException("当天该餐次已预约，请勿重复预约");
        }

        OffsetDateTime now = OffsetDateTime.now();
        StMealReservation reservation = new StMealReservation();
        reservation.setUserId(userId);
        reservation.setUserName(userName);
        // 单位：优先使用前端下拉选择的单位，否则回退到当前用户所属单位
        Long unitId = request.getUnitId();
        String unitName = null;
        if (unitId != null) {
            unitName = sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
        }
        if (unitId == null && user != null) {
            unitId = user.getUnitId();
            unitName = user.getUnitName();
        }
        reservation.setUnitId(unitId);
        reservation.setUnitName(unitName);
        reservation.setMealDate(mealDate);
        reservation.setMealType(mealType);
        reservation.setMealCount(mealCount);
        reservation.setReservationTime(now);
        reservation.setIsCancelled(false);
        reservation.setRemark(request.getRemark());
        reservation.setCreateTime(now);
        reservation.setUpdateTime(now);
        return toVO(mealReservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public void cancel(MealCancelRequest request) {
        StMealReservation reservation = mealReservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
        if (Boolean.TRUE.equals(reservation.getIsCancelled())) {
            throw new BusinessException("该预约已取消");
        }
        // 可取消时间校验：餐次开餐时间前1小时
        LocalTime mealTime = MEAL_TIME_MAP.get(reservation.getMealType());
        if (mealTime != null) {
            LocalDateTime cutoff = LocalDateTime.of(reservation.getMealDate(), mealTime).minusHours(CANCEL_LEAD_HOURS);
            if (LocalDateTime.now().isAfter(cutoff)) {
                throw new BusinessException("已过可取消时间（餐前1小时截止）");
            }
        }
        reservation.setIsCancelled(true);
        reservation.setCancelTime(OffsetDateTime.now());
        reservation.setUpdateTime(OffsetDateTime.now());
        mealReservationRepository.save(reservation);
    }

    @Override
    public PageResult<MealReservationVO> queryPage(MealPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();
        Long userId = resolveCurrentUser() == null ? DEFAULT_USER_ID : resolveCurrentUser().getId();

        Specification<StMealReservation> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 默认查询当前用户的预约记录
            predicates.add(cb.equal(root.get("userId"), userId));
            if (query.getMealDate() != null) {
                predicates.add(cb.equal(root.get("mealDate"), query.getMealDate()));
            }
            if (StringUtils.hasText(query.getMealType())) {
                predicates.add(cb.equal(cb.lower(root.get("mealType")), query.getMealType().toLowerCase()));
            }
            if (query.getIsCancelled() != null) {
                predicates.add(cb.equal(root.get("isCancelled"), query.getIsCancelled()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StMealReservation> result = mealReservationRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MealReservationVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public List<MealReservationVO> getByDate(LocalDate date) {
        List<StMealReservation> records = mealReservationRepository.findByMealDateAndIsCancelled(date, false);
        return records.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public MealStatisticsVO getStatistics(MealStatisticsQuery query) {
        LocalDate mealDate = query.getMealDate();
        if (mealDate == null) {
            throw new BusinessException("用餐日期不能为空");
        }
        List<StMealReservation> records = mealReservationRepository.findByMealDateAndIsCancelled(mealDate, false);
        if (StringUtils.hasText(query.getMealType())) {
            String type = query.getMealType().toUpperCase();
            records = records.stream()
                    .filter(r -> type.equalsIgnoreCase(r.getMealType()))
                    .collect(Collectors.toList());
        }
        MealStatisticsVO vo = new MealStatisticsVO();
        vo.setMealDate(mealDate);
        vo.setMealType(StringUtils.hasText(query.getMealType()) ? query.getMealType().toUpperCase() : null);
        // 总人数
        int totalCount = records.stream().mapToInt(r -> r.getMealCount() == null ? 0 : r.getMealCount()).sum();
        vo.setTotalCount(totalCount);
        // 按单位分组统计
        Map<Long, UnitMealStatVO> unitMap = new LinkedHashMap<>();
        for (StMealReservation r : records) {
            Long unitId = r.getUnitId();
            Long groupKey = unitId == null ? 0L : unitId;
            UnitMealStatVO stat = unitMap.computeIfAbsent(groupKey, k -> {
                UnitMealStatVO s = new UnitMealStatVO();
                s.setUnitId(groupKey == 0L ? null : groupKey);
                s.setUnitName(r.getUnitName());
                s.setCount(0);
                return s;
            });
            stat.setCount(stat.getCount() + (r.getMealCount() == null ? 0 : r.getMealCount()));
            if (stat.getUnitName() == null) {
                stat.setUnitName(r.getUnitName());
            }
        }
        vo.setUnitStatistics(new ArrayList<>(unitMap.values()));
        return vo;
    }

    /**
     * 预约转 VO，补充餐次中文名
     */
    private MealReservationVO toVO(StMealReservation reservation) {
        MealReservationVO vo = new MealReservationVO();
        BeanUtils.copyProperties(reservation, vo);
        vo.setMealTypeLabel(MEAL_TYPE_LABEL_MAP.getOrDefault(
                String.valueOf(reservation.getMealType()).toUpperCase(), reservation.getMealType()));
        return vo;
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户，未匹配时返回 null
     */
    private SysUser resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                return sysUserRepository.findByUsername(authentication.getName()).orElse(null);
            }
        }
        return null;
    }
}
