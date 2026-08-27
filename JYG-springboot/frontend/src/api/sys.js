import request from '@/utils/request'

/**
 * 系统基础数据接口
 */

/** 单位下拉列表 */
export function getUnitList() {
  return request.get('/sys/unit/list')
}

/** 当前登录用户信息 */
export function getCurrentUser() {
  return request.get('/sys/user/current')
}

/** 驾驶员列表 */
export function getDrivers() {
  return request.get('/sys/user/drivers')
}

/** 获取保洁员列表（角色 CLEANER） */
export function getCleaners() {
  return request.get('/sys/user/cleaners')
}
