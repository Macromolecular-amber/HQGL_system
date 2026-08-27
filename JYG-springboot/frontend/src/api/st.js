import request from '@/utils/request'

/** 新增或编辑物资 */
export function saveMaterial(data) {
  return request.post('/api/st/material/save', data)
}

/** 逻辑删除物资 */
export function deleteMaterial(id) {
  return request.delete(`/api/st/material/${id}`)
}

/** 分页查询物资 */
export function getMaterialPage(params) {
  return request.get('/api/st/material/page', { params })
}

/** 物资详情 */
export function getMaterialDetail(id) {
  return request.get(`/api/st/material/${id}`)
}

/** 按分类获取物资列表 */
export function getMaterialByCategory(category) {
  return request.get(`/api/st/material/category/${category}`)
}

/** 提交采购申请 */
export function applyPurchase(data) {
  return request.post('/api/st/purchase/apply', data)
}

/** 采购单审批 */
export function auditPurchase(data) {
  return request.put('/api/st/purchase/audit', data)
}

/** 采购单验收 */
export function acceptPurchase(data) {
  return request.put('/api/st/purchase/accept', data)
}

/** 分页查询采购单 */
export function getPurchasePage(params) {
  return request.get('/api/st/purchase/page', { params })
}

/** 采购单详情 */
export function getPurchaseDetail(id) {
  return request.get(`/api/st/purchase/${id}`)
}

/** 领用出库 */
export function stockOut(data) {
  return request.post('/api/st/inventory/stock-out', data)
}

/** 库存调整（盘点） */
export function adjustStock(data) {
  return request.post('/api/st/inventory/adjust', data)
}

/** 分页查询库存流水 */
export function getInventoryPage(params) {
  return request.get('/api/st/inventory/page', { params })
}

/** 查询某物资的流水 */
export function getInventoryByMaterial(id) {
  return request.get(`/api/st/inventory/material/${id}`)
}

/** 获取库存预警列表 */
export function getStockAlerts() {
  return request.get('/api/st/inventory/alerts')
}

/** 预约订餐 */
export function reserveMeal(data) {
  return request.post('/api/st/meal/reserve', data)
}

/** 取消预约 */
export function cancelMeal(data) {
  return request.put('/api/st/meal/cancel', data)
}

/** 分页查询个人预约记录 */
export function getMealPage(params) {
  return request.get('/api/st/meal/page', { params })
}

/** 按日期查询所有预约（管理员用） */
export function getMealByDate(date) {
  return request.get(`/api/st/meal/date/${date}`)
}

/** 备餐统计（管理员用） */
export function getMealStatistics(params) {
  return request.get('/api/st/meal/statistics', { params })
}

/** 记录餐余 */
export function recordWaste(data) {
  return request.post('/api/st/statistics/waste/record', data)
}

/** 分页查询餐余记录 */
export function getWastePage(params) {
  return request.get('/api/st/statistics/waste/page', { params })
}

/** 餐余统计 */
export function getWasteStatistics(params) {
  return request.get('/api/st/statistics/waste/statistics', { params })
}

/** 消费统计 */
export function getConsumeStatistics(params) {
  return request.get('/api/st/statistics/consume/statistics', { params })
}

/** 采购统计 */
export function getPurchaseStatistics(params) {
  return request.get('/api/st/statistics/purchase/statistics', { params })
}
