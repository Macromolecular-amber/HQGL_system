package com.logistics.service.st.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.st.InventoryPageQuery;
import com.logistics.dto.st.InventoryRecordVO;
import com.logistics.dto.st.MaterialVO;
import com.logistics.dto.st.StockAdjustRequest;
import com.logistics.dto.st.StockOutItemDTO;
import com.logistics.dto.st.StockOutRequest;
import com.logistics.entity.StInventoryRecord;
import com.logistics.entity.StMaterial;
import com.logistics.entity.StPurchaseDetail;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.entity.SysUser;
import com.logistics.repository.StInventoryRecordRepository;
import com.logistics.repository.StMaterialRepository;
import com.logistics.repository.StPurchaseDetailRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.st.StInventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食堂进销存管理服务实现
 */
@Service
@RequiredArgsConstructor
public class StInventoryServiceImpl implements StInventoryService {

    private static final Logger log = LoggerFactory.getLogger(StInventoryServiceImpl.class);

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    /** 流水类型 */
    private static final String TYPE_IN = "IN";
    private static final String TYPE_OUT = "OUT";
    private static final String TYPE_ADJUST = "ADJUST";
    private static final String TYPE_LOSS = "LOSS";

    /** 业务类型 */
    private static final String BIZ_PURCHASE = "PURCHASE";
    private static final String BIZ_CONSUME = "CONSUME";

    /** 流水类型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();

    static {
        TYPE_LABEL_MAP.put("IN", "入库");
        TYPE_LABEL_MAP.put("OUT", "出库");
        TYPE_LABEL_MAP.put("ADJUST", "库存调整");
        TYPE_LABEL_MAP.put("LOSS", "损耗");
    }

    private final StInventoryRecordRepository inventoryRecordRepository;
    private final StMaterialRepository materialRepository;
    private final StPurchaseOrderRepository purchaseOrderRepository;
    private final StPurchaseDetailRepository purchaseDetailRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    @Transactional
    public void stockIn(Long purchaseOrderId) {
        StPurchaseOrder order = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new BusinessException("采购单不存在"));
        List<StPurchaseDetail> details = purchaseDetailRepository.findByPurchaseOrderId(purchaseOrderId);
        if (details.isEmpty()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (StPurchaseDetail detail : details) {
            BigDecimal received = detail.getReceivedQuantity();
            if (received == null || received.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            StMaterial material = materialRepository.findById(detail.getMaterialId())
                    .orElseThrow(() -> new BusinessException("物资不存在（ID：" + detail.getMaterialId() + "）"));
            BigDecimal before = material.getCurrentStock() == null ? BigDecimal.ZERO : material.getCurrentStock();
            BigDecimal after = before.add(received);
            // 更新库存
            material.setCurrentStock(after);
            material.setUpdateTime(now);
            materialRepository.save(material);
            // 记录入库流水
            saveRecord(material, TYPE_IN, received, detail.getUnitPrice(), order.getOrderNo(), BIZ_PURCHASE,
                    before, after, now, order.getPurchaseReason());
        }
    }

    @Override
    @Transactional
    public void stockOut(StockOutRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        for (StockOutItemDTO item : request.getItems()) {
            StMaterial material = materialRepository.findById(item.getMaterialId())
                    .orElseThrow(() -> new BusinessException("物资不存在（ID：" + item.getMaterialId() + "）"));
            BigDecimal before = material.getCurrentStock() == null ? BigDecimal.ZERO : material.getCurrentStock();
            if (before.compareTo(item.getQuantity()) < 0) {
                throw new BusinessException("物资「" + material.getMaterialName() + "」库存不足（当前 " + before + "）");
            }
            BigDecimal after = before.subtract(item.getQuantity());
            // 更新库存
            material.setCurrentStock(after);
            material.setUpdateTime(now);
            materialRepository.save(material);
            // 记录出库流水（数量为负数）
            saveRecord(material, TYPE_OUT, item.getQuantity().negate(), item.getUnitPrice(), null, BIZ_CONSUME,
                    before, after, now, request.getRemark());
        }
    }

    @Override
    @Transactional
    public void adjust(StockAdjustRequest request) {
        StMaterial material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new BusinessException("物资不存在（ID：" + request.getMaterialId() + "）"));
        BigDecimal before = material.getCurrentStock() == null ? BigDecimal.ZERO : material.getCurrentStock();
        BigDecimal newStock = request.getNewStock();
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("调整后库存不能小于0");
        }
        BigDecimal diff = newStock.subtract(before);
        OffsetDateTime now = OffsetDateTime.now();
        material.setCurrentStock(newStock);
        material.setUpdateTime(now);
        materialRepository.save(material);
        // 记录调整流水（正数为盘盈，负数为盘亏）
        saveRecord(material, TYPE_ADJUST, diff, null, null, null, before, newStock, now, request.getRemark());
    }

    @Override
    public PageResult<InventoryRecordVO> queryPage(InventoryPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<StInventoryRecord> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getMaterialId() != null) {
                predicates.add(cb.equal(root.get("materialId"), query.getMaterialId()));
            }
            if (StringUtils.hasText(query.getRecordType())) {
                predicates.add(cb.equal(cb.lower(root.get("recordType")), query.getRecordType().toLowerCase()));
            }
            if (query.getStartTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"),
                        query.getEndTime().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StInventoryRecord> result = inventoryRecordRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        Map<Long, StMaterial> materialMap = loadMaterialMap(result.getContent());
        List<InventoryRecordVO> vos = result.getContent().stream()
                .map(r -> toVO(r, materialMap.get(r.getMaterialId())))
                .collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public List<InventoryRecordVO> getByMaterial(Long materialId) {
        List<StInventoryRecord> records = inventoryRecordRepository.findByMaterialIdOrderByCreateTimeDesc(materialId);
        Map<Long, StMaterial> materialMap = loadMaterialMap(records);
        return records.stream()
                .map(r -> toVO(r, materialMap.get(r.getMaterialId())))
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkStockAlert() {
        List<MaterialVO> alerts = getStockAlerts();
        if (alerts.isEmpty()) {
            log.info("[库存预警] 暂无短缺或积压物资");
            return;
        }
        for (MaterialVO vo : alerts) {
            BigDecimal current = vo.getCurrentStock() == null ? BigDecimal.ZERO : vo.getCurrentStock();
            if (vo.getSafetyStock() != null && current.compareTo(vo.getSafetyStock()) < 0) {
                log.warn("[库存预警-短缺] 物资 {}（{}）当前库存 {} 低于安全库存 {}",
                        vo.getMaterialName(), vo.getMaterialCode(), current, vo.getSafetyStock());
            } else if (vo.getMaxStock() != null && current.compareTo(vo.getMaxStock()) > 0) {
                log.warn("[库存预警-积压] 物资 {}（{}）当前库存 {} 高于库存上限 {}",
                        vo.getMaterialName(), vo.getMaterialCode(), current, vo.getMaxStock());
            }
        }
    }

    @Override
    public List<MaterialVO> getStockAlerts() {
        Specification<StMaterial> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            // 短缺：current < safety（safety 非空）；积压：current > max（max 非空）
            predicates.add(cb.or(
                    cb.and(cb.isNotNull(root.get("safetyStock")),
                            cb.lessThan(root.get("currentStock"), root.get("safetyStock"))),
                    cb.and(cb.isNotNull(root.get("maxStock")),
                            cb.greaterThan(root.get("currentStock"), root.get("maxStock")))));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<StMaterial> materials = materialRepository.findAll(spec);
        return materials.stream().map(this::toMaterialVO).collect(Collectors.toList());
    }

    /**
     * 保存库存流水
     */
    private void saveRecord(StMaterial material, String recordType, BigDecimal quantity, BigDecimal unitPrice,
                            String businessOrderNo, String businessType, BigDecimal before, BigDecimal after,
                            OffsetDateTime now, String remark) {
        StInventoryRecord record = new StInventoryRecord();
        record.setMaterialId(material.getId());
        record.setMaterialCode(material.getMaterialCode());
        record.setMaterialName(material.getMaterialName());
        record.setRecordType(recordType);
        record.setQuantity(quantity);
        record.setUnitPrice(unitPrice);
        record.setTotalAmount(unitPrice == null ? null : quantity.abs().multiply(unitPrice));
        record.setBusinessOrderNo(businessOrderNo);
        record.setBusinessType(businessType);
        record.setStockBefore(before);
        record.setStockAfter(after);
        record.setOperatorId(resolveCurrentUserId());
        record.setOperatorName(currentUserName());
        record.setRemark(remark);
        record.setCreateTime(now);
        inventoryRecordRepository.save(record);
    }

    /**
     * 批量加载物资信息
     */
    private Map<Long, StMaterial> loadMaterialMap(List<StInventoryRecord> records) {
        List<Long> materialIds = records.stream()
                .map(StInventoryRecord::getMaterialId)
                .distinct()
                .collect(Collectors.toList());
        if (materialIds.isEmpty()) {
            return new HashMap<>();
        }
        return materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(StMaterial::getId, m -> m, (a, b) -> a));
    }

    /**
     * 流水转 VO，补充类型中文名与物资信息
     */
    private InventoryRecordVO toVO(StInventoryRecord record, StMaterial material) {
        InventoryRecordVO vo = new InventoryRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setRecordTypeLabel(label(record.getRecordType()));
        if (material != null) {
            vo.setSpec(material.getSpec());
            vo.setUnit(material.getUnit());
        }
        return vo;
    }

    private MaterialVO toMaterialVO(StMaterial material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        return vo;
    }

    private String label(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return TYPE_LABEL_MAP.getOrDefault(value.toUpperCase(), value);
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
