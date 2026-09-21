<template>
  <view class="cl-page">
    <view class="card">
      <view class="card-title">📊 单车台账（{{ yearMonth }}）</view>
      <u-datetime-picker :show="showMonth" v-model="monthTs" mode="date" @confirm="onMonthConfirm"></u-datetime-picker>
      <u-button size="mini" type="primary" plain @click="showMonth = true">选择年月</u-button>
      <view class="list-item" v-for="row in list" :key="row.vehicleId">
        <view class="item-head">
          <text class="item-no">{{ row.plateNumber }}</text>
          <u-tag :text="'总 ' + money(row.totalCost)" type="warning" size="mini" />
        </view>
        <view class="summary-grid">
          <view class="grid-cell"><text class="g-label">燃油</text><text class="g-value">{{ money(row.totalFuelCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">维修</text><text class="g-value">{{ money(row.totalRepairCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">保险</text><text class="g-value">{{ money(row.totalInsuranceCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">过路</text><text class="g-value">{{ money(row.totalTollCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">ETC</text><text class="g-value">{{ money(row.totalEtcCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">停车</text><text class="g-value">{{ money(row.totalParkingCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">其他</text><text class="g-value">{{ money(row.totalOtherCost) }}</text></view>
          <view class="grid-cell"><text class="g-label">油耗 L/百km</text><text class="g-value">{{ row.avgFuelConsumption ?? '-' }}</text></view>
        </view>
      </view>
      <view v-if="!loading && !list.length" class="empty">该月暂无已审批费用</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { request } from '@/utils/request'

const currentMonth = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
const yearMonth = ref(currentMonth())
const monthTs = ref(Date.now())
const showMonth = ref(false)
const loading = ref(false)
const list = ref([])
const money = (v) => (v == null ? '0.00' : Number(v).toFixed(2))

const onMonthConfirm = (e) => {
  monthTs.value = e.value
  const d = new Date(e.value)
  yearMonth.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  showMonth.value = false
  loadList()
}

const loadList = async () => {
  loading.value = true
  try {
    list.value = (await request({ url: '/api/cl/cost/summary/all', data: { yearMonth: yearMonth.value } })) || []
  } catch (e) { /* 错误已统一提示 */ } finally { loading.value = false }
}

onMounted(() => { loadList() })
</script>

<style>
.cl-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12rpx; }
.grid-cell { display: flex; flex-direction: column; background: #f5f7fa; border-radius: 8rpx; padding: 10rpx; }
.g-label { font-size: 22rpx; color: #999; }
.g-value { font-size: 26rpx; color: #333; font-weight: 600; margin-top: 4rpx; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>