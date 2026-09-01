<template>
  <div class="dashboard">
    <!-- 顶部横幅 -->
    <div class="banner">
      <div class="banner-left">
        <h2>👋 欢迎回来，{{ userInfo.realName || '用户' }}！</h2>
        <span class="date">{{ currentDate }}</span>
      </div>
      <div class="banner-right">
        <el-tag type="success" size="large">系统运行正常</el-tag>
        <el-popover
          v-model:visible="msgPanelVisible"
          placement="bottom-end"
          :width="320"
          trigger="click"
        >
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon class="msg-bell" @click="loadLatestMessages"><Bell /></el-icon>
            </el-badge>
          </template>
          <div class="msg-panel">
            <div class="msg-panel-header">
              <span>消息通知</span>
              <el-button link type="primary" @click="goMessageCenter">查看更多</el-button>
            </div>
            <div v-loading="msgLoading" class="msg-panel-list">
              <div v-for="msg in latestMessages" :key="msg.id" class="msg-panel-item" @click="goMessageCenter">
                <span :class="['msg-panel-dot', { 'msg-panel-dot-unread': !msg.isRead }]"></span>
                <span :class="['msg-panel-title', { 'msg-panel-title-unread': !msg.isRead }]">{{ msg.title }}</span>
                <span class="msg-panel-time">{{ formatTime(msg.createTime) }}</span>
              </div>
              <el-empty v-if="!msgLoading && latestMessages.length === 0" description="暂无消息" :image-size="60" />
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-entries">
      <div class="entry-item" v-for="entry in quickEntries" :key="entry.key" @click="handleEntry(entry.path)">
        <el-icon :size="32"><component :is="entry.icon" /></el-icon>
        <span>{{ entry.label }}</span>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6" v-for="stat in statistics" :key="stat.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-icon" :style="{ color: stat.color }">
            <el-icon :size="24"><component :is="stat.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间区域：待办 + 消息 -->
    <el-row :gutter="20" class="middle-row">
      <el-col :span="14">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>📋 待办审批</span>
              <el-button link type="primary" @click="goToTodos">查看全部</el-button>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todos" :key="todo.id" class="todo-item" @click="handleTodoClick(todo)">
              <div class="todo-title" :title="todo.title">{{ todo.title }}</div>
              <div class="todo-meta">
                <div class="todo-tags">
                  <el-tag :type="getTagType(todo.module)" size="small">{{ todo.module }}</el-tag>
                  <el-tag type="warning" size="small">{{ todo.status }}</el-tag>
                </div>
                <span class="todo-time">{{ todo.time }}</span>
              </div>
            </div>
            <div v-if="todos.length === 0" class="empty-state">暂无待办事项</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="msg-card">
          <template #header>
            <div class="card-header">
              <span>💬 消息通知</span>
              <el-button link type="primary" @click="goToMessages">查看全部</el-button>
            </div>
          </template>
          <div class="msg-list">
            <div v-for="msg in messages" :key="msg.id" class="msg-item">
              <div class="msg-title" :title="msg.title">{{ msg.title }}</div>
              <div class="msg-meta">
                <el-tag :type="getMsgTagType(msg.type)" size="small">{{ msg.type }}</el-tag>
                <span class="msg-time">{{ msg.time }}</span>
              </div>
            </div>
            <div v-if="messages.length === 0" class="empty-state">暂无消息</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部趋势图 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 近7天资产趋势</span>
            </div>
          </template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getStatistics, getTodos, getMessages, getTrend } from '@/api/dashboard'
import { getUnreadCount, getLatestMessages } from '@/api/message'

export default {
  name: 'Dashboard',
  setup() {
    const router = useRouter()
    const chartRef = ref(null)
    let chartInstance = null

    // 用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{"realName":"用户"}')

    // 当前日期
    const currentDate = computed(() => {
      const now = new Date()
      return now.toLocaleString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'long',
        hour: '2-digit',
        minute: '2-digit'
      })
    })

    // 快捷入口
    const quickEntries = [
      { key: 'gc-apply', label: '资产入仓', icon: 'Box', path: '/gc/asset-apply' },
      { key: 'car-apply', label: '用车申请', icon: 'Van', path: '/cl/apply' },
      { key: 'room-apply', label: '公寓申请', icon: 'OfficeBuilding', path: '/gy/occupant' },
      { key: 'meal-reserve', label: '预约订餐', icon: 'Dish', path: '/st/meal-reserve' },
      { key: 'gc-borrow', label: '借用申请', icon: 'ShoppingTrolley', path: '/gc/borrow-apply' },
      { key: 'room-query', label: '房间查询', icon: 'House', path: '/gy/room' },
      { key: 'repair', label: '报修服务', icon: 'Tools', path: '/cl/repair' },
      { key: 'message', label: '消息中心', icon: 'Bell', path: '/message' },
    ]

    // 统计数据
    const statistics = ref([])

    // 待办列表
    const todos = ref([])

    // 消息列表
    const messages = ref([])

    // 消息铃铛：未读数 + 最新消息下拉
    const msgPanelVisible = ref(false)
    const msgLoading = ref(false)
    const unreadCount = ref(0)
    const latestMessages = ref([])

    const loadUnreadCount = async () => {
      try {
        unreadCount.value = (await getUnreadCount()) || 0
      } catch (e) {
        console.warn('未读数加载失败', e)
      }
    }

    const loadLatestMessages = async () => {
      msgLoading.value = true
      try {
        latestMessages.value = (await getLatestMessages(5)) || []
      } catch (e) {
        console.warn('最新消息加载失败', e)
      } finally {
        msgLoading.value = false
      }
    }

    const goMessageCenter = () => {
      msgPanelVisible.value = false
      router.push('/message')
    }

    const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

    // 加载数据
    const loadData = async () => {
      try {
        const [stat, todoData, msgData, trend] = await Promise.all([
          getStatistics(),
          getTodos(),
          getMessages(),
          getTrend()
        ])

        if (stat) {
          statistics.value = [
            { key: 'total', label: '总资产', value: stat.totalAssets || 0, icon: 'Box', color: '#409EFF' },
            { key: 'inStock', label: '在仓资产', value: stat.inStockAssets || 0, icon: 'CircleCheck', color: '#67C23A' },
            { key: 'borrowed', label: '已借用', value: stat.borrowedAssets || 0, icon: 'Clock', color: '#E6A23C' },
            { key: 'pending', label: '待审批', value: stat.pendingApprovals || 0, icon: 'Warning', color: '#F56C6C' },
          ]
        }

        // 待办审批预览最多显示 5 条，与消息通知保持一致
        todos.value = (todoData || []).slice(0, 5)

        messages.value = msgData || []

        if (trend) {
          renderChart(trend)
        }
      } catch (e) {
        console.warn('首页数据加载失败（使用默认数据）', e)
        // 使用内置默认数据
        statistics.value = [
          { key: 'total', label: '总资产', value: 156, icon: 'Box', color: '#409EFF' },
          { key: 'inStock', label: '在仓资产', value: 89, icon: 'CircleCheck', color: '#67C23A' },
          { key: 'borrowed', label: '已借用', value: 45, icon: 'Clock', color: '#E6A23C' },
          { key: 'pending', label: '待审批', value: 12, icon: 'Warning', color: '#F56C6C' },
        ]
        todos.value = [
          { id: 1, title: '资产入仓申请 #GLZ-2026-0008', module: '公物仓', time: '2026-08-23 09:30', status: '待审核' },
          { id: 2, title: '用车申请 #CL20260819001', module: '用车', time: '2026-08-23 08:45', status: '待审批' },
        ]
        messages.value = [
          { id: 1, title: '系统升级通知：将于本周末进行维护', time: '2026-08-23 10:00', type: '系统通知' },
          { id: 2, title: '公物仓资产盘点提醒：本月盘点截止日期为8月31日', time: '2026-08-23 09:00', type: '业务提醒' },
        ]
      }
    }

    // 渲染图表
    const renderChart = (data) => {
      if (!chartRef.value) return
      if (!chartInstance) {
        chartInstance = echarts.init(chartRef.value)
      }
      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: ['入仓', '借用'] },
        xAxis: {
          type: 'category',
          data: data.dates || ['8/17', '8/18', '8/19', '8/20', '8/21', '8/22', '8/23']
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '入仓',
            type: 'line',
            smooth: true,
            data: data.inStock || [5, 8, 12, 6, 9, 7, 4],
            lineStyle: { color: '#67C23A' },
            itemStyle: { color: '#67C23A' },
            areaStyle: { color: 'rgba(103, 194, 58, 0.2)' }
          },
          {
            name: '借用',
            type: 'line',
            smooth: true,
            data: data.borrowed || [3, 5, 4, 7, 6, 8, 5],
            lineStyle: { color: '#409EFF' },
            itemStyle: { color: '#409EFF' },
            areaStyle: { color: 'rgba(64, 158, 255, 0.2)' }
          }
        ]
      }
      chartInstance.setOption(option)
      chartInstance.resize()
    }

    // 窗口自适应
    const handleResize = () => {
      if (chartInstance) chartInstance.resize()
    }

    // 快捷入口点击
    const handleEntry = (path) => {
      router.push(path)
    }

    // 待办模块标签颜色
    const getTagType = (module) => {
      const map = { '公物仓': 'primary', '用车': 'success', '公寓': 'warning', '食堂': 'danger' }
      return map[module] || 'info'
    }

    // 消息类型标签颜色
    const getMsgTagType = (type) => {
      const map = { '系统通知': 'info', '业务提醒': 'warning', '预警提醒': 'danger' }
      return map[type] || 'info'
    }

    const goToTodos = () => {
      router.push('/todos')
    }

    const handleTodoClick = (todo) => {
      if (todo && todo.path) {
        router.push(todo.path)
        return
      }
      // 无路径时按模块回退到对应功能页
      const modulePathMap = {
        '公物仓': '/gc/borrow-apply',
        '用车': '/cl/apply',
        '公寓': '/gy/occupant',
        '食堂': '/st/purchase'
      }
      router.push((todo && modulePathMap[todo.module]) || '/todos')
    }

    const goToMessages = () => {
      router.push('/message')
    }

    onMounted(() => {
      loadData()
      loadUnreadCount()
      window.addEventListener('resize', handleResize)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
      if (chartInstance) {
        chartInstance.dispose()
        chartInstance = null
      }
    })

    return {
      userInfo,
      currentDate,
      quickEntries,
      statistics,
      todos,
      messages,
      chartRef,
      msgPanelVisible,
      msgLoading,
      unreadCount,
      latestMessages,
      loadLatestMessages,
      goMessageCenter,
      formatTime,
      handleEntry,
      getTagType,
      getMsgTagType,
      goToTodos,
      handleTodoClick,
      goToMessages
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 0;
  background: #f0f2f5;
  min-height: 100vh;
}

/* 顶部横幅（渐变流动动画） */
.banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 200% 200%;
  animation: gradientMove 4s ease infinite;
  border-radius: 16px;
  padding: 28px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  margin-bottom: 24px;
}
@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.banner-left h2 {
  margin: 0 0 4px 0;
  font-weight: 500;
}
.banner-left .date {
  font-size: 14px;
  opacity: 0.85;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.msg-bell {
  font-size: 20px;
  color: #fff;
  cursor: pointer;
  opacity: 0.9;
}

.msg-bell:hover {
  opacity: 1;
}

.msg-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  margin-bottom: 4px;
}

.msg-panel-list {
  max-height: 320px;
  overflow-y: auto;
}

.msg-panel-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}

.msg-panel-item:hover {
  background: #f7f9fc;
}

.msg-panel-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: transparent;
  flex-shrink: 0;
}

.msg-panel-dot-unread {
  background: #f56c6c;
}

.msg-panel-title {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-panel-title-unread {
  font-weight: 600;
  color: #000;
}

.msg-panel-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

/* 快捷入口 */
.quick-entries {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #333;
  min-width: 0;
  flex: 1;
}
.entry-item:hover {
  background: var(--primary-light);
  color: var(--primary);
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}
.entry-item .el-icon {
  color: var(--primary);
  font-size: 28px;
}
.entry-item span {
  font-size: 12px;
  white-space: nowrap;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  padding: 8px 0;
}
.stat-card .stat-value {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-primary);
}
.stat-card .stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}
.stat-card .stat-icon {
  position: absolute;
  right: 16px;
  top: 16px;
  opacity: 0.8;
}

/* 中间区域：待办审批与消息通知等高 */
.middle-row {
  margin-bottom: 20px;
  align-items: stretch;
}
.middle-row .el-col {
  display: flex;
}
.middle-row .el-col > .el-card {
  flex: 1;
  width: 100%;
  display: flex;
  flex-direction: column;
}
.middle-row .el-col > .el-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header span {
  font-weight: 500;
  font-size: 16px;
}

/* 待办列表与消息列表：等高填充并可滚动 */
.todo-list, .msg-list {
  flex: 1;
  min-height: 200px;
  overflow-y: auto;
}
.todo-item {
  padding: 10px 12px;
  border-radius: 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}
.todo-item:hover {
  background: var(--primary-light);
}
.todo-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.todo-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.todo-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.todo-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

/* 消息列表 */
.msg-item {
  padding: 10px 12px;
  border-radius: 8px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.15s;
}
.msg-item:hover {
  background: var(--primary-light);
}
.msg-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.msg-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.msg-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

/* 图表 */
.chart-container {
  width: 100%;
  height: 280px;
  background: #fff;
  border-radius: 16px;
  padding: 8px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  color: #999;
  padding: 30px 0;
  font-size: 14px;
}
</style>
