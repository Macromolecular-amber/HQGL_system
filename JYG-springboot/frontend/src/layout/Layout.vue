<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="layout-aside">
      <div class="logo-area">
        <span class="logo-text">🏛️ 后勤管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#fff"
        class="el-menu-vertical"
      >
        <el-menu-item index="/">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item v-if="canSeeLeadership" index="/dashboard/leadership">
          <el-icon><Monitor /></el-icon>
          <span>领导驾驶舱</span>
        </el-menu-item>
        <el-sub-menu v-for="sub in visibleMenus" :key="sub.path" :index="sub.path">
          <template #title>
            <el-icon><component :is="sub.icon" /></el-icon>
            <span>{{ sub.title }}</span>
          </template>
          <el-menu-item v-for="child in sub.children" :key="child.path" :index="child.path">
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span class="page-title">{{ $route.meta.title || '' }}</span>
        <div class="header-right">
          <el-popover
            v-model:visible="msgPanelVisible"
            placement="bottom-end"
            :width="320"
            trigger="click"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="msg-badge">
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
          <span class="username">{{ userInfo.realName || '用户' }}</span>
          <el-button
            type="danger"
            plain
            size="small"
            class="logout-btn"
            @click="handleLogout"
          >
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, SwitchButton, Monitor } from '@element-plus/icons-vue'
import { allMenus } from '@/config/menu'
import { getUnreadCount, getLatestMessages } from '@/api/message'

const router = useRouter()

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const roles = userInfo.roles || []

/** 领导驾驶舱入口：仅 ADMIN / DIRECTOR / BIZ_ADMIN 可见 */
const canSeeLeadership = computed(() =>
  roles.includes('ADMIN') || roles.includes('DIRECTOR') || roles.includes('BIZ_ADMIN')
)

/** 按角色过滤菜单：ADMIN 全显，其余按菜单项 roles 匹配 */
const visibleMenus = computed(() => {
  if (roles.includes('ADMIN')) {
    return allMenus
  }
  return allMenus
    .map((sub) => ({
      ...sub,
      children: sub.children.filter((child) => child.roles && child.roles.some((r) => roles.includes(r)))
    }))
    .filter((sub) => sub.children.length > 0)
})

/** 消息铃铛：未读数 + 最新消息下拉 */
const msgPanelVisible = ref(false)
const msgLoading = ref(false)
const unreadCount = ref(0)
const latestMessages = ref([])

const loadUnreadCount = async () => {
  try {
    unreadCount.value = (await getUnreadCount()) || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

const loadLatestMessages = async () => {
  msgLoading.value = true
  try {
    latestMessages.value = (await getLatestMessages(5)) || []
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    msgLoading.value = false
  }
}

const goMessageCenter = () => {
  msgPanelVisible.value = false
  router.push('/message')
}

/** 退出登录：确认后清除本地登录态并跳转登录页 */
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {
      // 用户取消，不做任何操作
    })
}

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

/** 未读数定时刷新间隔（毫秒） */
const UNREAD_REFRESH_INTERVAL = 30000

let unreadTimer = null

onMounted(() => {
  loadUnreadCount()
  unreadTimer = setInterval(loadUnreadCount, UNREAD_REFRESH_INTERVAL)
})

onBeforeUnmount(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
})

/** 路由切换后刷新未读数并收起消息面板 */
watch(
  () => router.currentRoute.value.path,
  () => {
    loadUnreadCount()
    msgPanelVisible.value = false
  }
)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #1f2d3d;

  .logo-area {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;

    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      letter-spacing: 2px;
    }
  }

  :deep(.el-menu) {
    border-right: none;

    .el-menu-item.is-active {
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3)) !important;
      border-right: 3px solid #667eea;
    }
  }
}

.layout-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

.layout-main {
  padding: 24px;
  background: #f0f2f5;
  overflow-y: auto;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: #333;
}

.logout-btn {
  margin-left: 4px;
}

.msg-badge {
  line-height: 1;
}

.msg-bell {
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  color: #606266;
}

.msg-bell:hover {
  color: #409eff;
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
</style>

<!-- 路由切换过渡动画（全局类名，作用于被渲染的页面根节点） -->
<style>
.fade-slide-enter-active {
  transition: all 0.3s ease;
}
.fade-slide-leave-active {
  transition: all 0.2s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
