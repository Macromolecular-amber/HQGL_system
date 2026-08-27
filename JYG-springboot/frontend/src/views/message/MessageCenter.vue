<template>
  <div class="message-center">
    <!-- 顶部：标题 + 统计 -->
    <div class="header">
      <div class="header-left">
        <h2>📬 消息中心</h2>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" type="danger">
          <span class="unread-text">未读消息</span>
        </el-badge>
      </div>
      <div class="header-right">
        <el-button type="primary" plain size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
          全部已读
        </el-button>
        <el-button size="small" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filters">
      <el-select v-model="query.type" placeholder="消息类型" clearable @change="handleSearch">
        <el-option label="全部类型" value="" />
        <el-option label="系统通知" value="SYSTEM" />
        <el-option label="业务提醒" value="BUSINESS" />
        <el-option label="预警提醒" value="WARNING" />
        <el-option label="审批通知" value="APPROVAL" />
      </el-select>
      <el-select v-model="query.readStatus" placeholder="阅读状态" clearable @change="handleSearch">
        <el-option label="全部状态" value="" />
        <el-option label="未读" value="false" />
        <el-option label="已读" value="true" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        @change="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 消息列表 -->
    <div class="message-list">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-item"
        :class="{ 'is-read': msg.isRead, 'is-unread': !msg.isRead }"
        @click="handleClickMessage(msg)"
      >
        <div class="msg-left">
          <div class="msg-dot" :class="{ 'unread-dot': !msg.isRead }"></div>
          <div class="msg-content">
            <div class="msg-title">
              <span>{{ msg.title }}</span>
              <el-tag :type="getTypeTag(msg.messageType)" size="small">{{ getTypeLabel(msg.messageType) }}</el-tag>
            </div>
            <div class="msg-preview">{{ msg.content || msg.title }}</div>
            <div class="msg-meta">
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
              <span v-if="msg.bizModule" class="msg-module">{{ msg.bizModule }}</span>
              <span v-if="msg.senderName" class="msg-sender">发送人：{{ msg.senderName }}</span>
            </div>
          </div>
        </div>
        <div class="msg-right">
          <el-button
            v-if="!msg.isRead"
            type="primary"
            size="small"
            plain
            @click.stop="handleMarkRead(msg.id)"
          >
            标记已读
          </el-button>
          <span v-else class="read-tag">已读</span>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="messages.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无消息" />
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:page-size="query.size"
          v-model:current-page="query.page"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="loadMessages"
        />
      </div>
    </div>

    <!-- 消息详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="detailMessage?.title || '消息详情'"
      width="600px"
      :close-on-click-modal="true"
    >
      <div class="detail-content" v-if="detailMessage">
        <div class="detail-meta">
          <el-tag :type="getTypeTag(detailMessage.messageType)" size="default">
            {{ getTypeLabel(detailMessage.messageType) }}
          </el-tag>
          <span class="detail-time">{{ formatTime(detailMessage.createTime) }}</span>
          <span v-if="detailMessage.senderName" class="detail-sender">发送人：{{ detailMessage.senderName }}</span>
        </div>
        <div class="detail-body">
          <div class="detail-title">{{ detailMessage.title }}</div>
          <div class="detail-content-text">{{ detailMessage.content || detailMessage.title }}</div>
          <div v-if="detailMessage.bizModule || detailMessage.bizOrderNo" class="detail-biz">
            <span>关联业务：</span>
            <span>{{ detailMessage.bizModule || '' }}</span>
            <span v-if="detailMessage.bizOrderNo"> - {{ detailMessage.bizOrderNo }}</span>
            <el-button
              v-if="detailMessage.relativeUrl"
              type="primary"
              size="small"
              @click="handleNavigateToBiz(detailMessage)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailMessage && !detailMessage.isRead"
          type="primary"
          @click="handleMarkReadFromDetail"
        >
          标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getMessagePage, markAsRead, markAllAsRead, getUnreadCount } from '@/api/message'

export default {
  name: 'MessageCenter',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const messages = ref([])
    const total = ref(0)
    const unreadCount = ref(0)
    const detailVisible = ref(false)
    const detailMessage = ref(null)
    const dateRange = ref([])

    const query = reactive({
      type: '',
      readStatus: '',
      page: 1,
      size: 20
    })

    // 加载消息列表（request 拦截器已解包 Result，直接返回 PageResult）
    const loadMessages = async () => {
      loading.value = true
      try {
        const params = {
          page: query.page,
          size: query.size,
          messageType: query.type || undefined,
          isRead: query.readStatus === '' ? undefined : query.readStatus === 'true'
        }
        if (dateRange.value && dateRange.value.length === 2) {
          params.startTime = dateRange.value[0]
          params.endTime = dateRange.value[1]
        }
        const res = await getMessagePage(params)
        messages.value = res.data || []
        total.value = res.total || 0
      } catch (e) {
        // 错误已由拦截器统一提示
      } finally {
        loading.value = false
      }
    }

    // 加载未读数量
    const loadUnreadCount = async () => {
      try {
        unreadCount.value = (await getUnreadCount()) || 0
      } catch (e) {
        // 静默失败
      }
    }

    // 查询
    const handleSearch = () => {
      query.page = 1
      loadMessages()
    }

    // 切换每页条数：回到第一页重新加载（当前页码由 v-model 更新）
    const handleSizeChange = () => {
      query.page = 1
      loadMessages()
    }

    // 重置
    const handleReset = () => {
      query.type = ''
      query.readStatus = ''
      dateRange.value = []
      query.page = 1
      loadMessages()
    }

    // 刷新
    const handleRefresh = () => {
      loadMessages()
      loadUnreadCount()
      ElMessage.success('已刷新')
    }

    // 标记单条已读
    const handleMarkRead = async (id) => {
      try {
        await markAsRead(id)
        ElMessage.success('已标记已读')
        loadMessages()
        loadUnreadCount()
      } catch (e) {
        // 错误已由拦截器统一提示
      }
    }

    // 全部已读
    const handleMarkAllRead = async () => {
      try {
        await markAllAsRead()
        ElMessage.success('全部已读')
        loadMessages()
        loadUnreadCount()
      } catch (e) {
        // 错误已由拦截器统一提示
      }
    }

    // 点击消息 -> 详情弹窗（未读自动标记已读）
    const handleClickMessage = (msg) => {
      detailMessage.value = msg
      detailVisible.value = true
      if (!msg.isRead) {
        msg.isRead = true
        handleMarkRead(msg.id)
      }
    }

    // 从详情弹窗标记已读
    const handleMarkReadFromDetail = () => {
      if (detailMessage.value) {
        detailMessage.value.isRead = true
        handleMarkRead(detailMessage.value.id)
      }
    }

    // 跳转到业务页面（先校验路由存在，避免跳转空白页）
    const handleNavigateToBiz = (msg) => {
      if (!msg.relativeUrl) {
        ElMessage.warning('该消息暂无可跳转的业务页面')
        return
      }
      const resolved = router.resolve(msg.relativeUrl)
      if (resolved.matched.length === 0) {
        ElMessage.warning('该消息暂无可跳转的业务页面')
        return
      }
      detailVisible.value = false
      router.push(msg.relativeUrl)
    }

    // 类型标签
    const getTypeTag = (type) => {
      const map = { SYSTEM: 'info', BUSINESS: 'warning', WARNING: 'danger', APPROVAL: 'primary' }
      return map[type] || 'info'
    }

    const getTypeLabel = (type) => {
      const map = { SYSTEM: '系统通知', BUSINESS: '业务提醒', WARNING: '预警提醒', APPROVAL: '审批通知' }
      return map[type] || type
    }

    // 时间格式化
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString('zh-CN', { hour12: false })
    }

    onMounted(() => {
      loadMessages()
      loadUnreadCount()
    })

    return {
      loading,
      messages,
      total,
      unreadCount,
      query,
      dateRange,
      detailVisible,
      detailMessage,
      loadMessages,
      handleSearch,
      handleSizeChange,
      handleReset,
      handleRefresh,
      handleMarkRead,
      handleMarkAllRead,
      handleClickMessage,
      handleMarkReadFromDetail,
      handleNavigateToBiz,
      getTypeTag,
      getTypeLabel,
      formatTime
    }
  }
}
</script>

<style scoped>
.message-center {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.header {
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
}

.unread-text {
  font-size: 14px;
  color: #666;
}

.header-right {
  display: flex;
  gap: 10px;
}

.filters {
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.filters .el-select {
  width: 150px;
}

.filters .el-date-picker {
  width: 360px;
}

.message-list {
  background: #fff;
  border-radius: 12px;
  padding: 8px 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.message-item {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s;
}

.message-item:hover {
  background: #f7f9fc;
}

.message-item.is-unread {
  background: #fafbff;
}

.message-item.is-read .msg-title {
  color: #999;
}

.msg-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.msg-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d9d9d9;
  margin-top: 6px;
  flex-shrink: 0;
}

.msg-dot.unread-dot {
  background: #409EFF;
}

.msg-content {
  flex: 1;
  min-width: 0;
}

.msg-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  margin-bottom: 4px;
}

.msg-title span {
  font-weight: 500;
}

.msg-preview {
  font-size: 14px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.msg-right {
  flex-shrink: 0;
  margin-left: 16px;
}

.read-tag {
  color: #999;
  font-size: 13px;
}

.empty-state {
  padding: 60px 0;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 40px 0;
  color: #999;
}

.pagination {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
}

/* 详情弹窗 */
.detail-content {
  padding: 8px 0;
}

.detail-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.detail-time, .detail-sender {
  font-size: 13px;
  color: #999;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.detail-content-text {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  background: #f7f9fc;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.detail-biz {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #666;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
