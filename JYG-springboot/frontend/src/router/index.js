import { createRouter, createWebHistory } from 'vue-router'

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
        path: '/gc/asset-apply',
        name: 'AssetApply',
        component: () => import('../views/gc/AssetApply.vue'),
        meta: { title: '资产入仓' }
      },
      {
        path: '/gc/asset-list',
        name: 'AssetList',
        component: () => import('../views/gc/AssetList.vue'),
        meta: { title: '资产管理' }
      },
      {
        path: '/gc/borrow-apply',
        name: 'BorrowApply',
        component: () => import('../views/gc/BorrowApply.vue'),
        meta: { title: '借用申请' }
      },
      {
        path: '/gc/return-apply',
        name: 'ReturnApply',
        component: () => import('../views/gc/ReturnApply.vue'),
        meta: { title: '归还验收' }
      },
      {
        path: '/gc/transfer-apply',
        name: 'TransferApply',
        component: () => import('../views/gc/TransferApply.vue'),
        meta: { title: '调剂管理' }
      },
      {
        path: '/gc/dispose-apply',
        name: 'DisposeApply',
        component: () => import('../views/gc/DisposeApply.vue'),
        meta: { title: '处置管理' }
      },
      {
        path: '/cl/vehicle',
        name: 'VehicleManage',
        component: () => import('../views/cl/VehicleManage.vue'),
        meta: { title: '车辆管理' }
      },
      {
        path: '/cl/apply',
        name: 'ApplyManage',
        component: () => import('../views/cl/ApplyManage.vue'),
        meta: { title: '用车申请' }
      },
      {
        path: '/cl/dispatch',
        name: 'DispatchManage',
        component: () => import('../views/cl/DispatchManage.vue'),
        meta: { title: '车辆调度' }
      },
      {
        path: '/cl/track',
        name: 'TrackMonitor',
        component: () => import('../views/cl/TrackMonitor.vue'),
        meta: { title: '运行监控' }
      },
      {
        path: '/cl/cost',
        name: 'CostManage',
        component: () => import('../views/cl/CostManage.vue'),
        meta: { title: '费用管理' }
      },
      {
        path: '/cl/cost-summary',
        name: 'CostSummary',
        component: () => import('../views/cl/CostSummary.vue'),
        meta: { title: '单车台账' }
      },
      {
        path: '/cl/repair',
        name: 'RepairManage',
        component: () => import('../views/cl/RepairManage.vue'),
        meta: { title: '维修保养' }
      },
      {
        path: '/gy/room',
        name: 'RoomManage',
        component: () => import('../views/gy/RoomManage.vue'),
        meta: { title: '房间管理' }
      },
      {
        path: '/gy/occupant',
        name: 'OccupantManage',
        component: () => import('../views/gy/OccupantManage.vue'),
        meta: { title: '入住管理' }
      },
      {
        path: '/gy/repair',
        name: 'GyRepairManage',
        component: () => import('../views/gy/RepairManage.vue'),
        meta: { title: '维修管理' }
      },
      {
        path: '/gy/cleaning',
        name: 'CleaningManage',
        component: () => import('../views/gy/CleaningManage.vue'),
        meta: { title: '保洁管理' }
      },
      {
        path: '/st/material',
        name: 'MaterialManage',
        component: () => import('../views/st/MaterialManage.vue'),
        meta: { title: '物资管理' }
      },
      {
        path: '/st/purchase',
        name: 'PurchaseManage',
        component: () => import('../views/st/PurchaseManage.vue'),
        meta: { title: '采购管理' }
      },
      {
        path: '/st/inventory',
        name: 'InventoryManage',
        component: () => import('../views/st/InventoryManage.vue'),
        meta: { title: '库存管理' }
      },
      {
        path: '/st/meal-reserve',
        name: 'MealReserve',
        component: () => import('../views/st/MealReserve.vue'),
        meta: { title: '预约订餐' }
      },
      {
        path: '/st/meal-statistics',
        name: 'MealStatistics',
        component: () => import('../views/st/MealStatistics.vue'),
        meta: { title: '备餐统计' }
      },
      {
        path: '/st/statistics',
        name: 'StatisticsManage',
        component: () => import('../views/st/StatisticsManage.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: '/pay/card',
        name: 'CardManage',
        component: () => import('../views/pay/CardManage.vue'),
        meta: { title: '餐卡管理' }
      },
      {
        path: '/message',
        name: 'MessageCenter',
        component: () => import('../views/message/MessageCenter.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: '/system/log',
        name: 'LogQuery',
        component: () => import('../views/system/LogQuery.vue'),
        meta: { title: '操作日志', roles: ['ADMIN'] }
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
    // 角色权限校验：仅当目标路由声明了 meta.roles 时拦截（与菜单可见性联动）
    const requiredRoles = to.meta.roles
    if (requiredRoles && requiredRoles.length > 0) {
      let userInfo = null
      try {
        userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      } catch (e) {
        userInfo = null
      }
      const userRoles = (userInfo && userInfo.roles) || []
      const hasPermission = userRoles.some((r) => requiredRoles.includes(r))
      if (!hasPermission) {
        next('/')
        return
      }
    }
    next()
  }
})

export default router
