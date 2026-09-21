<template>
  <view class="sys-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.username" placeholder="按用户名搜索" :showAction="true" actionText="查询" @search="handleSearch" @custom="handleSearch"></u-search>
      <view class="filter-row">
        <u-tag v-for="m in moduleOptions" :key="m.value" :text="m.label" :type="query.module === m.value ? 'primary' : 'info'" size="mini" plain @click="toggleModule(m.value)" />
      </view>
    </view>

    <!-- 日志列表 -->
    <view class="card">
      <view class="card-title"><text>📜 操作日志</text><text class="total">共 {{ total }} 条</text></view>
      <view class="list-item" v-for="row in logs" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.realName || row.username }}</text>
          <view class="tags">
            <u-tag :text="moduleLabel(row.module)" size="mini" />
            <u-tag :text="typeLabel(row.operationType)" :type="typeTag(row.operationType)" size="mini" />
          </view>
        </view>
        <view class="item-line"><text class="label">描述</text><text class="value">{{ row.operationDesc }}</text></view>
        <view class="item-line"><text class="label">IP</text><text class="value">{{ row.clientIp }} · {{ row.costTime || 0 }}ms</text></view>
        <view class="item-line"><text class="label">时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
      </view>
      <view v-if="!loading && !logs.length" class="empty">暂无日志</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">日志详情</view>
        <view class="detail-row"><text class="d-label">用户名</text><text class="d-value">{{ detail.username }}</text></view>
        <view class="detail-row"><text class="d-label">姓名</text><text class="d-value">{{ detail.realName }}</text></view>
        <view class="detail-row"><text class="d-label">模块</text><text class="d-value">{{ moduleLabel(detail.module) }}</text></view>
        <view class="detail-row"><text class="d-label">操作类型</text><text class="d-value">{{ typeLabel(detail.operationType) }}</text></view>
        <view class="detail-row"><text class="d-label">操作描述</text><text class="d-value">{{ detail.operationDesc }}</text></view>
        <view class="detail-row"><text class="d-label">请求URL</text><text class="d-value">{{ detail.requestUrl }}</text></view>
        <view class="detail-row" v-if="detail.requestParams"><text class="d-label">请求参数</text><text class="d-value">{{ detail.requestParams }}</text></view>
        <view class="detail-row"><text class="d-label">IP</text><text class="d-value">{{ detail.clientIp }}</text></view>
        <view class="detail-row"><text class="d-label">耗时</text><text class="d-value">{{ detail.costTime }}ms</text></view>
        <view class="detail-row"><text class="d-label">响应码</text><text class="d-value">{{ detail.responseCode }}</text></view>
        <view class="detail-row" v-if="detail.exceptionMsg"><text class="d-label">错误信息</text><text class="d-value">{{ detail.exceptionMsg }}</text></view>
        <view class="detail-row"><text class="d-label">操作时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const moduleOptions = [
  { value: '', label: '全部' },
  { value: 'GC', label: '公物仓' },
  { value: 'CL', label: '用车' },
  { value: 'GY', label: '公寓' },
  { value: 'ST', label: '食堂' },
  { value: 'SYS', label: '平台' },
  { value: 'PAY', label: '支付' }
]
const moduleLabel = (m) => ({ GC: '公物仓', CL: '用车', GY: '公寓', ST: '食堂', SYS: '平台', PAY: '支付' }[m] || m || '-')
const typeLabel = (t) => ({ LOGIN: '登录', QUERY: '查询', ADD: '新增', UPDATE: '编辑', DELETE: '删除', APPROVE: '审批', EXPORT: '导出' }[t] || t || '-')
const typeTag = (t) => ({ LOGIN: 'info', QUERY: 'primary', ADD: 'success', UPDATE: 'warning', DELETE: 'error', APPROVE: 'success', EXPORT: 'info' }[t] || 'info')
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 查询 */
const query = reactive({ username: '', module: '', page: 1, size: 20 })
const loading = ref(false)
const logs = ref([])
const total = ref(0)
const finished = ref(false)

const loadLogs = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({
      url: '/api/log/page',
      data: {
        username: query.username || undefined,
        module: query.module || undefined,
        page: query.page,
        size: query.size
      }
    })
    const rows = res.data || []
    logs.value = reset ? rows : [...logs.value, ...rows]
    total.value = res.total || 0
    if (rows.length < query.size) finished.value = true
    else query.page += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { loadLogs(true) }
const toggleModule = (v) => {
  query.module = query.module === v ? '' : v
  loadLogs(true)
}

onReachBottom(() => { loadLogs() })

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = (row) => {
  detail.value = row
  detailVisible.value = true
}

onMounted(() => { loadLogs(true) })
</script>

<style>
.sys-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.tags { display: flex; gap: 10rpx; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>