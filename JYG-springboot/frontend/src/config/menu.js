/**
 * 菜单配置：每个菜单项声明可访问角色（与后端 @RequiresRoles 一致）
 * 说明：角色为登录 token 中 roles 数组的原始值（如 ADMIN/USER/BIZ_ADMIN...）
 */
const R = {
  ALL: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'USER'],
  USER: ['ADMIN', 'USER', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'],
  APPLY: ['ADMIN', 'USER', 'BIZ_ADMIN'],
  BIZ: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE'],
  AUDIT: ['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'],
  USER_BIZ: ['ADMIN', 'USER', 'BIZ_ADMIN'],
  MSG: ['ADMIN', 'USER', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DRIVER', 'CLEANER'],
  LOG: ['ADMIN', 'DIRECTOR']
}

export const allMenus = [
  {
    path: 'gc',
    title: '公物仓管理',
    icon: 'Box',
    children: [
      { path: '/gc/asset-apply', title: '资产入仓', roles: R.APPLY },
      { path: '/gc/asset-list', title: '资产管理', roles: R.AUDIT },
      { path: '/gc/borrow-apply', title: '借用申请', roles: R.APPLY },
      { path: '/gc/return-apply', title: '归还验收', roles: R.APPLY },
      { path: '/gc/transfer-apply', title: '调剂管理', roles: R.APPLY },
      { path: '/gc/dispose-apply', title: '处置管理', roles: R.APPLY }
    ]
  },
  {
    path: 'cl',
    title: '公务用车管理',
    icon: 'Van',
    children: [
      { path: '/cl/vehicle', title: '车辆管理', roles: R.BIZ },
      { path: '/cl/apply', title: '用车申请', roles: R.USER_BIZ },
      { path: '/cl/dispatch', title: '车辆调度', roles: R.BIZ },
      { path: '/cl/track', title: '运行监控', roles: R.AUDIT },
      { path: '/cl/cost', title: '费用管理', roles: R.BIZ },
      { path: '/cl/cost-summary', title: '单车台账', roles: R.AUDIT },
      { path: '/cl/repair', title: '维修保养', roles: R.USER_BIZ }
    ]
  },
  {
    path: 'gy',
    title: '公寓管理',
    icon: 'OfficeBuilding',
    children: [
      { path: '/gy/room', title: '房间管理', roles: R.BIZ },
      { path: '/gy/occupant', title: '入住管理', roles: R.USER_BIZ },
      { path: '/gy/repair', title: '维修管理', roles: R.USER_BIZ },
      { path: '/gy/cleaning', title: '保洁管理', roles: R.USER_BIZ }
    ]
  },
  {
    path: 'st',
    title: '食堂管理',
    icon: 'Dish',
    children: [
      { path: '/st/material', title: '物资管理', roles: R.BIZ },
      { path: '/st/purchase', title: '采购管理', roles: R.BIZ },
      { path: '/st/inventory', title: '库存管理', roles: R.BIZ },
      { path: '/st/meal-reserve', title: '预约订餐', roles: R.USER_BIZ },
      { path: '/st/meal-statistics', title: '备餐统计', roles: R.BIZ },
      { path: '/st/statistics', title: '统计分析', roles: R.BIZ },
      { path: '/pay/card', title: '餐卡管理', roles: R.USER_BIZ }
    ]
  },
  {
    path: 'msg',
    title: '消息中心',
    icon: 'Bell',
    children: [
      { path: '/message', title: '我的消息', roles: R.MSG }
    ]
  },
  {
    path: 'sys',
    title: '系统管理',
    icon: 'Document',
    children: [
      { path: '/system/log', title: '操作日志', roles: R.LOG }
    ]
  }
]
