package com.logistics.controller.pay;

import com.logistics.annotation.Log;
import com.logistics.common.PageResult;
import com.logistics.common.Result;
import com.logistics.dto.pay.AccountVO;
import com.logistics.dto.pay.ConsumeRequest;
import com.logistics.dto.pay.RechargeRequest;
import com.logistics.dto.pay.TransactionPageQuery;
import com.logistics.dto.pay.TransactionVO;
import com.logistics.service.pay.PayService;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 食堂刷脸支付与餐卡管理
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 查询账户
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @GetMapping("/account/{userId}")
    public Result<AccountVO> account(@PathVariable Long userId) {
        return Result.success(payService.getAccount(userId));
    }

    /**
     * 充值
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @PostMapping("/recharge")
    @Log(module="PAY", operation="餐卡充值", type="ADD")
    public Result<Void> recharge(@Valid @RequestBody RechargeRequest request) {
        payService.recharge(request);
        return Result.success();
    }

    /**
     * 消费扣款
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @PostMapping("/consume")
    @Log(module="PAY", operation="餐卡消费", type="UPDATE")
    public Result<Void> consume(@Valid @RequestBody ConsumeRequest request) {
        payService.consume(request);
        return Result.success();
    }

    /**
     * 退款（对应原交易）
     */
    @RequiresRoles({"BIZ_ADMIN"})
    @PostMapping("/refund/{transactionId}")
    @Log(module="PAY", operation="餐卡退款", type="UPDATE")
    public Result<Void> refund(@PathVariable Long transactionId, @RequestParam(required = false) String remark) {
        payService.refund(transactionId, remark);
        return Result.success();
    }

    /**
     * 分页查询交易流水
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @GetMapping("/transactions/page")
    public Result<PageResult<TransactionVO>> page(TransactionPageQuery query) {
        return Result.success(payService.queryTransactions(query));
    }

    /**
     * 根据业务单号查询交易
     */
    @RequiresRoles({"USER","BIZ_ADMIN"})
    @GetMapping("/transactions/order/{bizOrderNo}")
    public Result<List<TransactionVO>> byOrder(@PathVariable String bizOrderNo) {
        return Result.success(payService.getByBizOrderNo(bizOrderNo));
    }
}
