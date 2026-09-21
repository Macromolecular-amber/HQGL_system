<template>
  <view class="dash-page">
    <view class="card">
      <view class="card-title"><text>📋 待办审批</text><u-button size="mini" type="primary" plain @click="loadList">刷新</u-button></view>
      <view class="list-item" v-for="todo in todos" :key="todo.id" @click="handleClick(todo)">
        <view class="item-head">
          <text class="item-no">{{ todo.title }}</text>
          <view class="tags">
            <u-tag :text="todo.module" :type="moduleType(todo.module)" size="mini" />
            <u-tag text="待处理" type="warning" size="mini" />
          </view>
        </view>
        <view class="item-line"><text class="label">时间</text><text class="value">{{ todo.time || '-' }}</text></view>
      </view>
      <view v-if="!loading && !todos.length" class="empty">暂无待办事项</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/utils/request'

const loading = ref(false)
const todos = ref([])

const moduleType = (m) => ({ 公物仓: 'primary', 用车: 'success', 公寓: 'warning', 食堂: 'error' }[m] || 'info')

const handleClick = (todo) => {
  // 无路径时按模块回退到对应功能页
  const modulePathMap = {
    公物仓: '/pages/gc/asset_list/asset_list',
    用车: '/pages/cl/apply/apply',
    公寓: '/pages/gy/occupant/occupant',
    食堂: '/pages/st/purchase/purchase'
  }
  const path = (todo && todo.path) || modulePathMap[todo && todo.module] || '/pages/index/index'
  if (path.startsWith('/pages/')) {
    uni.navigateTo({ url: path })
  }
}

const loadList = async () => {
  loading.value = true
  try {
    todos.value = (await request({ url: '/api/dashboard/todos' })) || []
  } catch (e) {
    todos.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>

<style>
.dash-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; flex: 1; margin-right: 16rpx; }
.tags { display: flex; gap: 10rpx; flex-shrink: 0; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>