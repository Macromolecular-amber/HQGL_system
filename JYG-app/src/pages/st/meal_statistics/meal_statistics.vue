<template>
  <view class="st-page">
    <view class="card">
      <view class="card-title">🥗 备餐统计</view>
      <u-input v-model="queryDate" placeholder="请选择日期" :border="false" disabled class="date-input" @click="showDate = true" />
      <view class="filter-row">
        <u-tag v-for="t in mealTypeOptions" :key="t.value" :text="t.label" :type="queryMealType === t.value ? 'primary' : 'info'" size="mini" plain @click="toggleMeal(t.value)" />
      </view>
      <u-button size="mini" type="primary" plain @click="handleQuery">查询</u-button>
      <u-button size="mini" type="info" plain @click="openDetail">查看预约明细</u-button>
    </view>

    <!-- 统计卡片 -->
    <view class="card">
      <view class="stat-row">
        <view class="stat-cell total"><text class="stat-num">{{ detailStat?.totalCount ?? '-' }}</text><text class="stat-label">总预约人数</text></view>
        <view class="stat-cell bf"><text class="stat-num">{{ breakfastCount ?? '-' }}</text><text class="stat-label">早餐</text></view>
        <view class="stat-cell lh"><text class="stat-num">{{ lunchCount ?? '-' }}</text><text class="stat-label">午餐</text></view>
        <view class="stat-cell dn"><text class="stat-num">{{ dinnerCount ?? '-' }}</text><text class="stat-label">晚餐</text></view>
      </view>
    </view>

    <!-- 按单位统计 -->
    <view class="card">
      <view class="card-title">按单位统计（{{ queryDate }}）</view>
      <view class="list-item" v-for="row in unitStats" :key="row.unitName">
        <view class="unit-head">
          <text class="unit-name">{{ row.unitName || '未知单位' }}</text>
          <text class="unit-count">{{ row.count }} 人</text>
        </view>
        <view class="bar-track"><view class="bar-fill" :style="{ width: percent(row.count) + '%' }" /></view>
      </view>
      <view v-if="!unitStats.length" class="empty">暂无统计数据</view>
    </view>

    <!-- 明细弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel">
        <view class="popup-title">{{ queryDate }} 预约明细</view>
        <view class="list-item" v-for="row in detailList" :key="row.id">
          <view class="item-head">
            <text class="item-no">{{ row.userName || '-' }}</text>
            <u-tag :text="row.mealTypeLabel || row.mealType" size="mini" />
          </view>
          <view class="item-line"><text class="label">单位</text><text class="value">{{ row.unitName || '-' }}</text></view>
          <view class="item-line"><text class="label">人数</text><text class="value">{{ row.mealCount }} · {{ formatTime(row.reservationTime) }}</text></view>
        </view>
        <view v-if="!detailList.length" class="empty">暂无预约明细</view>
      </view>
    </u-popup>

    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" @confirm="onDateConfirm"></u-datetime-picker>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { request } from '@/utils/request'

const mealTypeOptions = [
  { value: '', label: '全部' },
  { value: 'BREAKFAST', label: '早餐' },
  { value: 'LUNCH', label: '午餐' },
  { value: 'DINNER', label: '晚餐' }
]
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')
const today = () => { const d = new Date(); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` }

const queryDate = ref(today())
const queryMealType = ref('')
const showDate = ref(false)
const dateTs = ref(Date.now())
const loading = ref(false)

const allStat = ref(null)
const breakfastCount = ref(null)
const lunchCount = ref(null)
const dinnerCount = ref(null)

const detailStat = computed(() => {
  if (!queryMealType.value) return allStat.value
  return { totalCount: queryMealType.value === 'BREAKFAST' ? breakfastCount.value : queryMealType.value === 'LUNCH' ? lunchCount.value : dinnerCount.value }
})
const unitStats = computed(() => (queryMealType.value ? [] : allStat.value?.unitStatistics || []))

const percent = (count) => {
  const total = allStat.value?.totalCount || 0
  if (!total) return 0
  return Number(((count || 0) / total) * 100).toFixed(1)
}

const onDateConfirm = (e) => {
  dateTs.value = e.value
  const d = new Date(e.value)
  queryDate.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  showDate.value = false
}

const toggleMeal = (v) => {
  queryMealType.value = queryMealType.value === v ? '' : v
}

const handleQuery = async () => {
  loading.value = true
  try {
    allStat.value = await request({ url: '/api/st/meal/statistics', data: { mealDate: queryDate.value } })
    const [bf, lh, dn] = await Promise.all([
      request({ url: '/api/st/meal/statistics', data: { mealDate: queryDate.value, mealType: 'BREAKFAST' } }),
      request({ url: '/api/st/meal/statistics', data: { mealDate: queryDate.value, mealType: 'LUNCH' } }),
      request({ url: '/api/st/meal/statistics', data: { mealDate: queryDate.value, mealType: 'DINNER' } })
    ])
    breakfastCount.value = bf.totalCount
    lunchCount.value = lh.totalCount
    dinnerCount.value = dn.totalCount
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

/** 预约明细 */
const detailVisible = ref(false)
const detailList = ref([])
const openDetail = async () => {
  detailVisible.value = true
  try {
    detailList.value = (await request({ url: `/api/st/meal/date/${queryDate.value}` })) || []
  } catch (e) {
    detailList.value = []
  }
}

onMounted(() => { handleQuery() })
</script>

<style>
.st-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; }
.date-input { margin-bottom: 16rpx; }
.filter-row { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.stat-row { display: flex; justify-content: space-between; }
.stat-cell { flex: 1; text-align: center; background: #f5f7fa; border-radius: 12rpx; padding: 24rpx 0; margin: 0 6rpx; }
.stat-num { display: block; font-size: 36rpx; font-weight: 700; }
.stat-label { display: block; margin-top: 8rpx; font-size: 22rpx; color: #999; }
.stat-cell.total .stat-num { color: #409eff; }
.stat-cell.bf .stat-num { color: #e6a23c; }
.stat-cell.lh .stat-num { color: #409eff; }
.stat-cell.dn .stat-num { color: #67c23a; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.unit-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.unit-name { font-size: 28rpx; color: #333; }
.unit-count { font-size: 24rpx; color: #409eff; font-weight: 600; }
.bar-track { height: 22rpx; background: #f0f2f5; border-radius: 11rpx; overflow: hidden; }
.bar-fill { height: 100%; background: #409eff; border-radius: 11rpx; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.item-no { font-size: 28rpx; font-weight: 600; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
</style>