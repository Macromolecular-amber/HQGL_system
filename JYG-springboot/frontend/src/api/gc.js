import request from '@/utils/request'

/**
 * 公物仓资产接口
 */

/** 提交入仓申请 */
export function apply(data) {
  return request.post('/api/gc/asset/apply', data)
}

/** 审核入仓申请 */
export function audit(data) {
  return request.put('/api/gc/asset/audit', data)
}

/** 分页查询资产（入仓申请列表） */
export function getPage(params) {
  return request.get('/api/gc/asset/page', { params })
}

/** 资产详情（入仓申请列表用，简单字段） */
export function getAssetById(id) {
  return request.get(`/api/gc/asset/${id}`)
}

/** 分页查询资产列表（多条件筛选） */
export function getList(params) {
  return request.get('/api/gc/asset/list', { params })
}

/** 资产详情（完整字段，含审核信息） */
export function getDetail(id) {
  return request.get(`/api/gc/asset/detail/${id}`)
}

/** 提交借用申请 */
export function applyBorrow(data) {
  return request.post('/api/gc/borrow/apply', data)
}

/** 审批借用申请 */
export function auditBorrow(data) {
  return request.put('/api/gc/borrow/audit', data)
}

/** 分页查询借用单 */
export function getBorrowPage(params) {
  return request.get('/api/gc/borrow/page', { params })
}

/** 借用单详情 */
export function getBorrowDetail(id) {
  return request.get(`/api/gc/borrow/${id}`)
}

/** 借用单下的资产列表（用于归还选择） */
export function getBorrowedAssets(borrowOrderId) {
  return request.get(`/api/gc/borrow/${borrowOrderId}/assets`)
}

/** 提交归还申请 */
export function applyReturn(data) {
  return request.post('/api/gc/return/apply', data)
}

/** 归还验收 */
export function acceptReturn(data) {
  return request.put('/api/gc/return/accept', data)
}

/** 分页查询归还单 */
export function getReturnPage(params) {
  return request.get('/api/gc/return/page', { params })
}

/** 归还单详情 */
export function getReturnDetail(id) {
  return request.get(`/api/gc/return/${id}`)
}

/** 提交调剂申请 */
export function applyTransfer(data) {
  return request.post('/api/gc/transfer/apply', data)
}

/** 审批调剂申请 */
export function auditTransfer(data) {
  return request.put('/api/gc/transfer/audit', data)
}

/** 分页查询调剂单 */
export function getTransferPage(params) {
  return request.get('/api/gc/transfer/page', { params })
}

/** 调剂单详情 */
export function getTransferDetail(id) {
  return request.get(`/api/gc/transfer/${id}`)
}

/** 查询在仓资产（供调剂选择） */
export function getAvailableAssets(params) {
  return request.get('/api/gc/transfer/available-assets', { params })
}

/** 提交处置申请 */
export function applyDispose(data) {
  return request.post('/api/gc/transfer/dispose/apply', data)
}

/** 审批处置申请 */
export function auditDispose(data) {
  return request.put('/api/gc/transfer/dispose/audit', data)
}

/** 录入处置收益 */
export function recordIncome(data) {
  return request.put('/api/gc/transfer/dispose/income', data)
}

/** 分页查询处置单 */
export function getDisposePage(params) {
  return request.get('/api/gc/transfer/dispose/page', { params })
}

/** 处置单详情 */
export function getDisposeDetail(id) {
  return request.get(`/api/gc/transfer/dispose/${id}`)
}
