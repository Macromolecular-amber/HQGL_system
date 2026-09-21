/**
 * 登录态与权限工具（与 Web 端 permission.js 对齐）
 */

/** 读取登录用户信息 */
export const getUserInfo = () => uni.getStorageSync('userInfo') || {}

/** 读取 token */
export const getToken = () => uni.getStorageSync('token') || ''

/** 保存登录态 */
export const setLoginState = (token, userInfo) => {
  uni.setStorageSync('token', token)
  uni.setStorageSync('userInfo', userInfo)
}

/** 清除登录态 */
export const clearLoginState = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')
}

/** 当前用户角色列表 */
export const getUserRoles = () => {
  const info = getUserInfo()
  return (info && info.roles) || []
}

/** 是否拥有权限（ADMIN 通吃，与后端 @RequiresRoles 一致） */
export const hasRole = (requiredRoles) => {
  if (!requiredRoles || requiredRoles.length === 0) return true
  const roles = getUserRoles()
  return roles.includes('ADMIN') || requiredRoles.some((r) => roles.includes(r))
}

/**
 * 各功能所需角色（与 Web 端 menu.js 的 R 常量一致，单一数据源）
 */
export const R = {
  GC_APPLY: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'USER', 'DIRECTOR'],
  GC_BORROW: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER', 'USER', 'WAREHOUSE', 'DIRECTOR'],
  // 归还验收/调剂/处置（与 Web 端 menu.js GC_WH 一致：+USER申请/DIRECTOR审批）
  GC_RETURN: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'USER', 'DIRECTOR'],
  GC_TRANSFER: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'USER', 'DIRECTOR'],
  GC_DISPOSE: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'USER', 'DIRECTOR'],
  CL_APPLY: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER', 'USER', 'DIRECTOR', 'WAREHOUSE'],
  CL_VEHICLE: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'WAREHOUSE'],
  CL_DISPATCH: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'DRIVER'],
  CL_TRACK: ['ADMIN', 'DIRECTOR'],
  CL_COST: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DRIVER', 'WAREHOUSE'],
  CL_SUMMARY: ['ADMIN', 'DIRECTOR'],
  CL_REPAIR: ['ADMIN', 'BIZ_ADMIN', 'DRIVER', 'WAREHOUSE', 'DIRECTOR', 'USER'],
  GY_OCCUPANT: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'USER', 'WAREHOUSE'],
  GY_ROOM: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE'],
  GY_REPAIR: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER', 'WAREHOUSE', 'DIRECTOR', 'USER'],
  GY_CLEANING: ['ADMIN', 'DEPT_MANAGER', 'USER', 'CLEANER', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'],
  ST_MATERIAL: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE'],
  ST_PURCHASE: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE'],
  ST_INVENTORY: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE'],
  ST_STATS: ['ADMIN', 'DIRECTOR', 'BIZ_ADMIN', 'WAREHOUSE'],
  ST_MEAL_STATS: ['ADMIN', 'DIRECTOR', 'BIZ_ADMIN', 'WAREHOUSE'],
  GC_LIST: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'WAREHOUSE', 'USER'],
  PAY_CARD: ['ADMIN', 'USER', 'BIZ_ADMIN'],
  DASH_TODO: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE', 'USER', 'DRIVER', 'CLEANER'],
  DASH_LEADER: ['ADMIN', 'DIRECTOR'],
  ST_MEAL_RESERVE: ['ADMIN', 'DEPT_MANAGER', 'USER', 'BIZ_ADMIN'],
  MSG: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE', 'USER', 'DRIVER', 'CLEANER'],
  LOG: ['ADMIN', 'DIRECTOR'],
  BROADCAST: ['ADMIN']
}