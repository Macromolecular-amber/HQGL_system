package com.logistics.service.pay;

import com.logistics.common.PageResult;
import com.logistics.dto.pay.AccountVO;
import com.logistics.dto.pay.ConsumeRequest;
import com.logistics.dto.pay.RechargeRequest;
import com.logistics.dto.pay.TransactionPageQuery;
import com.logistics.dto.pay.TransactionVO;

import java.util.List;

/**
 * 食堂餐卡支付服务
 */
public interface PayService {

    /**
     * 查询账户（首次使用自动创建）
     */
    AccountVO getAccount(Long userId);

    /**
     * 充值
     */
    void recharge(RechargeRequest request);

    /**
     * 消费扣款
     */
    void consume(ConsumeRequest request);

    /**
     * 退款（对应原交易）
     */
    void refund(Long transactionId, String remark);

    /**
     * 分页查询交易流水
     */
    PageResult<TransactionVO> queryTransactions(TransactionPageQuery query);

    /**
     * 根据业务单号查询交易
     */
    List<TransactionVO> getByBizOrderNo(String bizOrderNo);
}
