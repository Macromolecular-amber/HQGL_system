import request from '@/utils/request'

/** 新增或编辑房间 */
export function saveRoom(data) {
  return request.post('/gy/room/save', data)
}

/** 逻辑删除房间 */
export function deleteRoom(id) {
  return request.delete(`/gy/room/${id}`)
}

/** 分页查询房间 */
export function getRoomPage(params) {
  return request.get('/gy/room/page', { params })
}

/** 房间详情 */
export function getRoomDetail(id) {
  return request.get(`/gy/room/${id}`)
}

/** 获取空闲房间（供分配选择） */
export function getAvailableRooms(roomType) {
  return request.get('/gy/room/available', { params: { roomType } })
}

/** 专家公寓直接分配入住 */
export function assignDirect(data) {
  return request.post('/gy/occupant/assign/direct', data)
}

/** 人才公寓提交入住申请 */
export function applyOccupant(data) {
  return request.post('/gy/occupant/apply', data)
}

/** 人才公寓入住审批 */
export function auditOccupant(data) {
  return request.put('/gy/occupant/audit', data)
}

/** 分页查询入住记录 */
export function getOccupantPage(params) {
  return request.get('/gy/occupant/page', { params })
}

/** 入住记录详情 */
export function getOccupantDetail(id) {
  return request.get(`/gy/occupant/${id}`)
}

/** 退住 */
export function checkoutOccupant(id, params) {
  return request.put(`/gy/occupant/checkout/${id}`, null, { params })
}

/** 退住验收 */
export function acceptCheckout(data) {
  return request.put('/gy/occupant/checkout/accept', data)
}

/** 租期到期预警 */
export function getExpiring(days) {
  return request.get('/gy/occupant/expiring', { params: { days } })
}

/** 提交维修申请 */
export function applyRepair(data) {
  return request.post('/gy/repair/apply', data)
}

/** 维修审批 */
export function auditRepair(data) {
  return request.put('/gy/repair/audit', data)
}

/** 开始维修 */
export function startRepair(data) {
  return request.put('/gy/repair/start', data)
}

/** 维修验收 */
export function acceptRepair(data) {
  return request.put('/gy/repair/accept', data)
}

/** 分页查询维修单 */
export function getRepairPage(params) {
  return request.get('/gy/repair/page', { params })
}

/** 维修单详情 */
export function getRepairDetail(id) {
  return request.get(`/gy/repair/${id}`)
}

/** 获取某房间的所有维修记录 */
export function getRepairByRoom(roomId) {
  return request.get(`/gy/repair/room/${roomId}`)
}

/** 提交保洁申请 */
export function applyCleaning(data) {
  return request.post('/gy/cleaning/apply', data)
}

/** 保洁审批 */
export function auditCleaning(data) {
  return request.put('/gy/cleaning/audit', data)
}

/** 保洁派单 */
export function assignCleaning(data) {
  return request.put('/gy/cleaning/assign', data)
}

/** 保洁验收 */
export function acceptCleaning(data) {
  return request.put('/gy/cleaning/accept', data)
}

/** 分页查询保洁单 */
export function getCleaningPage(params) {
  return request.get('/gy/cleaning/page', { params })
}

/** 保洁单详情 */
export function getCleaningDetail(id) {
  return request.get(`/gy/cleaning/${id}`)
}

/** 获取某房间的所有保洁记录 */
export function getCleaningByRoom(roomId) {
  return request.get(`/gy/cleaning/room/${roomId}`)
}
