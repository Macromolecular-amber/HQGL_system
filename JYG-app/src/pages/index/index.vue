<template>
  <view class="home">
    <!-- 顶部横幅 -->
    <view class="banner">
      <view class="banner-greet">👋 {{ userInfo.realName || '用户' }}</view>
      <view class="banner-date">{{ currentDate }}</view>
      <view class="banner-tag" @click="goMessage">未读消息 {{ unreadCount }} 条</view>
    </view>

    <!-- 快捷入口（按模块分组，权限过滤） -->
    <view class="quick-group" v-for="g in visibleGroups" :key="g.title">
      <view class="quick-group-title">{{ g.title }}</view>
      <view class="quick-card">
        <view class="quick-item" v-for="e in g.entries" :key="e.key" :style="{ width: (100 / g.cols) + '%' }" @click="handleEntry(e)">
          <text class="quick-icon">{{ e.icon }}</text>
          <text class="quick-label">{{ e.label }}</text>
        </view>
      </view>
    </view>

    <!-- 待办预览 -->
    <view class="section-card" v-if="todos.length > 0">
      <view class="section-title">
        <text>📋 我的待办</text>
        <text class="section-more" @click="goTodos">查看全部 ›</text>
      </view>
      <view class="todo-item" v-for="t in todos.slice(0, 5)" :key="t.id" @click="goTodos">
        <view class="todo-line">
          <text class="todo-title">{{ t.title }}</text>
        </view>
        <view class="todo-meta">
          <text class="todo-status">{{ t.status }}</text>
          <text class="todo-time">{{ t.time }}</text>
        </view>
      </view>
    </view>

    <!-- 消息预览 -->
    <view class="section-card" v-if="messages.length > 0">
      <view class="section-title">
        <text>💬 最新消息</text>
        <text class="section-more" @click="goMessage">查看全部 ›</text>
      </view>
      <view class="msg-item" v-for="m in messages.slice(0, 5)" :key="m.id" @click="goMessage">
        <view class="msg-line">
          <text class="msg-title">{{ m.title }}</text>
          <text v-if="!m.isRead" class="msg-dot">●</text>
        </view>
        <text class="msg-time">{{ formatTime(m.createTime) }}</text>
      </view>
    </view>

    <view class="empty" v-if="!todos.length && !messages.length">暂无待办与消息</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { getUserInfo, getUserRoles, hasRole, R } from '@/utils/auth'

const userInfo = getUserInfo()
const roles = getUserRoles()

// 快捷入口（按模块分组：公物仓管理 → 公车管理 → 公寓管理 → 食堂管理 → 系统功能）
const quickGroups = [
  {
    title: '公物仓管理',
    entries: [
      { key: 'asset', label: '资产入仓', icon: '📦', page: '/pages/gc/asset_apply/asset_apply', roles: R.GC_APPLY },
      { key: 'asset-list', label: '资产管理', icon: '🏷️', page: '/pages/gc/asset_list/asset_list', roles: R.GC_LIST },
      { key: 'borrow', label: '借用申请', icon: '🧺', page: '/pages/gc/borrow/borrow', roles: R.GC_BORROW },
      { key: 'asset-return', label: '资产归还', icon: '🔙', page: '/pages/gc/return_apply/return_apply', roles: R.GC_RETURN },
      { key: 'asset-transfer', label: '资产调剂', icon: '🔄', page: '/pages/gc/transfer_apply/transfer_apply', roles: R.GC_TRANSFER },
      { key: 'asset-dispose', label: '资产处置', icon: '🗑️', page: '/pages/gc/dispose_apply/dispose_apply', roles: R.GC_DISPOSE }
    ]
  },
  {
    title: '公车管理',
    entries: [
      { key: 'car-apply', label: '用车申请', icon: '🚗', page: '/pages/cl/apply/apply', roles: R.CL_APPLY },
      { key: 'vehicle', label: '车辆管理', icon: '🚙', page: '/pages/cl/vehicle/vehicle', roles: R.CL_VEHICLE },
      { key: 'dispatch', label: '车辆调度', icon: '🚦', page: '/pages/cl/dispatch/dispatch', roles: R.CL_DISPATCH },
      { key: 'track', label: '运行监控', icon: '🛰️', page: '/pages/cl/track/track', roles: R.CL_TRACK },
      { key: 'cost', label: '费用管理', icon: '💰', page: '/pages/cl/cost/cost', roles: R.CL_COST },
      { key: 'cost-summary', label: '单车台账', icon: '📊', page: '/pages/cl/cost_summary/cost_summary', roles: R.CL_SUMMARY },
      { key: 'repair', label: '维修保养', icon: '🔧', page: '/pages/cl/repair/repair', roles: R.CL_REPAIR }
    ]
  },
  {
    title: '公寓管理',
    entries: [
      { key: 'occupant', label: '公寓申请', icon: '🏢', page: '/pages/gy/occupant/occupant', roles: R.GY_OCCUPANT },
      { key: 'gy-room', label: '公寓房间', icon: '🏠', page: '/pages/gy/room/room', roles: R.GY_ROOM },
      { key: 'gy-repair', label: '公寓维修', icon: '🛠️', page: '/pages/gy/repair/repair', roles: R.GY_REPAIR },
      { key: 'cleaning', label: '保洁管理', icon: '🧹', page: '/pages/gy/cleaning/cleaning', roles: R.GY_CLEANING }
    ]
  },
  {
    title: '食堂管理',
    entries: [
      { key: 'meal', label: '预约订餐', icon: '🍚', page: '/pages/st/meal/meal', roles: R.ST_MEAL_RESERVE },
      { key: 'meal-statistics', label: '备餐统计', icon: '🥗', page: '/pages/st/meal_statistics/meal_statistics', roles: R.ST_MEAL_STATS },
      { key: 'material', label: '物资管理', icon: '🥬', page: '/pages/st/material/material', roles: R.ST_MATERIAL },
      { key: 'purchase', label: '采购管理', icon: '🛒', page: '/pages/st/purchase/purchase', roles: R.ST_PURCHASE },
      { key: 'inventory', label: '库存管理', icon: '📦', page: '/pages/st/inventory/inventory', roles: R.ST_INVENTORY },
      { key: 'statistics', label: '统计分析', icon: '📈', page: '/pages/st/statistics/statistics', roles: R.ST_STATS },
      { key: 'card', label: '餐卡管理', icon: '💳', page: '/pages/pay/card/card', roles: R.PAY_CARD }
    ]
  },
  {
    title: '系统功能',
    entries: [
      { key: 'todo', label: '待办中心', icon: '📋', page: '/pages/dashboard/todo/todo', roles: R.DASH_TODO },
      { key: 'message', label: '消息中心', icon: '🔔', page: '/pages/message/message', roles: R.MSG },
      { key: 'broadcast', label: '发布全体消息', icon: '📢', page: '/pages/system/broadcast/broadcast', roles: R.BROADCAST },
      { key: 'log', label: '操作日志', icon: '📜', page: '/pages/system/log/log', roles: R.LOG },
      { key: 'leadership', label: '领导驾驶舱', icon: '📊', page: '/pages/dashboard/leadership/leadership', roles: R.DASH_LEADER }
    ]
  }
]

// 快捷入口：仅展示"已有对应页面"且当前账号有权限的入口（其余模块页面后续开发中）
const implementedPages = ['/pages/gy/occupant/occupant', '/pages/gc/asset_apply/asset_apply', '/pages/st/meal/meal', '/pages/gc/borrow/borrow', '/pages/cl/apply/apply', '/pages/gc/return_apply/return_apply', '/pages/gc/transfer_apply/transfer_apply', '/pages/gc/dispose_apply/dispose_apply', '/pages/cl/vehicle/vehicle', '/pages/cl/repair/repair', '/pages/cl/dispatch/dispatch', '/pages/cl/cost/cost', '/pages/cl/cost_summary/cost_summary', '/pages/cl/track/track', '/pages/gy/room/room', '/pages/gy/cleaning/cleaning', '/pages/gy/repair/repair', '/pages/st/material/material', '/pages/st/purchase/purchase', '/pages/st/inventory/inventory', '/pages/st/statistics/statistics', '/pages/system/broadcast/broadcast', '/pages/system/log/log', '/pages/message/message', '/pages/dashboard/todo/todo', '/pages/dashboard/leadership/leadership', '/pages/gc/asset_list/asset_list', '/pages/pay/card/card', '/pages/st/meal_statistics/meal_statistics']
/** 计算每组列数：按数量选最优列数，让组内尽量排满、避免零星空格 */
const calcCols = (n) => {
  if (n <= 5) return n || 1
  if (n === 6) return 3 // 3×2 满格
  if (n <= 8) return 4 // 4+3 / 4×2
  return 5
}
const visibleGroups = quickGroups
  .map((g) => {
    const entries = g.entries.filter((e) => implementedPages.includes(e.page) && hasRole(e.roles))
    return { ...g, entries, cols: calcCols(entries.length) }
  })
  .filter((g) => g.entries.length > 0)

const todos = ref([])
const messages = ref([])
const unreadCount = ref(0)

const currentDate = new Date().toLocaleDateString('zh-CN', { weekday: 'long', month: 'long', day: 'numeric' })

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

const loadData = async () => {
  try {
    const [todoData, msgData, unread] = await Promise.all([
      request({ url: '/api/dashboard/todos' }),
      request({ url: '/api/message/latest', data: { limit: 5 } }),
      request({ url: '/api/message/unread-count' })
    ])
    todos.value = todoData || []
    messages.value = msgData || []
    unreadCount.value = unread || 0
  } catch (e) {
    // 错误已统一提示
  }
}

const handleEntry = (entry) => {
  uni.navigateTo({ url: entry.page })
}

const goMessage = () => uni.navigateTo({ url: '/pages/message/message' })
const goTodos = () => uni.navigateTo({ url: '/pages/dashboard/todo/todo' })

onShow(() => {
  loadData()
})
</script>

<style>
.home { min-height: 100vh; background: #f5f7fa; padding: 24rpx; box-sizing: border-box; }
.banner {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 20rpx;
  padding: 36rpx 32rpx;
  color: #fff;
}
.banner-greet { font-size: 40rpx; font-weight: 700; }
.banner-date { font-size: 24rpx; opacity: .85; margin: 8rpx 0 20rpx; }
.banner-tag {
  display: inline-block;
  background: rgba(255,255,255,.2);
  border-radius: 30rpx;
  padding: 10rpx 24rpx;
  font-size: 24rpx;
}
.quick-group { margin-top: 24rpx; }
.quick-group-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #666;
  margin: 0 8rpx 12rpx;
}
.quick-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx 8rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
}
.quick-icon { font-size: 44rpx; }
.quick-label { font-size: 22rpx; color: #333; margin-top: 8rpx; }
.section-card {
  background: #fff;
  border-radius: 20rpx;
  margin-top: 24rpx;
  padding: 28rpx 24rpx;
}
.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}
.section-more { font-size: 24rpx; color: #409eff; font-weight: 400; }
.todo-item, .msg-item { padding: 16rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.todo-line, .msg-line { display: flex; justify-content: space-between; align-items: center; }
.todo-title, .msg-title { font-size: 28rpx; color: #333; flex: 1; margin-right: 16rpx; }
.todo-meta { display: flex; justify-content: space-between; margin-top: 8rpx; }
.todo-status { font-size: 22rpx; color: #e6a23c; }
.todo-time, .msg-time { font-size: 22rpx; color: #999; }
.msg-dot { color: #f56c6c; font-size: 20rpx; }
.empty { text-align: center; color: #999; padding: 80rpx 0; font-size: 26rpx; }
</style>