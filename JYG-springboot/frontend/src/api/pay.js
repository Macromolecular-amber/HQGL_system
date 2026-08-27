import request from '@/utils/request'

/** 查询餐卡账户 */
export function getAccount(userId) {
  return request.get(`/api/pay/account/${userId}`)
}

/** 充值 */
export function recharge(data) {
  return request.post('/api/pay/recharge', data)
}

/** 消费扣款 */
export function consume(data) {
  return request.post('/api/pay/consume', data)
}

/** 退款（对应原交易） */
export function refund(transactionId, data) {
  return request.post(`/api/pay/refund/${transactionId}`, data)
}

/** 分页查询交易流水 */
export function getTransactionPage(params) {
  return request.get('/api/pay/transactions/page', { params })
}

/** 根据业务单号查询交易 */
export function getTransactionByOrder(bizOrderNo) {
  return request.get(`/api/pay/transactions/order/${bizOrderNo}`)
}
