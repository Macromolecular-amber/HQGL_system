package com.logistics.service.pay.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.PageResult;
import com.logistics.dto.pay.AccountVO;
import com.logistics.dto.pay.ConsumeRequest;
import com.logistics.dto.pay.RechargeRequest;
import com.logistics.dto.pay.TransactionPageQuery;
import com.logistics.dto.pay.TransactionVO;
import com.logistics.entity.PayAccount;
import com.logistics.entity.PayTransaction;
import com.logistics.entity.SysUser;
import com.logistics.repository.PayAccountRepository;
import com.logistics.repository.PayTransactionRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.service.pay.PayService;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 食堂餐卡支付服务实现
 */
@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

    /** 账户类型：餐卡 */
    private static final String ACCOUNT_TYPE_MEAL_CARD = "MEAL_CARD";

    /** 卡状态/账户状态 */
    private static final String CARD_STATUS_NORMAL = "NORMAL";
    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";

    /** 支付方式 */
    private static final String PAY_METHOD_MEAL_CARD = "MEAL_CARD";

    /** 支付状态 */
    private static final String PAY_STATUS_SUCCESS = "SUCCESS";

    /** 交易类型 */
    private static final String TYPE_RECHARGE = "RECHARGE";
    private static final String TYPE_CONSUME = "CONSUME";
    private static final String TYPE_REFUND = "REFUND";

    /** 交易类型中文名 */
    private static final Map<String, String> TYPE_LABEL_MAP = new HashMap<>();

    /** 支付方式中文名 */
    private static final Map<String, String> METHOD_LABEL_MAP = new HashMap<>();

    static {
        TYPE_LABEL_MAP.put("RECHARGE", "充值");
        TYPE_LABEL_MAP.put("CONSUME", "消费");
        TYPE_LABEL_MAP.put("REFUND", "退款");
        METHOD_LABEL_MAP.put("MEAL_CARD", "餐卡");
        METHOD_LABEL_MAP.put("FACE", "刷脸");
        METHOD_LABEL_MAP.put("BALANCE", "余额");
    }

    /** 交易号格式 */
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final Random RANDOM = new Random();

    private final PayAccountRepository payAccountRepository;
    private final PayTransactionRepository payTransactionRepository;
    private final SysUserRepository sysUserRepository;

    @Override
    public AccountVO getAccount(Long userId) {
        PayAccount account = getOrCreateAccount(userId);
        AccountVO vo = new AccountVO();
        vo.setUserId(account.getUserId());
        vo.setAccountType(account.getAccountType());
        vo.setBalance(account.getBalance());
        vo.setCardNo(account.getCardNo());
        vo.setCardStatus(account.getCardStatus());
        vo.setUserName(resolveUserName(userId));
        return vo;
    }

    @Override
    @Transactional
    public void recharge(RechargeRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        PayAccount account = getOrCreateAccount(request.getUserId());
        BigDecimal before = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        BigDecimal after = before.add(request.getAmount());
        account.setBalance(after);
        account.setLastTransactionTime(OffsetDateTime.now());
        account.setUpdateTime(OffsetDateTime.now());
        payAccountRepository.save(account);
        // 生成充值流水（金额为正）
        saveTransaction(request.getUserId(), account, TYPE_RECHARGE, request.getAmount(), before, after,
                null, request.getRemark());
    }

    @Override
    @Transactional
    public void consume(ConsumeRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("消费金额必须大于0");
        }
        PayAccount account = getOrCreateAccount(request.getUserId());
        BigDecimal before = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        if (before.compareTo(request.getAmount()) < 0) {
            throw new BusinessException("餐卡余额不足");
        }
        BigDecimal after = before.subtract(request.getAmount());
        account.setBalance(after);
        account.setLastTransactionTime(OffsetDateTime.now());
        account.setUpdateTime(OffsetDateTime.now());
        payAccountRepository.save(account);
        // 生成消费流水（金额为负），记录业务单号
        saveTransaction(request.getUserId(), account, TYPE_CONSUME, request.getAmount().negate(), before, after,
                request.getBizOrderNo(), request.getRemark());
    }

    @Override
    @Transactional
    public void refund(Long transactionId, String remark) {
        PayTransaction original = payTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("原交易记录不存在"));
        if (!TYPE_CONSUME.equalsIgnoreCase(original.getTransactionType())) {
            throw new BusinessException("仅消费交易可退款");
        }
        if (!PAY_STATUS_SUCCESS.equalsIgnoreCase(original.getPayStatus())) {
            throw new BusinessException("仅支付成功的交易可退款");
        }
        // 退款金额不能超过原消费金额：原金额（负）+ 退款金额（正）≤ 0
        BigDecimal refundAmount = original.getAmount().abs();
        if (original.getAmount().add(refundAmount).compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("退款金额不能超过原消费金额");
        }
        PayAccount account = getOrCreateAccount(original.getUserId());
        BigDecimal before = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();
        BigDecimal after = before.add(refundAmount);
        account.setBalance(after);
        account.setLastTransactionTime(OffsetDateTime.now());
        account.setUpdateTime(OffsetDateTime.now());
        payAccountRepository.save(account);
        // 生成退款流水（金额为正）
        String bizOrderNo = original.getBizOrderNo();
        String refundRemark = StringUtils.hasText(remark)
                ? remark + "（原交易号：" + original.getTransactionNo() + "）"
                : "原交易号：" + original.getTransactionNo();
        saveTransaction(original.getUserId(), account, TYPE_REFUND, refundAmount, before, after,
                bizOrderNo, refundRemark);
    }

    @Override
    public PageResult<TransactionVO> queryTransactions(TransactionPageQuery query) {
        int page = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int size = (query.getSize() == null || query.getSize() < 1) ? 20 : query.getSize();

        Specification<PayTransaction> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), query.getUserId()));
            }
            if (StringUtils.hasText(query.getTransactionType())) {
                predicates.add(cb.equal(cb.lower(root.get("transactionType")), query.getTransactionType().toLowerCase()));
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

        Page<PayTransaction> result = payTransactionRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
        Map<Long, String> userNameMap = loadUserNames(result.getContent());
        List<TransactionVO> vos = result.getContent().stream()
                .map(t -> toVO(t, userNameMap.get(t.getUserId())))
                .collect(Collectors.toList());
        return PageResult.of(result, vos);
    }

    @Override
    public List<TransactionVO> getByBizOrderNo(String bizOrderNo) {
        List<PayTransaction> transactions = payTransactionRepository.findByBizOrderNo(bizOrderNo);
        Map<Long, String> userNameMap = loadUserNames(transactions);
        return transactions.stream()
                .map(t -> toVO(t, userNameMap.get(t.getUserId())))
                .collect(Collectors.toList());
    }

    /**
     * 获取账户，不存在则自动创建（首次使用餐卡）
     */
    private PayAccount getOrCreateAccount(Long userId) {
        PayAccount account = payAccountRepository.findByUserIdAndIsDeleted(userId, false).orElse(null);
        if (account == null) {
            OffsetDateTime now = OffsetDateTime.now();
            account = new PayAccount();
            account.setUserId(userId);
            account.setAccountType(ACCOUNT_TYPE_MEAL_CARD);
            account.setAccountNo(generateAccountNo());
            account.setBalance(BigDecimal.ZERO);
            account.setFrozenAmount(BigDecimal.ZERO);
            account.setCardNo(generateCardNo());
            account.setCardStatus(CARD_STATUS_NORMAL);
            account.setAccountStatus(ACCOUNT_STATUS_ACTIVE);
            account.setIsDeleted(false);
            account.setCreateTime(now);
            account.setUpdateTime(now);
            account = payAccountRepository.save(account);
        }
        return account;
    }

    /**
     * 保存交易流水
     */
    private void saveTransaction(Long userId, PayAccount account, String type, BigDecimal amount,
                                 BigDecimal before, BigDecimal after, String bizOrderNo, String remark) {
        OffsetDateTime now = OffsetDateTime.now();
        PayTransaction transaction = new PayTransaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setUserId(userId);
        transaction.setAccountType(account.getAccountType());
        transaction.setAccountNo(account.getAccountNo());
        transaction.setCardNo(account.getCardNo());
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(before);
        transaction.setBalanceAfter(after);
        transaction.setPayMethod(PAY_METHOD_MEAL_CARD);
        transaction.setPayStatus(PAY_STATUS_SUCCESS);
        transaction.setBizModule("MEAL");
        transaction.setBizOrderNo(bizOrderNo);
        transaction.setRemark(remark);
        transaction.setPayTime(now);
        transaction.setCreateTime(now);
        payTransactionRepository.save(transaction);
    }

    /**
     * 生成餐卡号：CARD + 时间戳后10位 + 2位随机
     */
    private String generateCardNo() {
        String ts = String.valueOf(System.currentTimeMillis());
        return "CARD" + ts.substring(ts.length() - 10) + String.format("%02d", RANDOM.nextInt(100));
    }

    /**
     * 生成账户号：ACC + 时间戳后10位 + 2位随机
     */
    private String generateAccountNo() {
        String ts = String.valueOf(System.currentTimeMillis());
        return "ACC" + ts.substring(ts.length() - 10) + String.format("%02d", RANDOM.nextInt(100));
    }

    /**
     * 生成交易流水号：TRX + yyyyMMddHHmmss + 4位随机
     */
    private String generateTransactionNo() {
        return "TRX" + LocalDateTime.now().format(NO_FORMATTER) + String.format("%04d", RANDOM.nextInt(10000));
    }

    /**
     * 批量加载用户名
     */
    private Map<Long, String> loadUserNames(List<PayTransaction> transactions) {
        List<Long> userIds = transactions.stream()
                .map(PayTransaction::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> map = new HashMap<>();
        if (userIds.isEmpty()) {
            return map;
        }
        for (SysUser user : sysUserRepository.findAllById(userIds)) {
            map.put(user.getId(), StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        }
        return map;
    }

    private String resolveUserName(Long userId) {
        return sysUserRepository.findById(userId)
                .map(u -> StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername())
                .orElse(null);
    }

    /**
     * 交易转 VO，补充中文名
     */
    private TransactionVO toVO(PayTransaction transaction, String userName) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);
        vo.setTransactionTypeLabel(label(TYPE_LABEL_MAP, transaction.getTransactionType()));
        vo.setPayMethodLabel(label(METHOD_LABEL_MAP, transaction.getPayMethod()));
        vo.setUserName(userName);
        return vo;
    }

    private String label(Map<String, String> map, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return map.getOrDefault(value.toUpperCase(), value);
    }
}
