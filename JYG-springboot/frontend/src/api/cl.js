import request from '@/utils/request'

/**
 * 公务用车车辆档案接口
 */

/** 新增或编辑车辆 */
export function saveVehicle(data) {
  return request.post('/api/cl/vehicle/save', data)
}

/** 逻辑删除车辆 */
export function deleteVehicle(id) {
  return request.delete(`/api/cl/vehicle/${id}`)
}

/** 分页查询车辆 */
export function getVehiclePage(params) {
  return request.get('/api/cl/vehicle/page', { params })
}

/** 车辆详情 */
export function getVehicleDetail(id) {
  return request.get(`/api/cl/vehicle/${id}`)
}

/** 可用车辆列表（供调度选择） */
export function getAvailableVehicles() {
  return request.get('/api/cl/vehicle/available')
}

/** 提交用车申请 */
export function applyCar(data) {
  return request.post('/api/cl/apply/apply', data)
}

/** 审批用车申请 */
export function auditApply(data) {
  return request.put('/api/cl/apply/audit', data)
}

/** 分页查询用车申请 */
export function getApplyPage(params) {
  return request.get('/api/cl/apply/page', { params })
}

/** 用车申请详情 */
export function getApplyDetail(id) {
  return request.get(`/api/cl/apply/${id}`)
}

/** 取消用车申请 */
export function cancelApply(id) {
  return request.put(`/api/cl/apply/cancel/${id}`)
}

/** 派单 */
export function dispatchCar(data) {
  return request.post('/api/cl/dispatch/dispatch', data)
}

/** 车辆归还 */
export function returnCar(data) {
  return request.put('/api/cl/dispatch/return', data)
}

/** 分页查询派单 */
export function getDispatchPage(params) {
  return request.get('/api/cl/dispatch/page', { params })
}

/** 派单详情 */
export function getDispatchDetail(id) {
  return request.get(`/api/cl/dispatch/${id}`)
}

/** 根据申请ID查询派单记录 */
export function getDispatchByApply(applyId) {
  return request.get(`/api/cl/dispatch/by-apply/${applyId}`)
}

/** 获取车辆最新位置 */
export function getCurrentLocation(vehicleId) {
  return request.get(`/api/cl/track/current/${vehicleId}`)
}

/** 获取所有出车中车辆的最新位置 */
export function getAllCurrentLocations() {
  return request.get('/api/cl/track/current/all')
}

/** 查询车辆轨迹历史（按时间范围） */
export function getTrackHistory(data) {
  return request.post('/api/cl/track/history', data)
}

/** 手动触发模拟轨迹生成（测试用） */
export function simulateTrack(dispatchId) {
  return request.post(`/api/cl/track/simulate/${dispatchId}`)
}

/** 费用登记 */
export function saveCost(data) {
  return request.post('/api/cl/cost/save', data)
}

/** 编辑费用（仅待审批状态） */
export function updateCost(id, data) {
  return request.put(`/api/cl/cost/update/${id}`, data)
}

/** 费用审批 */
export function auditCost(data) {
  return request.put('/api/cl/cost/audit', data)
}

/** 分页查询费用明细 */
export function getCostPage(params) {
  return request.get('/api/cl/cost/page', { params })
}

/** 费用详情 */
export function getCostDetail(id) {
  return request.get(`/api/cl/cost/${id}`)
}

/** 单车台账汇总（按月） */
export function getVehicleSummary(vehicleId, yearMonth) {
  return request.get(`/api/cl/cost/summary/vehicle/${vehicleId}`, { params: { yearMonth } })
}

/** 所有车辆台账汇总（按月） */
export function getAllSummary(yearMonth) {
  return request.get('/api/cl/cost/summary/all', { params: { yearMonth } })
}

/** 提交维修申请 */
export function applyRepair(data) {
  return request.post('/api/cl/repair/apply', data)
}

/** 维修审批 */
export function auditRepair(data) {
  return request.put('/api/cl/repair/audit', data)
}

/** 开始维修 */
export function startRepair(data) {
  return request.put('/api/cl/repair/start', data)
}

/** 维修验收 */
export function acceptRepair(data) {
  return request.put('/api/cl/repair/accept', data)
}

/** 分页查询维修单 */
export function getRepairPage(params) {
  return request.get('/api/cl/repair/page', { params })
}

/** 维修单详情 */
export function getRepairDetail(id) {
  return request.get(`/api/cl/repair/${id}`)
}

/** 获取某车辆的所有维修记录 */
export function getRepairByVehicle(id) {
  return request.get(`/api/cl/repair/vehicle/${id}`)
}
