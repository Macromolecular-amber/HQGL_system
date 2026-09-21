<template>
  <view class="st-page">
    <!-- Tab 切换 -->
    <view class="card">
      <view class="tab-row">
        <text v-for="t in tabs" :key="t.value" class="tab" :class="{ active: tabType === t.value }" @click="tabType = t.value">{{ t.label }}</text>
      </view>
    </view>

    <!-- Tab1 餐余管理 -->
    <template v-if="tabType === 'waste'">
      <view class="card">
        <view class="card-title">📝 录入餐余</view>
        <u-form :model="wasteForm" label-width="150rpx">
          <u-form-item label="日期">
            <u-input v-model="wasteForm.recordDate" placeholder="请选择日期" :border="false" disabled @click="showDate = true" />
          </u-form-item>
          <u-form-item label="餐次">
            <u-input v-model="wasteForm.mealTypeText" placeholder="请选择" :border="false" disabled @click="showMeal = true" />
          </u-form-item>
          <u-form-item label="重量(kg)">
            <u-input v-model="wasteForm.wasteWeight" type="digit" placeholder="请输入重量" :border="false" />
          </u-form-item>
          <u-form-item label="类型">
            <u-input v-model="wasteForm.wasteTypeText" placeholder="请选择" :border="false" disabled @click="showWasteType = true" />
          </u-form-item>
          <u-form-item label="处理方式">
            <u-input v-model="wasteForm.disposalText" placeholder="请选择" :border="false" disabled @click="showDisposal = true" />
          </u-form-item>
          <u-form-item label="处理人">
            <u-input v-model="wasteForm.disposalPerson" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <u-button type="primary" :loading="recording" @click="submitWaste">录入</u-button>
      </view>

      <view class="card">
        <view class="card-title">📊 餐余概览（近30天）</view>
        <view class="stat-row">
          <view class="stat-cell"><text class="stat-num">{{ wasteStat?.totalWeight ?? '-' }}</text><text class="stat-label">总餐余(kg)</text></view>
          <view class="stat-cell"><text class="stat-num">{{ wasteStat?.avgWeightPerDay ?? '-' }}</text><text class="stat-label">日均(kg)</text></view>
        </view>
        <view class="sub-title">按餐次分布</view>
        <view class="bar-line" v-for="d in byMealData" :key="d.name">
          <text class="bar-name">{{ d.name }}</text>
          <view class="bar-track"><view class="bar-fill warn" :style="{ width: barWidth(d.value, maxMealValue) + '%' }" /></view>
          <text class="bar-val">{{ d.value }}kg</text>
        </view>
        <view class="sub-title">按处理方式分布</view>
        <view class="bar-line" v-for="d in byDisposalData" :key="d.name">
          <text class="bar-name">{{ d.name }}</text>
          <view class="bar-track"><view class="bar-fill" :style="{ width: barWidth(d.value, maxDisposalValue) + '%' }" /></view>
          <text class="bar-val">{{ d.value }}kg</text>
        </view>
      </view>

      <view class="card">
        <view class="card-title">📋 餐余记录</view>
        <view class="list-item" v-for="row in wasteList" :key="row.id">
          <view class="item-head"><text class="item-no">{{ row.recordDate }}</text><u-tag :text="row.mealTypeLabel || row.mealType || '-'" size="mini" /></view>
          <view class="item-line"><text class="label">重量</text><text class="value">{{ row.wasteWeight }} kg · {{ row.wasteTypeLabel || '-' }}</text></view>
          <view class="item-line"><text class="label">处理</text><text class="value">{{ row.disposalMethodLabel || '-' }} · {{ row.disposalPerson || '-' }}</text></view>
          <view class="item-line"><text class="label">备注</text><text class="value">{{ row.remark || '-' }}</text></view>
        </view>
        <view v-if="!wasteLoading && !wasteList.length" class="empty">暂无餐余记录</view>
      </view>
    </template>

    <!-- Tab2 消费统计 -->
    <template v-else-if="tabType === 'consume'">
      <view class="card">
        <view class="card-title">💰 消费统计</view>
        <view class="period-row">
          <u-tag v-for="p in periods" :key="p.days" :text="p.label" :type="consumeDays === p.days ? 'primary' : 'info'" size="mini" plain @click="changeConsume(p.days)" />
        </view>
        <view class="stat-row">
          <view class="stat-cell"><text class="stat-num">￥{{ formatAmount(consumeStat?.totalAmount) }}</text><text class="stat-label">总消费</text></view>
          <view class="stat-cell"><text class="stat-num">{{ consumeStat?.totalCount ?? '-' }}</text><text class="stat-label">消费笔数</text></view>
          <view class="stat-cell"><text class="stat-num">￥{{ formatAmount(consumeStat?.avgAmountPerPerson) }}</text><text class="stat-label">人均消费</text></view>
        </view>
        <view class="sub-title">每日消费趋势</view>
        <view v-if="consumeTrend.length" class="bar-columns">
          <view class="col" v-for="(d, i) in consumeTrend" :key="i">
            <text class="col-val">{{ d.totalAmount > 0 ? d.totalAmount : '' }}</text>
            <view class="col-track"><view class="col-fill" :style="{ height: barHeight(d.totalAmount, maxConsumeAmount) + '%' }" /></view>
            <text class="col-label">{{ (d.date || '').slice(5) }}</text>
          </view>
        </view>
        <view v-else class="empty-mini">暂无消费数据</view>
      </view>
    </template>

    <!-- Tab3 采购统计 -->
    <template v-else>
      <view class="card">
        <view class="card-title">🛒 采购统计</view>
        <view class="period-row">
          <u-tag v-for="p in periods" :key="p.days" :text="p.label" :type="purchaseDays === p.days ? 'primary' : 'info'" size="mini" plain @click="changePurchase(p.days)" />
        </view>
        <view class="stat-row">
          <view class="stat-cell"><text class="stat-num">￥{{ formatAmount(purchaseStat?.totalPurchaseAmount) }}</text><text class="stat-label">采购总金额</text></view>
        </view>
        <view class="sub-title">月度趋势</view>
        <view v-if="monthlyTrend.length" class="bar-columns">
          <view class="col" v-for="(m, i) in monthlyTrend" :key="i">
            <text class="col-val">{{ m.totalAmount > 0 ? m.totalAmount : '' }}</text>
            <view class="col-track"><view class="col-fill green" :style="{ height: barHeight(m.totalAmount, maxMonthly) + '%' }" /></view>
            <text class="col-label">{{ m.month }}</text>
          </view>
        </view>
        <view v-else class="empty-mini">暂无采购数据</view>
        <view class="sub-title">采购金额前5物资</view>
        <view class="bar-line" v-for="d in topMaterialData" :key="d.name">
          <text class="bar-name">{{ d.name }}</text>
          <view class="bar-track"><view class="bar-fill orange" :style="{ width: barWidth(d.value, maxTop) + '%' }" /></view>
          <text class="bar-val">￥{{ d.value }}</text>
        </view>
      </view>
    </template>

    <!-- 日期/餐次/类型选择 -->
    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" @confirm="onDateConfirm"></u-datetime-picker>
    <u-picker v-model:show="showMeal" :columns="mealColumns" @confirm="onMealConfirm"></u-picker>
    <u-picker v-model:show="showWasteType" :columns="wasteTypeColumns" @confirm="onWasteTypeConfirm"></u-picker>
    <u-picker v-model:show="showDisposal" :columns="disposalColumns" @confirm="onDisposalConfirm"></u-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const tabs = [
  { value: 'waste', label: '餐余管理' },
  { value: 'consume', label: '消费统计' },
  { value: 'purchase', label: '采购统计' }
]
const tabType = ref('waste')

const mealTypeOptions = [
  { value: 'BREAKFAST', text: '早餐' },
  { value: 'LUNCH', text: '午餐' },
  { value: 'DINNER', text: '晚餐' }
]
const wasteTypeOptions = [
  { value: 'FOOD', text: '食物' },
  { value: 'PACKAGING', text: '包装' },
  { value: 'OTHER', text: '其他' }
]
const disposalOptions = [
  { value: 'COMPOST', text: '堆肥' },
  { value: 'FEED', text: '饲料' },
  { value: 'WASTE', text: '废弃物' }
]

const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))
const pad = (n) => String(n).padStart(2, '0')
const todayStr = () => { const d = new Date(); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` }
const daysAgo = (n) => { const d = new Date(); d.setDate(d.getDate() - n); return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` }

/* 图表辅助（CSS 条形/柱状） */
const barWidth = (v, max) => (max > 0 ? Math.max((Number(v) / max) * 100, 2) : 0)
const barHeight = (v, max) => (max > 0 ? Math.max((Number(v) / max) * 100, 3) : 0)

/** ===== 餐余 ===== */
const showDate = ref(false)
const dateTs = ref(Date.now())
const wasteForm = reactive({ recordDate: todayStr(), mealType: 'LUNCH', mealTypeText: '午餐', wasteWeight: '10', wasteType: 'FOOD', wasteTypeText: '食物', disposalMethod: 'COMPOST', disposalText: '堆肥', disposalPerson: '' })
const recording = ref(false)
const onDateConfirm = (e) => {
  dateTs.value = e.value
  const d = new Date(e.value)
  wasteForm.recordDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  showDate.value = false
}
const showMeal = ref(false)
const mealColumns = computed(() => [mealTypeOptions.map((m) => ({ value: m.value, text: m.text }))])
const onMealConfirm = (e) => { wasteForm.mealType = e.value[0].value; wasteForm.mealTypeText = e.value[0].text }
const showWasteType = ref(false)
const wasteTypeColumns = computed(() => [wasteTypeOptions.map((w) => ({ value: w.value, text: w.text }))])
const onWasteTypeConfirm = (e) => { wasteForm.wasteType = e.value[0].value; wasteForm.wasteTypeText = e.value[0].text }
const showDisposal = ref(false)
const disposalColumns = computed(() => [disposalOptions.map((d) => ({ value: d.value, text: d.text }))])
const onDisposalConfirm = (e) => { wasteForm.disposalMethod = e.value[0].value; wasteForm.disposalText = e.value[0].text }

const submitWaste = async () => {
  if (wasteForm.wasteWeight === '' || Number(wasteForm.wasteWeight) <= 0) {
    uni.showToast({ title: '请输入餐余重量', icon: 'none' }); return
  }
  recording.value = true
  try {
    await request({
      url: '/api/st/statistics/waste/record', method: 'POST',
      data: {
        recordDate: wasteForm.recordDate,
        mealType: wasteForm.mealType,
        wasteWeight: Number(wasteForm.wasteWeight),
        wasteType: wasteForm.wasteType,
        disposalMethod: wasteForm.disposalMethod,
        disposalPerson: wasteForm.disposalPerson || undefined
      }
    })
    uni.showToast({ title: '录入成功', icon: 'success' })
    loadWasteList(true); loadWasteStat()
  } catch (e) { /* 错误已统一提示 */ } finally { recording.value = false }
}

const wasteLoading = ref(false)
const wasteList = ref([])
const wastePage = ref(1)
const wasteTotal = ref(0)
const wasteFinished = ref(false)

const loadWasteList = async (reset = false) => {
  if (reset) { wastePage.value = 1; wasteFinished.value = false }
  if (wasteFinished.value) return
  wasteLoading.value = true
  try {
    const res = await request({ url: '/api/st/statistics/waste/page', data: { page: wastePage.value, size: 20 } })
    const rows = res.data || []
    wasteList.value = reset ? rows : [...wasteList.value, ...rows]
    wasteTotal.value = res.total || 0
    if (rows.length < 20) wasteFinished.value = true
    else wastePage.value += 1
  } catch (e) { /* 错误已统一提示 */ } finally { wasteLoading.value = false }
}
onReachBottom(() => { if (tabType.value === 'waste') loadWasteList() })

const wasteStat = ref(null)
const byMealData = computed(() => {
  const by = wasteStat.value?.byMealType || {}
  const labels = { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐' }
  const arr = Object.entries(by).map(([k, v]) => ({ name: labels[k] || k, value: Number(v) }))
  return arr.slice(0, 3)
})
const maxMealValue = computed(() => Math.max(1, ...byMealData.value.map((d) => d.value)))
const byDisposalData = computed(() => {
  const by = wasteStat.value?.byDisposalMethod || {}
  const labels = { COMPOST: '堆肥', FEED: '饲料', WASTE: '废弃物' }
  return Object.entries(by).map(([k, v]) => ({ name: labels[k] || k, value: Number(v) }))
})
const maxDisposalValue = computed(() => Math.max(1, ...byDisposalData.value.map((d) => d.value)))

const loadWasteStat = async () => {
  try {
    wasteStat.value = await request({ url: '/api/st/statistics/waste/statistics', data: { periodType: 'MONTH', startDate: daysAgo(30), endDate: todayStr() } })
  } catch (e) { /* 错误已统一提示 */ }
}

/** ===== 消费统计 ===== */
const periods = [{ days: 7, label: '近7天' }, { days: 30, label: '近30天' }, { days: 90, label: '近90天' }]
const consumeDays = ref(30)
const consumeStat = ref(null)
const consumeTrend = ref([])
const maxConsumeAmount = computed(() => Math.max(1, ...consumeTrend.value.map((d) => Number(d.totalAmount))))

const changeConsume = async (days) => {
  consumeDays.value = days
  await loadConsumeStat()
}
const loadConsumeStat = async () => {
  try {
    consumeStat.value = await request({ url: '/api/st/statistics/consume/statistics', data: { periodType: 'MONTH', startDate: daysAgo(consumeDays.value), endDate: todayStr() } })
    consumeTrend.value = consumeStat.value?.dailyTrend || []
    if (consumeTrend.value.length > 14) consumeTrend.value = consumeTrend.value.slice(-14)
  } catch (e) { /* 错误已统一提示 */ }
}

/** ===== 采购统计 ===== */
const purchaseDays = ref(90)
const purchaseStat = ref(null)
const monthlyTrend = ref([])
const topMaterialData = ref([])
const maxMonthly = computed(() => Math.max(1, ...monthlyTrend.value.map((m) => Number(m.totalAmount))))
const maxTop = computed(() => Math.max(1, ...topMaterialData.value.map((d) => Number(d.value))))

const changePurchase = async (days) => {
  purchaseDays.value = days
  await loadPurchaseStat()
}
const loadPurchaseStat = async () => {
  try {
    purchaseStat.value = await request({ url: '/api/st/statistics/purchase/statistics', data: { periodType: 'MONTH', startDate: daysAgo(purchaseDays.value), endDate: todayStr() } })
    monthlyTrend.value = purchaseStat.value?.monthlyTrend || []
    topMaterialData.value = (purchaseStat.value?.topMaterials || []).map((t) => ({ name: t.materialName, value: Number(t.totalAmount) }))
  } catch (e) { /* 错误已统一提示 */ }
}

onMounted(() => {
  loadWasteList(true)
  loadWasteStat()
  loadConsumeStat()
  loadPurchaseStat()
})
</script>

<style>
.st-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; }
.tab-row { display: flex; gap: 16rpx; }
.tab { flex: 1; text-align: center; padding: 14rpx 0; font-size: 28rpx; color: #666; border-bottom: 4rpx solid transparent; }
.tab.active { color: #409eff; border-bottom-color: #409eff; font-weight: 600; }
.stat-row { display: flex; justify-content: space-between; margin-bottom: 20rpx; }
.stat-cell { flex: 1; text-align: center; background: #f5f7fa; border-radius: 12rpx; padding: 24rpx 0; margin: 0 8rpx; }
.stat-num { display: block; font-size: 36rpx; font-weight: 700; color: #409eff; }
.stat-label { display: block; margin-top: 8rpx; font-size: 24rpx; color: #999; }
.sub-title { font-size: 28rpx; font-weight: 600; margin: 24rpx 0 16rpx; }
.bar-line { display: flex; align-items: center; margin-bottom: 16rpx; }
.bar-name { width: 140rpx; font-size: 26rpx; color: #333; }
.bar-track { flex: 1; height: 24rpx; background: #f0f2f5; border-radius: 12rpx; overflow: hidden; }
.bar-fill { height: 100%; background: #409eff; border-radius: 12rpx; }
.bar-fill.warn { background: #e6a23c; }
.bar-fill.orange { background: #e6a23c; }
.bar-fill.green { background: #67c23a; }
.bar-val { width: 120rpx; text-align: right; font-size: 24rpx; color: #333; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
.item-no { font-size: 28rpx; font-weight: 600; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.empty-mini { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
.period-row { display: flex; gap: 12rpx; margin-bottom: 20rpx; }
.bar-columns { display: flex; align-items: flex-end; height: 320rpx; padding-top: 30rpx; }
.col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; }
.col-val { font-size: 18rpx; color: #666; margin-bottom: 6rpx; }
.col-track { width: 26rpx; height: 220rpx; background: #f0f2f5; border-radius: 6rpx; overflow: hidden; display: flex; align-items: flex-end; }
.col-fill { width: 100%; background: #409eff; border-radius: 6rpx; }
.col-fill.green { background: #67c23a; }
.col-label { font-size: 20rpx; color: #999; margin-top: 6rpx; }
</style>