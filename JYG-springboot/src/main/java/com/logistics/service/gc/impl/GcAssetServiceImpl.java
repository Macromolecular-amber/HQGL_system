package com.logistics.service.gc.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gc.AssetApplyRequest;
import com.logistics.dto.gc.AssetAuditRequest;
import com.logistics.dto.gc.AssetDetailVO;
import com.logistics.dto.gc.AssetListVO;
import com.logistics.dto.gc.AssetPageQuery;
import com.logistics.dto.gc.AssetQuery;
import com.logistics.dto.gc.AssetVO;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageNotifier;
import com.logistics.service.gc.GcAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 公物仓资产服务实现
 */
@Service
@RequiredArgsConstructor
public class GcAssetServiceImpl implements GcAssetService {

    /** 资产状态：待入仓 */
    private static final String STATUS_PENDING = "PENDING";
    /** 资产状态：在仓 */
    private static final String STATUS_IN_STOCK = "IN_STOCK";
    /** 资产状态：已驳回 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 审核结果：通过 */
    private static final String AUDIT_PASS = "PASS";
    /** 审核结果：驳回 */
    private static final String AUDIT_REJECT = "REJECT";

    /** 分类编码 -> 分类名称（临时硬编码，后续扩展字典表） */
    private static final Map<String, String> CATEGORY_NAME_MAP = new HashMap<>();

    static {
        CATEGORY_NAME_MAP.put("IT_01", "办公设备");
        CATEGORY_NAME_MAP.put("JJ_01", "办公家具");
        CATEGORY_NAME_MAP.put("DQ_01", "电器设备");
        CATEGORY_NAME_MAP.put("CL_01", "车辆");
        CATEGORY_NAME_MAP.put("FURNITURE", "办公家具");
        CATEGORY_NAME_MAP.put("IT", "办公设备");
        CATEGORY_NAME_MAP.put("ELECTRONIC", "电器设备");
        CATEGORY_NAME_MAP.put("VEHICLE", "车辆");
    }

    private final GcAssetCardRepository gcAssetCardRepository;
    private final SysUnitRepository sysUnitRepository;
    private final SysUserRepository sysUserRepository;
    private final MessageNotifier messageNotifier;

    @Override
    public AssetVO apply(AssetApplyRequest request) {
        // 业务校验：原值 >= 0，使用年限 > 0
        if (request.getOriginalValue() == null || request.getOriginalValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("资产原值不能小于0");
        }
        if (request.getUsefulLife() == null || request.getUsefulLife() <= 0) {
            throw new BusinessException("使用年限必须大于0");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GcAssetCard card = new GcAssetCard();
        card.setAssetCode(generateAssetCode(request.getCategoryCode()));
        card.setAssetName(request.getAssetName());
        card.setCategoryCode(request.getCategoryCode());
        card.setSpecModel(request.getSpecModel());
        card.setBrand(request.getBrand());
        card.setOriginalValue(request.getOriginalValue());
        card.setPurchaseDate(request.getPurchaseDate());
        card.setUsefulLife(request.getUsefulLife());
        card.setLocation(request.getLocation());
        card.setOwnerUnitId(request.getOwnerUnitId());
        card.setOwnerUnitName(resolveUnitName(request.getOwnerUnitId()));
        card.setDescription(request.getDescription());
        card.setPhotoUrls(request.getPhotoUrls() == null ? null : String.join(",", request.getPhotoUrls()));
        card.setRemark(request.getRemark());
        // 默认值
        card.setQuantity(1);
        card.setResidualRate(new BigDecimal("5.00"));
        card.setCurrentValue(request.getOriginalValue());
        card.setAccumulatedDepreciation(BigDecimal.ZERO);
        card.setDepreciationMethod("STRAIGHT_LINE");
        card.setAssetStatus(STATUS_PENDING);
        card.setIsDeleted(false);
        card.setCreateBy(resolveCurrentUserId());
        card.setCreateTime(now);
        card.setUpdateTime(now);

        GcAssetCard saved = gcAssetCardRepository.save(card);
        // 通知业务管理员审核
        messageNotifier.notifyRoles("您有新的资产入仓申请待审核", "gc-asset", saved.getAssetCode(), "BIZ_ADMIN");
        return toVO(saved);
    }

    @Override
    public void audit(AssetAuditRequest request) {
        GcAssetCard card = gcAssetCardRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("资产不存在"));
        if (!STATUS_PENDING.equals(card.getAssetStatus())) {
            throw new BusinessException("当前状态不可审核");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String result = request.getAuditResult();
        if (AUDIT_PASS.equals(result)) {
            card.setAssetStatus(STATUS_IN_STOCK);
            card.setInStockTime(now);
        } else if (AUDIT_REJECT.equals(result)) {
            card.setAssetStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审核结果无效，只能为 PASS 或 REJECT");
        }
        // 记录审核信息
        card.setAuditTime(now);
        card.setAuditRemark(request.getAuditRemark());
        card.setAuditUserName(currentUserName());
        card.setUpdateTime(now);

        gcAssetCardRepository.save(card);
        // 通知申请人审核结果
        String resultTitle = AUDIT_PASS.equals(result) ? "您的资产入仓申请已审核通过" : "您的资产入仓申请已驳回";
        messageNotifier.notifyUser(resultTitle, "gc-asset", card.getAssetCode(), card.getCreateBy());
    }

    @Override
    public PageResult<AssetVO> queryPage(AssetPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcAssetCard> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getAssetName())) {
                predicates.add(cb.like(root.get("assetName"), "%" + query.getAssetName().trim() + "%"));
            }
            if (StringUtils.hasText(query.getCategoryCode())) {
                predicates.add(cb.equal(root.get("categoryCode"), query.getCategoryCode()));
            }
            if (StringUtils.hasText(query.getStatus())) {
                predicates.add(cb.equal(root.get("assetStatus"), query.getStatus()));
            }
            if (query.getOwnerUnitId() != null) {
                predicates.add(cb.equal(root.get("ownerUnitId"), query.getOwnerUnitId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GcAssetCard> result = gcAssetCardRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
        List<AssetVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public AssetVO getDetail(Long id) {
        GcAssetCard card = gcAssetCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        return toVO(card);
    }

    @Override
    public PageResult<AssetListVO> queryAssetList(AssetQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcAssetCard> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getAssetName())) {
                predicates.add(cb.like(root.get("assetName"), "%" + query.getAssetName().trim() + "%"));
            }
            if (StringUtils.hasText(query.getCategoryCode())) {
                predicates.add(cb.equal(root.get("categoryCode"), query.getCategoryCode()));
            }
            if (StringUtils.hasText(query.getAssetStatus())) {
                predicates.add(cb.equal(root.get("assetStatus"), query.getAssetStatus()));
            }
            if (query.getOwnerUnitId() != null) {
                predicates.add(cb.equal(root.get("ownerUnitId"), query.getOwnerUnitId()));
            }
            if (query.getStartValue() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("originalValue"), query.getStartValue()));
            }
            if (query.getEndValue() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("originalValue"), query.getEndValue()));
            }
            if (query.getPurchaseDateStart() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("purchaseDate"), query.getPurchaseDateStart()));
            }
            if (query.getPurchaseDateEnd() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("purchaseDate"), query.getPurchaseDateEnd()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GcAssetCard> result = gcAssetCardRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<AssetListVO> vos = result.getContent().stream().map(this::toListVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public AssetDetailVO getAssetDetail(Long id) {
        GcAssetCard card = gcAssetCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException("资产不存在"));
        return toDetailVO(card);
    }

    @Override
    public List<AssetListVO> listAvailableAssets(String categoryCode) {
        Specification<GcAssetCard> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            // 仅限在仓资产
            predicates.add(cb.equal(root.get("assetStatus"), "IN_STOCK"));
            if (StringUtils.hasText(categoryCode)) {
                predicates.add(cb.equal(root.get("categoryCode"), categoryCode));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return gcAssetCardRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createTime"))
                .stream().map(this::toListVO).collect(Collectors.toList());
    }

    /**
     * 生成资产编号：GLZ-分类编码-年份后两位-当天序号，如 GLZ-FURNITURE-26-0001
     */
    private String generateAssetCode(String categoryCode) {
        LocalDate today = LocalDate.now();
        String yy = today.format(DateTimeFormatter.ofPattern("yy"));
        String prefix = "GLZ-" + categoryCode + "-" + yy + "-";
        OffsetDateTime dayStart = today.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        long todayCount = gcAssetCardRepository.countByAssetCodeStartingWithAndCreateTimeAfter(prefix, dayStart);
        return prefix + String.format("%04d", todayCount + 1);
    }

    /**
     * 实体转 VO，补充单位名称、分类名称
     */
    private AssetVO toVO(GcAssetCard card) {
        AssetVO vo = new AssetVO();
        BeanUtils.copyProperties(card, vo);
        vo.setUnitName(resolveUnitName(card.getOwnerUnitId(), card.getOwnerUnitName()));
        if (!StringUtils.hasText(vo.getCategoryName())) {
            vo.setCategoryName(card.getCategoryCode());
        }
        return vo;
    }

    /**
     * 实体转列表 VO，补充单位名称、分类名称
     */
    private AssetListVO toListVO(GcAssetCard card) {
        AssetListVO vo = new AssetListVO();
        BeanUtils.copyProperties(card, vo);
        vo.setOwnerUnitName(resolveUnitName(card.getOwnerUnitId(), card.getOwnerUnitName()));
        vo.setCategoryName(resolveCategoryName(card));
        return vo;
    }

    /**
     * 实体转详情 VO，补充单位名称、分类名称、审核人
     */
    private AssetDetailVO toDetailVO(GcAssetCard card) {
        AssetDetailVO vo = new AssetDetailVO();
        BeanUtils.copyProperties(card, vo);
        vo.setOwnerUnitName(resolveUnitName(card.getOwnerUnitId(), card.getOwnerUnitName()));
        vo.setUnitName(resolveUnitName(card.getOwnerUnitId(), card.getOwnerUnitName()));
        vo.setCategoryName(resolveCategoryName(card));
        vo.setAuditUser(card.getAuditUserName());
        return vo;
    }

    /**
     * 解析分类名称：优先实体存储值，其次硬编码映射，最后回退分类编码
     */
    private String resolveCategoryName(GcAssetCard card) {
        if (StringUtils.hasText(card.getCategoryName())) {
            return card.getCategoryName();
        }
        String name = CATEGORY_NAME_MAP.get(card.getCategoryCode());
        return name != null ? name : card.getCategoryCode();
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) {
            return null;
        }
        return sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
    }

    private String resolveUnitName(Long unitId, String fallback) {
        String name = resolveUnitName(unitId);
        return StringUtils.hasText(name) ? name : fallback;
    }

    /**
     * 从 SecurityContext 解析当前用户 ID，未匹配到时返回 null
     */
    private Long resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                return sysUserRepository.findByUsername(authentication.getName())
                        .map(SysUser::getId)
                        .orElse(null);
            }
        }
        return null;
    }

    /**
     * 当前登录用户名
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
