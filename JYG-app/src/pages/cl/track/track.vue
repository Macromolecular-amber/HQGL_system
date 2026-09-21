<template>
  <view class="cl-page">
    <view class="card">
      <view class="card-title"><text>🛰️ 车辆运行监控</text><text class="total">出车中 {{ vehicles.length }} 辆</text></view>
      <u-button size="mini" type="primary" plain @click="loadVehicles">刷新</u-button>
      <view class="list-item" v-for="v in vehicles" :key="v.vehicleId">
        <view class="item-head">
          <text class="item-no">{{ v.plateNumber }}</text>
          <u-tag :text="v.statusLabel || v.status" :type="statusType(v.status)" size="mini" />
        </view>
        <view class="item-line"><text class="label">驾驶员</text><text class="value">{{ v.driverName || '-' }}</text></view>
        <view class="item-line"><text class="label">位置</text><text class="value">{{ v.lng ? `${v.lng.toFixed(4)}, ${v.lat.toFixed(4)}` : '暂无轨迹' }}</text></view>
        <view class="item-line"><text class="label">速度</text><text class="value">{{ v.speed ? v.speed + ' km/h' : '-' }}</text></view>
        <view class="item-line"><text class="label">目的地</text><text class="value">{{ v.destination || '-' }}</text></view>
        <view class="item-line"><text class="label">上报时间</text><text class="value">{{ formatTime(v.lastUpdateTime) }}</text></view>
        <view class="item-actions">
          <u-button size="mini" type="danger" plain @click="simulate(v)">模拟轨迹(测试)</u-button>
          <u-button size="mini" type="primary" plain @click="openReplay(v)">轨迹回放</u-button>
        </view>
      </view>
      <view v-if="!loading && !vehicles.length" class="empty">暂无出车中车辆</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 轨迹回放弹窗 -->
    <u-popup :show="replayVisible" mode="bottom" round :closeOnClickOverlay="true" @close="replayVisible = false" height="70%">
      <view class="popup-panel">
        <view class="popup-title">轨迹回放</view>
        <view class="popup-no">{{ replayForm.plateNumber }}</view>
        <view class="date-row">
          <u-button size="mini" plain @click="shiftStart(-1)">开始-1h</u-button>
          <u-button size="mini" plain @click="shiftStart(1)">开始+1h</u-button>
          <u-button size="mini" plain @click="shiftEnd(-1)">结束-1h</u-button>
          <u-button size="mini" plain @click="shiftEnd(1)">结束+1h</u-button>
        </view>
        <view class="detail-row"><text class="d-label">时间段</text><text class="d-value">{{ replayForm.startTime }} ~ {{ replayForm.endTime }}</text></view>
        <view class="popup-actions">
          <u-button @click="replayVisible = false">关闭</u-button>
          <u-button type="primary" :loading="querying" @click="queryReplay">查询轨迹</u-button>
        </view>
        <view class="sub-title" v-if="points.length">轨迹点（{{ points.length }}）</view>
        <view class="vehicle-item" v-for="(p, i) in points" :key="i">
          {{ formatTime(p.recordTime) }} · {{ p.lng.toFixed(4) }}, {{ p.lat.toFixed(4) }} · {{ p.speed ?? '-' }}km/h
        </view>
        <view v-if="querying" class="empty-mini">加载中...</view>
        <view v-else-if="queryDone && !points.length" class="empty-mini">该时间段暂无轨迹数据</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { request } from '@/utils/request'

const statusMap = { ONGOING: { type: 'primary' }, WAITING: { type: 'warning' }, RETURNED: { type: 'success' }, CANCELLED: { type: 'info' } }
const statusType = (s) => (statusMap[s] || { type: 'primary' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatDateTime = (d) => { const p = (n) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}` }
const parseDT = (s) => new Date(s.replace('T', ' ').replace(/-/g, '/'))

const loading = ref(false)
const vehicles = ref([])

const loadVehicles = async () => {
  loading.value = true
  try { vehicles.value = (await request({ url: '/api/cl/track/current/all' })) || [] } catch (e) { /* 错误已统一提示 */ } finally { loading.value = false }
}

/** 模拟轨迹 */
const simulate = async (v) => {
  uni.showModal({
    title: '提示', content: `为「${v.plateNumber}」生成模拟轨迹？`, confirmText: '生成', cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/cl/track/simulate/${v.dispatchId}`, method: 'POST' })
        uni.showToast({ title: '模拟轨迹已生成', icon: 'success' })
        loadVehicles()
      } catch (e) { /* 错误已统一提示 */ }
    }
  })
}

/** 轨迹回放 */
const replayVisible = ref(false)
const querying = ref(false)
const queryDone = ref(false)
const points = ref([])
const replayForm = reactive({ vehicleId: null, plateNumber: '', startTime: '', endTime: '' })

const openReplay = (v) => {
  replayForm.vehicleId = v.vehicleId
  replayForm.plateNumber = v.plateNumber
  const now = new Date()
  const start = new Date(now.getTime() - 60 * 60 * 1000)
  replayForm.startTime = formatDateTime(start)
  replayForm.endTime = formatDateTime(now)
  points.value = []
  queryDone.value = false
  replayVisible.value = true
}

const shiftStart = (h) => {
  const d = parseDT(replayForm.startTime)
  d.setHours(d.getHours() + h)
  replayForm.startTime = formatDateTime(d)
}
const shiftEnd = (h) => {
  const d = parseDT(replayForm.endTime)
  d.setHours(d.getHours() + h)
  replayForm.endTime = formatDateTime(d)
}

const queryReplay = async () => {
  querying.value = true
  queryDone.value = false
  try {
    const vo = await request({ url: '/api/cl/track/history', method: 'POST', data: { vehicleId: replayForm.vehicleId, startTime: replayForm.startTime, endTime: replayForm.endTime } })
    points.value = (vo && vo.points) || []
    queryDone.value = true
  } catch (e) { /* 错误已统一提示 */ } finally { querying.value = false }
}

onMounted(() => { loadVehicles() })
</script>

<style>
.cl-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.empty-mini { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.date-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
.sub-title { font-size: 30rpx; font-weight: 600; margin: 24rpx 0 12rpx; }
.vehicle-item { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx 20rpx; font-size: 26rpx; color: #333; margin-bottom: 10rpx; }
</style>