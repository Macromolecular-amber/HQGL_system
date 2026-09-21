<template>
  <view class="dash-page">
    <view class="card">
      <view class="card-title"><text>📊 领导驾驶舱</text><text class="total">{{ currentMonth }} · 实时统计</text></view>
      <u-button size="mini" type="primary" plain @click="loadData">刷新数据</u-button>
    </view>

    <!-- KPI 卡片 -->
    <view class="kpi-grid">
      <view class="kpi-card" v-for="kpi in kpiList" :key="kpi.key">
        <text class="kpi-value">{{ kpi.displayValue }}</text>
        <text class="kpi-label">{{ kpi.label }}</text>
        <view class="kpi-change" :class="'trend-' + kpi.trend">
          {{ kpi.trend === 'up' ? '▲' : kpi.trend === 'down' ? '▼' : '—' }} {{ Math.abs(kpi.change) }}%
        </view>
      </view>
    </view>

    <!-- 近7天趋势 -->
    <view class="card">
      <view class="card-title">📈 各模块趋势（近7天）</view>
      <view class="legend-row">
        <text class="legend"><i class="dot gc"></i>公物仓入仓</text>
        <text class="legend"><i class="dot cl"></i>用车次数</text>
        <text class="legend"><i class="dot st"></i>食堂预约</text>
      </view>
      <view v-if="trendDates.length">
        <view class="trend-series" v-for="s in seriesList" :key="s.name">
          <text class="trend-name">{{ s.name }}</text>
          <view class="trend-cols">
            <view class="trend-col" v-for="(v, i) in s.data" :key="i">
              <view class="trend-fill" :style="{ height: barH(v) + '%', background: s.color }" />
            </view>
          </view>
        </view>
        <view class="x-labels">
          <text v-for="(d, i) in trendDates" :key="i" class="x-label">{{ (d || '').slice(5) }}</text>
        </view>
      </view>
      <view v-else class="empty">暂无趋势数据</view>
    </view>

    <!-- 本月业务分布 -->
    <view class="card">
      <view class="card-title">🍩 本月业务分布（总量 {{ distTotal }}）</view>
      <view class="bar-line" v-for="d in distData" :key="d.name">
        <text class="bar-name">{{ d.name }}</text>
        <view class="bar-track"><view class="bar-fill" :style="{ width: d.percent + '%', background: d.color }" /></view>
        <text class="bar-val">{{ d.value }}（{{ d.percent }}%）</text>
      </view>
    </view>

    <!-- 本月核心数据明细 -->
    <view class="card">
      <view class="card-title">📋 本月核心数据明细</view>
      <view class="list-item" v-for="row in detailRows" :key="row.module">
        <view class="item-head">
          <text class="item-no">{{ row.module }}</text>
          <u-tag :text="row.status === 'normal' ? '正常' : '异常'" :type="row.status === 'normal' ? 'success' : 'error'" size="mini" />
        </view>
        <view class="item-line"><text class="label">本月新增</text><text class="value">{{ formatDetail(row.monthNew, row.unit) }}</text></view>
        <view class="item-line"><text class="label">本月完成</text><text class="value">{{ formatDetail(row.monthDone, row.unit) }}</text></view>
        <view class="item-line"><text class="label">环比上月</text><text class="value" :style="{ color: row.lastMonth >= 0 ? '#16a34a' : '#dc2626' }">{{ row.lastMonth >= 0 ? '+' : '' }}{{ row.lastMonth }}%</text></view>
        <view class="item-line"><text class="label">同比增长</text><text class="value" :style="{ color: row.yoy >= 0 ? '#16a34a' : '#dc2626' }">{{ row.yoy >= 0 ? '+' : '' }}{{ row.yoy }}%</text></view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/utils/request'

const loading = ref(false)
const currentMonth = ref('')
const kpis = ref({})
const trends = ref(null)
const distribution = ref({})
const details = ref({})

const kpiList = computed(() => {
  const defs = [
    { key: 'totalAssets', label: '总资产（件）', suffix: '' },
    { key: 'inStockAssets', label: '在仓资产（件）', suffix: '' },
    { key: 'monthCarUses', label: '本月用车（次）', suffix: '' },
    { key: 'occupancyRate', label: '公寓入住率', suffix: '%' },
    { key: 'monthMealReserves', label: '本月预约（人次）', suffix: '' },
    { key: 'pendingApprovals', label: '待审批（条）', suffix: '' }
  ]
  return defs.map((d) => {
    const raw = kpis.value[d.key] || { value: 0, change: 0, trend: 'flat' }
    const value = d.suffix === '%' ? Number(raw.value).toFixed(1) : Math.round(Number(raw.value)).toLocaleString()
    return { key: d.key, label: d.label, displayValue: value + d.suffix, change: Number(raw.change || 0), trend: raw.trend || 'flat' }
  })
})

const distTotal = computed(() => Object.values(distribution.value).reduce((s, v) => s + Number(v || 0), 0))

const distData = computed(() => {
  const d = distribution.value || {}
  const names = { gc: '公物仓借用', cl: '用车申请', gy: '公寓入住', st: '食堂预约' }
  const colors = ['#5470c6', '#91cc75', '#909399', '#fa8c16']
  const keys = ['gc', 'cl', 'gy', 'st']
  const items = keys.filter((k) => Number(d[k]) > 0).map((k, i) => ({ name: names[k], value: Number(d[k]), color: colors[i] }))
  const total = items.reduce((s, x) => s + x.value, 0) || 1
  return items.map((x) => ({ ...x, percent: Math.round((x.value / total) * 100) }))
})

const detailRows = computed(() => {
  const defs = [
    { key: 'gc', module: '公物仓', unit: '件' },
    { key: 'cl', module: '公务用车', unit: '次' },
    { key: 'gy', module: '公寓', unit: '人' },
    { key: 'st', module: '食堂', unit: '元' }
  ]
  return defs.map((d) => {
    const r = details.value[d.key] || {}
    return { module: d.module, unit: d.unit, monthNew: Number(r.monthNew || 0), monthDone: Number(r.monthDone || 0), lastMonth: Number(r.lastMonth || 0), yoy: Number(r.yoy || 0), status: r.status || 'normal' }
  })
})

/** 近7天趋势（CSS 柱状，跨端安全） */
const trendDates = computed(() => trends.value?.dates || [])
const maxTrend = computed(() => Math.max(1, ...(trends.value?.gc || []), ...(trends.value?.cl || []), ...(trends.value?.st || [])))
const seriesList = computed(() => {
  const t = trends.value || { dates: [], gc: [], cl: [], st: [] }
  return [
    { name: '公物仓入仓', data: t.gc || [], color: '#667eea' },
    { name: '用车次数', data: t.cl || [], color: '#36cfc9' },
    { name: '食堂预约', data: t.st || [], color: '#ff9c6e' }
  ]
})
const barH = (v) => {
  const max = maxTrend.value || 1
  return Math.max((Number(v) / max) * 100, 2)
}

const formatDetail = (v, unit) => {
  if (unit === '元') return `¥${Number(v).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
  return Number(v).toLocaleString() + (unit || '')
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await request({ url: '/api/dashboard/leadership' })
    kpis.value = data.kpis || {}
    trends.value = data.trends || null
    distribution.value = data.distribution || {}
    details.value = data.details || {}
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const d = new Date()
  currentMonth.value = `${d.getFullYear()}年${d.getMonth() + 1}月`
  loadData()
})
</script>

<style>
.dash-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.kpi-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 20rpx; }
.kpi-card { background: #fff; border-radius: 16rpx; padding: 24rpx; text-align: center; }
.kpi-value { display: block; font-size: 36rpx; font-weight: 700; color: #303133; }
.kpi-label { display: block; margin-top: 6rpx; font-size: 24rpx; color: #666; }
.kpi-change { display: inline-block; margin-top: 12rpx; font-size: 22rpx; font-weight: 600; border-radius: 16rpx; padding: 4rpx 16rpx; }
.trend-up { color: #16a34a; background: rgba(22,163,74,.1); }
.trend-down { color: #dc2626; background: rgba(220,38,38,.1); }
.trend-flat { color: #909399; background: rgba(144,147,153,.12); }
.legend-row { display: flex; gap: 24rpx; margin-bottom: 16rpx; }
.legend { font-size: 22rpx; color: #666; display: flex; align-items: center; gap: 6rpx; }
.dot { width: 16rpx; height: 16rpx; border-radius: 50%; display: inline-block; }
.dot.gc { background: #667eea; } .dot.cl { background: #36cfc9; } .dot.st { background: #ff9c6e; }
.trend-series { display: flex; align-items: center; margin-bottom: 14rpx; }
.trend-name { width: 150rpx; font-size: 22rpx; color: #666; flex-shrink: 0; }
.trend-cols { flex: 1; display: flex; align-items: flex-end; height: 90rpx; gap: 6rpx; }
.trend-col { flex: 1; height: 100%; display: flex; align-items: flex-end; background: #f5f7fa; border-radius: 4rpx; overflow: hidden; }
.trend-fill { width: 100%; border-radius: 4rpx; }
.x-labels { display: flex; justify-content: space-between; margin-left: 150rpx; }
.x-label { font-size: 18rpx; color: #999; }
.bar-line { display: flex; align-items: center; margin-bottom: 16rpx; }
.bar-name { width: 160rpx; font-size: 26rpx; color: #333; }
.bar-track { flex: 1; height: 24rpx; background: #f0f2f5; border-radius: 12rpx; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 12rpx; }
.bar-val { width: 150rpx; text-align: right; font-size: 22rpx; color: #333; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 160rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>