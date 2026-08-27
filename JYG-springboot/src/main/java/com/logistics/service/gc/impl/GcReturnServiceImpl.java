package com.logistics.service.gc.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.gc.ReturnAcceptRequest;
import com.logistics.dto.gc.ReturnApplyRequest;
import com.logistics.dto.gc.ReturnDetailVO;
import com.logistics.dto.gc.ReturnOrderVO;
import com.logistics.dto.gc.ReturnPageQuery;
import com.logistics.entity.GcAssetCard;
import com.logistics.entity.GcBorrowDetail;
import com.logistics.entity.GcBorrowOrder;
import com.logistics.entity.GcReturnOrder;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.GcAssetCardRepository;
import com.logistics.repository.GcBorrowDetailRepository;
import com.logistics.repository.GcBorrowOrderRepository;
import com.logistics.repository.GcReturnOrderRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.gc.GcReturnService;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 公物仓资产归还验收服务实现
 */
@Service
@RequiredArgsConstructor
public class GcReturnServiceImpl implements GcReturnService {

    /** 归还单状态：待验收 */
    private static final String STATUS_PENDING = "PENDING";
    /** 归还单状态：已验收 */
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    /** 归还单状态：验收不通过 */
    private static final String STATUS_REJECTED = "REJECTED";
    /** 归还单状态：需维修 */
    private static final String STATUS_REPAIRING = "REPAIRING";
    /** 验收结果：通过 */
    private static final String ACCEPT_PASS = "PASS";
    /** 验收结果：不通过 */
    private static final String ACCEPT_FAIL = "FAIL";
    /** 验收结果：需维修 */
    private static final String ACCEPT_REPAIR = "REPAIR";
    /** 借用单状态：已通过（可归还） */
    private static final String BORROW_APPROVED = "APPROVED";
    /** 借用单状态：已完成 */
    private static final String BORROW_DONE = "DONE";
    /** 资产状态：在仓 */
    private static final String ASSET_IN_STOCK = "IN_STOCK";
    /** 资产状态：维修中 */
    private static final String ASSET_REPAIRING = "REPAIRING";
    /** 默认归还人（登录体系接入前的兜底用户） */
    private static final Long DEFAULT_USER_ID = 1L;

    private final GcReturnOrderRepository returnOrderRepository;
    private final GcBorrowOrderRepository borrowOrderRepository;
    private final GcBorrowDetailRepository borrowDetailRepository;
    private final GcAssetCardRepository gcAssetCardRepository;
    private final SysUserRepository sysUserRepository;
    private final SysUnitRepository sysUnitRepository;

    @Override
    @Transactional
    public ReturnOrderVO apply(ReturnApplyRequest request) {
        // 校验借用单：状态必须为已通过
        GcBorrowOrder borrowOrder = borrowOrderRepository.findById(request.getBorrowOrderId())
                .orElseThrow(() -> new BusinessException("借用单不存在"));
        if (!BORROW_APPROVED.equals(borrowOrder.getOrderStatus())) {
            throw new BusinessException("借用单当前状态不可发起归还");
        }

        // 校验归还资产属于该借用单（为空表示全部归还）
        List<GcBorrowDetail> borrowDetails = borrowDetailRepository.findByBorrowOrderId(request.getBorrowOrderId());
        if (request.getAssetIds() != null && !request.getAssetIds().isEmpty()) {
            Set<Long> ownedAssetIds = borrowDetails.stream()
                    .map(GcBorrowDetail::getAssetId).collect(Collectors.toSet());
            for (Long assetId : request.getAssetIds()) {
                if (!ownedAssetIds.contains(assetId)) {
                    throw new BusinessException("资产[" + assetId + "]不属于该借用单");
                }
            }
        }

        // 归还申请人：从当前登录用户获取（登录体系未接入时兜底为默认用户）
        SysUser user = resolveCurrentUser();
        if (user == null) {
            user = sysUserRepository.findById(DEFAULT_USER_ID).orElse(null);
        }
        if (user == null) {
            throw new BusinessException("未找到归还申请人");
        }

        OffsetDateTime now = OffsetDateTime.now();
        GcReturnOrder order = new GcReturnOrder();
        order.setReturnNo(generateReturnNo());
        order.setBorrowOrderId(request.getBorrowOrderId());
        order.setReturnApplicantId(user.getId());
        order.setReturnApplicantUnitId(user.getUnitId() != null ? user.getUnitId() : DEFAULT_USER_ID);
        order.setPlanReturnTime(toOffsetDateTime(request.getPlanReturnTime()));
        order.setReturnStatus(STATUS_PENDING);
        order.setIsDeleted(false);
        order.setCreateTime(now);
        order.setUpdateTime(now);

        GcReturnOrder saved = returnOrderRepository.save(order);
        return toVO(saved);
    }

    @Override
    @Transactional
    public void accept(ReturnAcceptRequest request) {
        GcReturnOrder order = returnOrderRepository.findById(request.getReturnOrderId())
                .orElseThrow(() -> new BusinessException("归还单不存在"));
        if (!STATUS_PENDING.equals(order.getReturnStatus())) {
            throw new BusinessException("当前状态不可验收");
        }

        String result = request.getAcceptResult();
        if (!ACCEPT_PASS.equals(result) && !ACCEPT_FAIL.equals(result) && !ACCEPT_REPAIR.equals(result)) {
            throw new BusinessException("验收结果无效，只能为 PASS、FAIL 或 REPAIR");
        }

        OffsetDateTime now = OffsetDateTime.now();
        // 记录验收信息
        order.setAcceptResult(result);
        order.setAcceptRemark(request.getAcceptRemark());
        order.setAcceptTime(now);
        order.setActualReturnTime(now);
        order.setAcceptUserIds(currentUserName());
        order.setAcceptPhotos(request.getAcceptPhotos() == null ? null : String.join(",", request.getAcceptPhotos()));
        order.setDamageInfo(request.getDamageInfo());
        order.setDamageResponsibility(request.getDamageResponsibility());
        order.setRepairCost(request.getRepairCost());
        order.setCompensationAmount(request.getCompensationAmount());
        order.setUpdateTime(now);

        List<GcBorrowDetail> details = borrowDetailRepository.findByBorrowOrderId(order.getBorrowOrderId());
        if (ACCEPT_PASS.equals(result)) {
            // 验收通过：资产恢复在仓，借用单完成
            order.setReturnStatus(STATUS_ACCEPTED);
            updateBorrowStatus(order.getBorrowOrderId(), BORROW_DONE);
            updateAssetsStatus(details, ASSET_IN_STOCK, now);
        } else if (ACCEPT_REPAIR.equals(result)) {
            // 需维修：归还单转维修中，资产转维修中，借用单完成
            order.setReturnStatus(STATUS_REPAIRING);
            updateBorrowStatus(order.getBorrowOrderId(), BORROW_DONE);
            updateAssetsStatus(details, ASSET_REPAIRING, now);
        } else {
            // 验收不通过：归还单驳回，借用单与资产状态保持不变
            order.setReturnStatus(STATUS_REJECTED);
        }

        returnOrderRepository.save(order);
    }

    @Override
    public PageResult<ReturnOrderVO> queryPage(ReturnPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<GcReturnOrder> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 排除已删除
            predicates.add(cb.or(cb.isFalse(root.get("isDeleted")), cb.isNull(root.get("isDeleted"))));
            if (StringUtils.hasText(query.getReturnNo())) {
                predicates.add(cb.like(root.get("returnNo"), "%" + query.getReturnNo().trim() + "%"));
            }
            if (query.getBorrowOrderId() != null) {
                predicates.add(cb.equal(root.get("borrowOrderId"), query.getBorrowOrderId()));
            }
            if (StringUtils.hasText(query.getReturnStatus())) {
                predicates.add(cb.equal(root.get("returnStatus"), query.getReturnStatus()));
            }
            if (query.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"),
                        query.getStartDate().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            if (query.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"),
                        query.getEndDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toOffsetDateTime()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<GcReturnOrder> result = returnOrderRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        List<ReturnOrderVO> vos = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public ReturnOrderVO getDetail(Long id) {
        GcReturnOrder order = returnOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("归还单不存在"));
        return toVO(order);
    }

    /**
     * 生成归还单编号：RG + 年月 + 4位序号，如 RG2026080001
     */
    private String generateReturnNo() {
        String prefix = "RG" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = returnOrderRepository.countByReturnNoStartingWith(prefix);
        return prefix + String.format("%04d", count + 1);
    }

    /**
     * 归还单转 VO，补充借用单号、申请人姓名/单位名称、资产明细
     */
    private ReturnOrderVO toVO(GcReturnOrder order) {
        ReturnOrderVO vo = new ReturnOrderVO();
        BeanUtils.copyProperties(order, vo);
        GcBorrowOrder borrowOrder = borrowOrderRepository.findById(order.getBorrowOrderId()).orElse(null);
        if (borrowOrder != null) {
            vo.setBorrowOrderNo(borrowOrder.getOrderNo());
        }
        vo.setApplicantName(resolveUserName(order.getReturnApplicantId(), borrowOrder == null ? null : borrowOrder.getApplicantName()));
        vo.setApplicantUnitName(resolveUnitName(order.getReturnApplicantUnitId(), borrowOrder == null ? null : borrowOrder.getApplicantUnitName()));
        List<GcBorrowDetail> details = borrowDetailRepository.findByBorrowOrderId(order.getBorrowOrderId());
        vo.setAssetCount(details.size());
        vo.setDetailList(details.stream().map(this::toDetailVO).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 借用明细转归还明细 VO，补充规格型号与归还数量
     */
    private ReturnDetailVO toDetailVO(GcBorrowDetail detail) {
        ReturnDetailVO vo = new ReturnDetailVO();
        vo.setAssetId(detail.getAssetId());
        vo.setAssetCode(detail.getAssetCode());
        vo.setAssetName(detail.getAssetName());
        vo.setReturnQuantity(detail.getBorrowQuantity());
        vo.setSpecModel(gcAssetCardRepository.findById(detail.getAssetId())
                .map(GcAssetCard::getSpecModel).orElse(null));
        return vo;
    }

    /**
     * 更新借用单状态
     */
    private void updateBorrowStatus(Long borrowOrderId, String status) {
        GcBorrowOrder borrowOrder = borrowOrderRepository.findById(borrowOrderId)
                .orElseThrow(() -> new BusinessException("借用单不存在"));
        borrowOrder.setOrderStatus(status);
        borrowOrder.setUpdateTime(OffsetDateTime.now());
        borrowOrderRepository.save(borrowOrder);
    }

    /**
     * 批量更新资产状态
     */
    private void updateAssetsStatus(List<GcBorrowDetail> details, String status, OffsetDateTime now) {
        List<Long> assetIds = details.stream().map(GcBorrowDetail::getAssetId).collect(Collectors.toList());
        List<GcAssetCard> assets = gcAssetCardRepository.findAllById(assetIds);
        for (GcAssetCard asset : assets) {
            asset.setAssetStatus(status);
            asset.setUpdateTime(now);
        }
        gcAssetCardRepository.saveAll(assets);
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    private String resolveUserName(Long userId, String fallback) {
        if (userId != null) {
            String name = sysUserRepository.findById(userId).map(SysUser::getRealName).orElse(null);
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return fallback;
    }

    private String resolveUnitName(Long unitId, String fallback) {
        if (unitId != null) {
            String name = sysUnitRepository.findById(unitId).map(SysUnit::getUnitName).orElse(null);
            if (StringUtils.hasText(name)) {
                return name;
            }
        }
        return fallback;
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户
     */
    private SysUser resolveCurrentUser() {
        String username = currentUserName();
        if (StringUtils.hasText(username)) {
            return sysUserRepository.findByUsername(username).orElse(null);
        }
        return null;
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
