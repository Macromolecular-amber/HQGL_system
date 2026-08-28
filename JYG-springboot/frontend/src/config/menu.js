/**
 * 菜单配置：按角色定义"菜单即权限"，同一功能域内菜单完整分配
 * 说明：角色为登录 token 中 roles 数组的原始值（如 ADMIN/USER/BIZ_ADMIN...）
 * 规则：多角色取并集去重；无权限菜单完全隐藏；空分组自动不渲染
 */
const R = {
  // 公物仓
  GC_APPLY: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE'],                       // 资产入仓
  GC_LIST: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'WAREHOUSE', 'USER'],    // 资产管理
  GC_BORROW: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER', 'USER'],           // 借用申请
  GC_WH: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE'],                          // 归还验收/调剂/处置
  // 公务用车
  CL_VEHICLE: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'WAREHOUSE', 'DRIVER'],
  CL_APPLY: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER', 'USER'],
  CL_DISPATCH: ['ADMIN', 'BIZ_ADMIN'],
  CL_TRACK: ['ADMIN', 'DIRECTOR'],
  CL_COST: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DRIVER'],
  CL_SUMMARY: ['ADMIN', 'DIRECTOR'],
  CL_REPAIR: ['ADMIN', 'BIZ_ADMIN', 'DRIVER'],
  // 公寓
  GY_ROOM: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE'],
  GY_OCCUPANT: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'USER'],
  GY_REPAIR: ['ADMIN', 'BIZ_ADMIN', 'DEPT_MANAGER'],
  GY_CLEANING: ['ADMIN', 'DEPT_MANAGER', 'USER', 'CLEANER'],
  // 食堂
  ST_MATERIAL: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER'],
  ST_PURCHASE: ['ADMIN', 'BIZ_ADMIN'],
  ST_INVENTORY: ['ADMIN', 'BIZ_ADMIN'],
  ST_MEAL_RESERVE: ['ADMIN', 'DEPT_MANAGER', 'USER'],
  ST_MEAL_STATS: ['ADMIN', 'DIRECTOR'],
  ST_STATS: ['ADMIN', 'DIRECTOR'],
  PAY_CARD: ['ADMIN', 'USER'],
  // 系统管理
  MSG: ['ADMIN', 'BIZ_ADMIN', 'DIRECTOR', 'DEPT_MANAGER', 'WAREHOUSE', 'USER', 'DRIVER', 'CLEANER'],
  LOG: ['ADMIN']
}

export const allMenus = [
  {
    path: 'gc',
    title: '公物仓管理',
    icon: 'Box',
    children: [
      { path: '/gc/asset-apply', title: '资产入仓', roles: R.GC_APPLY },
      { path: '/gc/asset-list', title: '资产管理', roles: R.GC_LIST },
      { path: '/gc/borrow-apply', title: '借用申请', roles: R.GC_BORROW },
      { path: '/gc/return-apply', title: '归还验收', roles: R.GC_WH },
      { path: '/gc/transfer-apply', title: '调剂管理', roles: R.GC_WH },
      { path: '/gc/dispose-apply', title: '处置管理', roles: R.GC_WH }
    ]
  },
  {
    path: 'cl',
    title: '公务用车管理',
    icon: 'Van',
    children: [
      { path: '/cl/vehicle', title: '车辆管理', roles: R.CL_VEHICLE },
      { path: '/cl/apply', title: '用车申请', roles: R.CL_APPLY },
      { path: '/cl/dispatch', title: '车辆调度', roles: R.CL_DISPATCH },
      { path: '/cl/track', title: '运行监控', roles: R.CL_TRACK },
      { path: '/cl/cost', title: '费用管理', roles: R.CL_COST },
      { path: '/cl/cost-summary', title: '单车台账', roles: R.CL_SUMMARY },
      { path: '/cl/repair', title: '维修保养', roles: R.CL_REPAIR }
    ]
  },
  {
    path: 'gy',
    title: '公寓管理',
    icon: 'OfficeBuilding',
    children: [
      { path: '/gy/room', title: '房间管理', roles: R.GY_ROOM },
      { path: '/gy/occupant', title: '入住管理', roles: R.GY_OCCUPANT },
      { path: '/gy/repair', title: '维修管理', roles: R.GY_REPAIR },
      { path: '/gy/cleaning', title: '保洁管理', roles: R.GY_CLEANING }
    ]
  },
  {
    path: 'st',
    title: '食堂管理',
    icon: 'Dish',
    children: [
      { path: '/st/material', title: '物资管理', roles: R.ST_MATERIAL },
      { path: '/st/purchase', title: '采购管理', roles: R.ST_PURCHASE },
      { path: '/st/inventory', title: '库存管理', roles: R.ST_INVENTORY },
      { path: '/st/meal-reserve', title: '预约订餐', roles: R.ST_MEAL_RESERVE },
      { path: '/st/meal-statistics', title: '备餐统计', roles: R.ST_MEAL_STATS },
      { path: '/st/statistics', title: '统计分析', roles: R.ST_STATS },
      { path: '/pay/card', title: '餐卡管理', roles: R.PAY_CARD }
    ]
  },
  {
    path: 'sys',
    title: '系统管理',
    icon: 'Document',
    children: [
      { path: '/message', title: '消息中心', roles: R.MSG },
      { path: '/system/log', title: '操作日志', roles: R.LOG }
    ]
  }
]
