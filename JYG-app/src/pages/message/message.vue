<template>
  <view class="msg-page">
    <view class="msg-item" v-for="m in list" :key="m.id" @click="handleMarkRead(m)">
      <view class="msg-head">
        <text class="msg-title" :class="{ unread: !m.isRead }">{{ m.title }}</text>
        <text v-if="!m.isRead" class="badge">未读</text>
      </view>
      <view class="msg-content">{{ m.content || m.title }}</view>
      <view class="msg-meta">
        <text class="msg-time">{{ formatTime(m.createTime) }}</text>
        <text class="msg-sender">{{ m.senderName || '系统' }}</text>
      </view>
    </view>

    <view class="empty" v-if="!loading && list.length === 0">暂无消息</view>
    <view class="loading" v-if="loading">加载中...</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const list = ref([])
const loading = ref(false)
const page = 1

const loadData = async () => {
  loading.value = true
  try {
    const res = await request({ url: '/api/message/page', data: { page, size: 20 } })
    list.value = res.data || []
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

const handleMarkRead = async (m) => {
  if (m.isRead) return
  try {
    await request({ url: `/api/message/read/${m.id}`, method: 'PUT' })
    m.isRead = true
  } catch (e) {
    // 忽略
  }
}

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '-')

onShow(() => {
  loadData()
})
</script>

<style>
.msg-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.msg-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}
.msg-head { display: flex; justify-content: space-between; align-items: center; }
.msg-title { font-size: 30rpx; color: #333; flex: 1; }
.msg-title.unread { font-weight: 700; }
.badge {
  font-size: 20rpx;
  color: #fff;
  background: #f56c6c;
  border-radius: 20rpx;
  padding: 4rpx 16rpx;
}
.msg-content {
  font-size: 26rpx;
  color: #666;
  margin: 12rpx 0;
  line-height: 1.6;
}
.msg-meta { display: flex; justify-content: space-between; font-size: 22rpx; color: #999; }
.empty, .loading { text-align: center; color: #999; padding: 120rpx 0; font-size: 26rpx; }
</style>