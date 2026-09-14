import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { R } from '@/config/menu'
import { hasRole } from '@/utils/permission'

/**
 * 路由权限规则：每个业务路由通过 meta.roles 声明所需角色（与菜单 menu.js 单一数据源对齐）
 * - 首页 / 登录 / 待办中心：所有登录用户可用，不设 roles
 * - 未声明 roles 的路由视为全员可用
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/dashboard/leadership',
        name: 'LeadershipDashboard',
        component: () => import('../views/dashboard/Leadership.vue'),
        meta: { title: '领导驾驶舱', roles: ['ADMIN', 'DIRECTOR', 'BIZ_ADMIN'] }
      },
      {
        path: '/todos',
        name: 'TodoCenter',
        component: () => import('../views/dashboard/TodoCenter.vue'),
        meta: { title: '待办审批' }
      },
      {
        path: '/gc/asset-apply',
        name: 'AssetApply',
        component: () => import('../views/gc/AssetApply.vue'),
        meta: { title: '资产入仓', roles: R.GC_APPLY }
      },
      {
        path: '/gc/asset-list',
        name: 'AssetList',
        component: () => import('../views/gc/AssetList.vue'),
        meta: { title: '资产管理', roles: R.GC_LIST }
      },
      {
        path: '/gc/borrow-apply',
        name: 'BorrowApply',
        component: () => import('../views/gc/BorrowApply.vue'),
        meta: { title: '借用申请', roles: R.GC_BORROW }
      },
      {
        path: '/gc/return-apply',
        name: 'ReturnApply',
        component: () => import('../views/gc/ReturnApply.vue'),
        meta: { title: '归还验收', roles: R.GC_WH }
      },
      {
        path: '/gc/transfer-apply',
        name: 'TransferApply',
        component: () => import('../views/gc/TransferApply.vue'),
        meta: { title: '调剂管理', roles: R.GC_WH }
      },
      {
        path: '/gc/dispose-apply',
        name: 'DisposeApply',
        component: () => import('../views/gc/DisposeApply.vue'),
        meta: { title: '处置管理', roles: R.GC_WH }
      },
      {
        path: '/cl/vehicle',
        name: 'VehicleManage',
        component: () => import('../views/cl/VehicleManage.vue'),
        meta: { title: '车辆管理', roles: R.CL_VEHICLE }
      },
      {
        path: '/cl/apply',
        name: 'ApplyManage',
        component: () => import('../views/cl/ApplyManage.vue'),
        meta: { title: '用车申请', roles: R.CL_APPLY }
      },
      {
        path: '/cl/dispatch',
        name: 'DispatchManage',
        component: () => import('../views/cl/DispatchManage.vue'),
        meta: { title: '车辆调度', roles: R.CL_DISPATCH }
      },
      {
        path: '/cl/track',
        name: 'TrackMonitor',
        component: () => import('../views/cl/TrackMonitor.vue'),
        meta: { title: '运行监控', roles: R.CL_TRACK }
      },
      {
        path: '/cl/cost',
        name: 'CostManage',
        component: () => import('../views/cl/CostManage.vue'),
        meta: { title: '费用管理', roles: R.CL_COST }
      },
      {
        path: '/cl/cost-summary',
        name: 'CostSummary',
        component: () => import('../views/cl/CostSummary.vue'),
        meta: { title: '单车台账', roles: R.CL_SUMMARY }
      },
      {
        path: '/cl/repair',
        name: 'RepairManage',
        component: () => import('../views/cl/RepairManage.vue'),
        meta: { title: '维修保养', roles: R.CL_REPAIR }
      },
      {
        path: '/gy/room',
        name: 'RoomManage',
        component: () => import('../views/gy/RoomManage.vue'),
        meta: { title: '房间管理', roles: R.GY_ROOM }
      },
      {
        path: '/gy/occupant',
        name: 'OccupantManage',
        component: () => import('../views/gy/OccupantManage.vue'),
        meta: { title: '入住管理', roles: R.GY_OCCUPANT }
      },
      {
        path: '/gy/repair',
        name: 'GyRepairManage',
        component: () => import('../views/gy/RepairManage.vue'),
        meta: { title: '维修管理', roles: R.GY_REPAIR }
      },
      {
        path: '/gy/cleaning',
        name: 'CleaningManage',
        component: () => import('../views/gy/CleaningManage.vue'),
        meta: { title: '保洁管理', roles: R.GY_CLEANING }
      },
      {
        path: '/st/material',
        name: 'MaterialManage',
        component: () => import('../views/st/MaterialManage.vue'),
        meta: { title: '物资管理', roles: R.ST_MATERIAL }
      },
      {
        path: '/st/purchase',
        name: 'PurchaseManage',
        component: () => import('../views/st/PurchaseManage.vue'),
        meta: { title: '采购管理', roles: R.ST_PURCHASE }
      },
      {
        path: '/st/inventory',
        name: 'InventoryManage',
        component: () => import('../views/st/InventoryManage.vue'),
        meta: { title: '库存管理', roles: R.ST_INVENTORY }
      },
      {
        path: '/st/meal-reserve',
        name: 'MealReserve',
        component: () => import('../views/st/MealReserve.vue'),
        meta: { title: '预约订餐', roles: R.ST_MEAL_RESERVE }
      },
      {
        path: '/st/meal-statistics',
        name: 'MealStatistics',
        component: () => import('../views/st/MealStatistics.vue'),
        meta: { title: '备餐统计', roles: R.ST_MEAL_STATS }
      },
      {
        path: '/st/statistics',
        name: 'StatisticsManage',
        component: () => import('../views/st/StatisticsManage.vue'),
        meta: { title: '统计分析', roles: R.ST_STATS }
      },
      {
        path: '/pay/card',
        name: 'CardManage',
        component: () => import('../views/pay/CardManage.vue'),
        meta: { title: '餐卡管理', roles: R.PAY_CARD }
      },
      {
        path: '/message',
        name: 'MessageCenter',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { title: '消息中心', roles: R.MSG }
      },
      {
        path: '/system/broadcast',
        name: 'BroadcastMessage',
        component: () => import('../views/system/BroadcastMessage.vue'),
        meta: { title: '发布全体消息', roles: R.BROADCAST }
      },
      {
        path: '/system/log',
        name: 'LogQuery',
        component: () => import('../views/system/LogQuery.vue'),
        meta: { title: '操作日志', roles: R.LOG }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页，已登录访问登录页跳回首页；带 meta.roles 的路由校验角色权限
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
      return
    }
    // 角色权限校验：无权限时提示并拦截，避免绕过菜单直接访问 URL
    const requiredRoles = to.meta.roles
    if (requiredRoles && requiredRoles.length > 0) {
      if (!hasRole(requiredRoles)) {
        ElMessage.warning('权限不足，无法访问该功能')
        next('/')
        return
      }
    }
    next()
  }
})

export default router
