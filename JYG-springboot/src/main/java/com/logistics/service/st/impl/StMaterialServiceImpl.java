package com.logistics.service.st.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.st.MaterialPageQuery;
import com.logistics.dto.st.MaterialSaveRequest;
import com.logistics.dto.st.MaterialVO;
import com.logistics.entity.StMaterial;
import com.logistics.entity.StPurchaseDetail;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.repository.StMaterialRepository;
import com.logistics.repository.StPurchaseDetailRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.service.st.StMaterialService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食堂物资档案管理服务实现
 */
@Service
@RequiredArgsConstructor
public class StMaterialServiceImpl implements StMaterialService {

    /** 采购单完成状态（已收货即完成） */
    private static final String PURCHASE_STATUS_RECEIVED = "RECEIVED";

    /** 分类中文名 */
    private static final Map<String, String> CATEGORY_LABEL_MAP = new HashMap<>();

    static {
        CATEGORY_LABEL_MAP.put("FRESH_INGREDIENTS", "生鲜食材");
        CATEGORY_LABEL_MAP.put("CONDIMENT", "调味品");
        CATEGORY_LABEL_MAP.put("DAILY_GOODS", "日用品");
    }

    private final StMaterialRepository materialRepository;
    private final StPurchaseDetailRepository purchaseDetailRepository;
    private final StPurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional
    public MaterialVO save(MaterialSaveRequest request) {
        // 分类白名单校验
        String category = request.getCategory() == null ? null : request.getCategory().toUpperCase();
        if (!CATEGORY_LABEL_MAP.containsKey(category)) {
            throw new BusinessException("物资分类无效");
        }
        // 物资编码唯一性校验（编辑时排除自身，仅校验未删除记录）
        String materialCode = request.getMaterialCode().trim();
        List<StMaterial> exists = materialRepository.findByMaterialCodeAndIsDeleted(materialCode, false);
        boolean duplicate = exists.stream()
                .anyMatch(m -> request.getId() == null || !request.getId().equals(m.getId()));
        if (duplicate) {
            throw new BusinessException("物资编码已存在");
        }
        // 库存上下限非负校验
        if (request.getSafetyStock() != null && request.getSafetyStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("安全库存不能小于0");
        }
        if (request.getMaxStock() != null && request.getMaxStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("库存上限不能小于0");
        }

        OffsetDateTime now = OffsetDateTime.now();
        StMaterial material;
        if (request.getId() != null) {
            material = materialRepository.findById(request.getId())
                    .orElseThrow(() -> new BusinessException("物资不存在"));
        } else {
            material = new StMaterial();
            material.setIsDeleted(false);
            material.setCreateTime(now);
            // 新增默认库存与占用库存为 0
            material.setCurrentStock(BigDecimal.ZERO);
            material.setOccupiedStock(BigDecimal.ZERO);
            // 默认值：安全库存 0、库存上限 1000
            material.setSafetyStock(BigDecimal.ZERO);
            material.setMaxStock(new BigDecimal("1000"));
        }
        // 库存下限不能大于库存上限
        if (material.getMaxStock() != null && material.getSafetyStock() != null
                && material.getSafetyStock().compareTo(material.getMaxStock()) > 0) {
            throw new BusinessException("安全库存不能大于库存上限");
        }
        // 当前库存非负校验（编辑时保留原库存）
        if (material.getCurrentStock() != null && material.getCurrentStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("当前库存不能小于0");
        }

        material.setMaterialCode(materialCode);
        material.setMaterialName(request.getMaterialName().trim());
        material.setCategory(category);
        material.setSpec(request.getSpec());
        material.setUnit(request.getUnit().trim());
        material.setShelfLife(request.getShelfLife());
        if (request.getSafetyStock() != null) {
            material.setSafetyStock(request.getSafetyStock());
        }
        if (request.getMaxStock() != null) {
            material.setMaxStock(request.getMaxStock());
        }
        material.setCurrentPrice(request.getCurrentPrice());
        material.setRemark(request.getRemark());
        material.setUpdateTime(now);
        return toVO(materialRepository.save(material));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物资不存在"));
        // 校验是否有未完成的采购单引用该物资
        List<StPurchaseDetail> details = purchaseDetailRepository.findByMaterialId(id);
        if (!details.isEmpty()) {
            List<Long> orderIds = details.stream()
                    .map(StPurchaseDetail::getPurchaseOrderId)
                    .distinct()
                    .collect(Collectors.toList());
            List<StPurchaseOrder> orders = purchaseOrderRepository.findByIdIn(orderIds);
            boolean hasUnfinished = orders.stream()
                    .filter(o -> !Boolean.TRUE.equals(o.getIsDeleted()))
                    .anyMatch(o -> !PURCHASE_STATUS_RECEIVED.equalsIgnoreCase(o.getOrderStatus()));
            if (hasUnfinished) {
                throw new BusinessException("存在未完成的采购单引用该物资，不可删除");
            }
        }
        material.setIsDeleted(true);
        material.setUpdateTime(OffsetDateTime.now());
        materialRepository.save(material);
    }

    @Override
    public PageResult<MaterialVO> queryPage(MaterialPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<StMaterial> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除
            predicates.add(cb.isFalse(root.get("isDeleted")));
            if (StringUtils.hasText(query.getMaterialCode())) {
                predicates.add(cb.like(root.get("materialCode"), "%" + query.getMaterialCode().trim() + "%"));
            }
            if (StringUtils.hasText(query.getMaterialName())) {
                predicates.add(cb.like(root.get("materialName"), "%" + query.getMaterialName().trim() + "%"));
            }
            if (StringUtils.hasText(query.getCategory())) {
                predicates.add(cb.equal(cb.lower(root.get("category")), query.getCategory().toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StMaterial> result = materialRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MaterialVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public MaterialVO getDetail(Long id) {
        StMaterial material = materialRepository.findById(id)
                .orElseThrow(() -> new BusinessException("物资不存在"));
        return toVO(material);
    }

    @Override
    public List<MaterialVO> getByCategory(String category) {
        Specification<StMaterial> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<StMaterial> materials = materialRepository.findAll(spec,
                Sort.by(Sort.Direction.ASC, "materialCode"));
        return materials.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 物资转 VO，补充分类中文名
     */
    private MaterialVO toVO(StMaterial material) {
        MaterialVO vo = new MaterialVO();
        BeanUtils.copyProperties(material, vo);
        vo.setCategoryLabel(label(material.getCategory()));
        return vo;
    }

    private String label(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return CATEGORY_LABEL_MAP.getOrDefault(value.toUpperCase(), value);
    }
}
