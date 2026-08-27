package com.logistics.service.st.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.st.PurchaseAcceptRequest;
import com.logistics.dto.st.PurchaseApplyRequest;
import com.logistics.dto.st.PurchaseAuditRequest;
import com.logistics.dto.st.PurchaseDetailVO;
import com.logistics.dto.st.PurchaseItemDTO;
import com.logistics.dto.st.PurchaseOrderVO;
import com.logistics.dto.st.PurchasePageQuery;
import com.logistics.dto.st.ReceivedItemDTO;
import com.logistics.entity.StMaterial;
import com.logistics.entity.StPurchaseDetail;
import com.logistics.entity.StPurchaseOrder;
import com.logistics.entity.SysUser;
import com.logistics.repository.StMaterialRepository;
import com.logistics.repository.StPurchaseDetailRepository;
import com.logistics.repository.StPurchaseOrderRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.MessageNotifier;
import com.logistics.service.st.StInventoryService;
import com.logistics.service.st.StPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 食堂采购供应链管理服务实现
 */
@Service
@RequiredArgsConstructor
public class StPurchaseServiceImpl implements StPurchaseService {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    /** 单据状态 */
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_EXPIRED = "EXPIRED";

    /** 审批/验收结果 */
    private static final String RESULT_PASS = "PASS";
    private static final String RESULT_FAIL = "FAIL";

    /** 单据状态中文名 */
    private static final Map<String, String> STATUS_LABEL_MAP = new HashMap<>();

    static {
        STATUS_LABEL_MAP.put("DRAFT", "草稿");
        STATUS_LABEL_MAP.put("PENDING", "待审批");
        STATUS_LABEL_MAP.put("APPROVED", "待验收");
        STATUS_LABEL_MAP.put("REJECTED", "已驳回");
        STATUS_LABEL_MAP.put("COMPLETED", "已完成");
        STATUS_LABEL_MAP.put("RECEIVED", "已收货");
        STATUS_LABEL_MAP.put("EXPIRED", "已过期");
    }

    /** 编号日期格式：yyyyMM */
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final StPurchaseOrderRepository purchaseOrderRepository;
    private final StPurchaseDetailRepository purchaseDetailRepository;
    private final StMaterialRepository materialRepository;
    private final SysUserRepository sysUserRepository;
    private final StInventoryService stInventoryService;
    private final MessageNotifier messageNotifier;

    @Override
    @Transactional
    public PurchaseOrderVO apply(PurchaseApplyRequest request) {
        List<PurchaseItemDTO> items = request.getItems();
        // 校验物资存在
        List<Long> materialIds = items.stream().map(PurchaseItemDTO::getMaterialId).distinct().collect(Collectors.toList());
        Map<Long, StMaterial> materialMap = materialRepository.findAllById(materialIds).stream()
                .collect(Collectors.toMap(StMaterial::getId, m -> m, (a, b) -> a));
        for (Long materialId : materialIds) {
            if (!materialMap.containsKey(materialId)) {
                throw new BusinessException("物资不存在（ID：" + materialId + "）");
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        StPurchaseOrder order = new StPurchaseOrder();
        order.setOrderNo(generateOrderNo(now));
        order.setPurchaseReason(request.getPurchaseReason());
        order.setSupplierId(request.getSupplierId());
        order.setSupplierName(request.getSupplierName());
        order.setMaterialCount(items.size());
        // 总金额 = Σ(数量 × 单价)
        BigDecimal totalAmount = items.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        // 生效时间：当前时间 ~ 当前时间 + 2天
        order.setEffectiveStart(now);
        order.setEffectiveEnd(now.plusDays(2));
        order.setOrderStatus(STATUS_DRAFT);
        order.setIsExpired(false);
        order.setIsDeleted(false);
        order.setCreateBy(resolveCurrentUserId());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order = purchaseOrderRepository.save(order);

        // 保存采购明细
        for (PurchaseItemDTO item : items) {
            StMaterial material = materialMap.get(item.getMaterialId());
            StPurchaseDetail detail = new StPurchaseDetail();
            detail.setPurchaseOrderId(order.getId());
            detail.setMaterialId(material.getId());
            detail.setMaterialCode(material.getMaterialCode());
            detail.setMaterialName(material.getMaterialName());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());
            detail.setSubtotal(item.getQuantity().multiply(item.getUnitPrice()));
            detail.setCreateTime(now);
            detail.setUpdateTime(now);
            purchaseDetailRepository.save(detail);
        }
        // 通知食堂管理员审批
        messageNotifier.notifyRoles("您有新的采购申请待审批", "st-purchase", order.getOrderNo(), "BIZ_ADMIN", "WAREHOUSE");
        return toVO(order);
    }

    @Override
    @Transactional
    public void audit(PurchaseAuditRequest request) {
        StPurchaseOrder order = getOrder(request.getOrderId());
        if (!STATUS_DRAFT.equalsIgnoreCase(order.getOrderStatus())) {
            throw new BusinessException("仅草稿状态的采购单可审批");
        }
        String result = request.getAuditResult() == null ? null : request.getAuditResult().toUpperCase();
        OffsetDateTime now = OffsetDateTime.now();
        order.setAuditUserId(resolveCurrentUserId());
        order.setAuditUserName(currentUserName());
        order.setAuditTime(now);
        order.setAuditRemark(request.getAuditRemark());
        if (RESULT_PASS.equals(result)) {
            order.setOrderStatus(STATUS_APPROVED);
        } else if (RESULT_FAIL.equals(result) || "REJECT".equals(result)) {
            order.setOrderStatus(STATUS_REJECTED);
        } else {
            throw new BusinessException("审批结果无效");
        }
        order.setUpdateTime(now);
        purchaseOrderRepository.save(order);
        // 通知申请人审批结果
        String resultTitle = RESULT_PASS.equals(result) ? "您的采购申请已审核通过" : "您的采购申请已驳回";
        messageNotifier.notifyUser(resultTitle, "st-purchase", order.getOrderNo(), order.getCreateBy());
    }

    @Override
    @Transactional
    public void accept(PurchaseAcceptRequest request) {
        StPurchaseOrder order = getOrder(request.getOrderId());
        if (!STATUS_APPROVED.equalsIgnoreCase(order.getOrderStatus())) {
            throw new BusinessException("仅待验收状态的采购单可验收");
        }
        String result = request.getAcceptResult() == null ? null : request.getAcceptResult().toUpperCase();
        OffsetDateTime now = OffsetDateTime.now();
        if (RESULT_PASS.equals(result)) {
            // 验收通过：更新明细实际收货数量
            List<StPurchaseDetail> details = purchaseDetailRepository.findByPurchaseOrderId(order.getId());
            Map<Long, BigDecimal> receivedMap = new HashMap<>();
            if (request.getReceivedItems() != null) {
                for (ReceivedItemDTO ri : request.getReceivedItems()) {
                    receivedMap.put(ri.getDetailId(), ri.getReceivedQuantity());
                }
            }
            for (StPurchaseDetail detail : details) {
                BigDecimal received = receivedMap.get(detail.getId());
                if (received == null) {
                    received = BigDecimal.ZERO;
                }
                if (received.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("验收数量不能小于0");
                }
                detail.setReceivedQuantity(received);
                detail.setReceiveTime(now);
                detail.setUpdateTime(now);
                purchaseDetailRepository.save(detail);
            }
            order.setOrderStatus(STATUS_COMPLETED);
            order.setAcceptStatus(RESULT_PASS);
            // 验收通过后自动入库（更新库存并记录入库流水）
            stInventoryService.stockIn(order.getId());
        } else if (RESULT_FAIL.equals(result)) {
            order.setOrderStatus(STATUS_REJECTED);
            order.setAcceptStatus(RESULT_FAIL);
        } else {
            throw new BusinessException("验收结果无效");
        }
        order.setAcceptUsers(currentUserName());
        order.setAcceptTime(now);
        order.setAcceptRemark(request.getAcceptRemark());
        order.setUpdateTime(now);
        purchaseOrderRepository.save(order);
    }

    @Override
    public PageResult<PurchaseOrderVO> queryPage(PurchasePageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<StPurchaseOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除
            predicates.add(cb.isFalse(root.get("isDeleted")));
            if (StringUtils.hasText(query.getOrderNo())) {
                predicates.add(cb.like(root.get("orderNo"), "%" + query.getOrderNo().trim() + "%"));
            }
            if (StringUtils.hasText(query.getOrderStatus())) {
                predicates.add(cb.equal(cb.lower(root.get("orderStatus")), query.getOrderStatus().toLowerCase()));
            }
            if (StringUtils.hasText(query.getSupplierName())) {
                predicates.add(cb.like(root.get("supplierName"), "%" + query.getSupplierName().trim() + "%"));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartDate().atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"),
                        query.getEndDate().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<StPurchaseOrder> result = purchaseOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<PurchaseOrderVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public PurchaseOrderVO getDetail(Long id) {
        return toVO(getOrder(id));
    }

    /**
     * 定时任务：检查并处理过期单据（启动1分钟后首次执行，之后每小时）
     */
    @Override
    @Transactional
    @Scheduled(initialDelay = 60000, fixedDelay = 3600000)
    public void checkExpiredOrders() {
        OffsetDateTime now = OffsetDateTime.now();
        Specification<StPurchaseOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));
            predicates.add(cb.isFalse(root.get("isExpired")));
            predicates.add(cb.equal(cb.lower(root.get("orderStatus")), STATUS_DRAFT.toLowerCase()));
            predicates.add(cb.lessThan(root.get("effectiveEnd"), now));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<StPurchaseOrder> expiredOrders = purchaseOrderRepository.findAll(spec);
        for (StPurchaseOrder order : expiredOrders) {
            order.setIsExpired(true);
            order.setOrderStatus(STATUS_EXPIRED);
            order.setUpdateTime(now);
            purchaseOrderRepository.save(order);
        }
    }

    /**
     * 生成采购单编号：CG + yyyyMM + 4位序号（如 CG2026080001）
     */
    private String generateOrderNo(OffsetDateTime now) {
        String prefix = "CG" + now.format(MONTH_FORMATTER);
        String seq = "0001";
        StPurchaseOrder last = purchaseOrderRepository
                .findTopByOrderNoStartingWithOrderByOrderNoDesc(prefix)
                .orElse(null);
        if (last != null && StringUtils.hasText(last.getOrderNo()) && last.getOrderNo().length() >= prefix.length() + 4) {
            try {
                int lastSeq = Integer.parseInt(last.getOrderNo().substring(prefix.length()));
                seq = String.format("%04d", lastSeq + 1);
            } catch (NumberFormatException ignored) {
                // 序号解析失败时回退默认 0001
            }
        }
        return prefix + seq;
    }

    private StPurchaseOrder getOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采购单不存在"));
    }

    /**
     * 采购单转 VO，补充明细与状态中文名
     */
    private PurchaseOrderVO toVO(StPurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusLabel(label(order.getOrderStatus()));
        List<StPurchaseDetail> details = purchaseDetailRepository.findByPurchaseOrderId(order.getId());
        Map<Long, StMaterial> materialMap = new HashMap<>();
        List<Long> materialIds = details.stream().map(StPurchaseDetail::getMaterialId).distinct().collect(Collectors.toList());
        if (!materialIds.isEmpty()) {
            materialMap = materialRepository.findAllById(materialIds).stream()
                    .collect(Collectors.toMap(StMaterial::getId, m -> m, (a, b) -> a));
        }
        Map<Long, StMaterial> finalMap = materialMap;
        vo.setItems(details.stream().map(d -> toDetailVO(d, finalMap.get(d.getMaterialId()))).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 明细转 VO，补充物资规格/单位
     */
    private PurchaseDetailVO toDetailVO(StPurchaseDetail detail, StMaterial material) {
        PurchaseDetailVO vo = new PurchaseDetailVO();
        BeanUtils.copyProperties(detail, vo);
        if (material != null) {
            vo.setSpec(material.getSpec());
            vo.setUnit(material.getUnit());
        }
        return vo;
    }

    private String label(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return STATUS_LABEL_MAP.getOrDefault(value.toUpperCase(), value);
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
