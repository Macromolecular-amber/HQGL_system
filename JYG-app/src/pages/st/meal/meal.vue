<template>
  <view class="meal-page">
    <!-- 预约表单 -->
    <view class="card">
      <view class="card-title">🍚 预约订餐</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="就餐日期" prop="mealDate" required label-width="170rpx">
          <u-input v-model="form.mealDate" placeholder="请选择日期" :border="false" disabled @click="showDate = true" />
        </u-form-item>
        <u-form-item label="餐次" prop="mealType" required label-width="170rpx">
          <u-radio-group v-model="form.mealType" placement="row">
            <u-radio label="BREAKFAST" name="BREAKFAST">早餐</u-radio>
            <u-radio label="LUNCH" name="LUNCH">午餐</u-radio>
            <u-radio label="DINNER" name="DINNER">晚餐</u-radio>
          </u-radio-group>
        </u-form-item>
        <u-form-item label="单位名称" prop="unitId" required label-width="170rpx">
          <u-input v-model="form.unitName" placeholder="请选择单位" :border="false" disabled @click="showUnit = true" />
        </u-form-item>
        <u-form-item label="就餐人数" prop="mealCount" required label-width="170rpx">
          <u-input v-model="form.mealCount" type="number" placeholder="请输入人数" :border="false" />
        </u-form-item>
        <u-form-item label="备注" label-width="170rpx">
          <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="reserving" @click="submitReserve">提交预约</u-button>
    </view>

    <!-- 我的预约记录 -->
    <view class="card">
      <view class="card-title">
        <text>📋 我的预约记录</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id">
        <view class="item-head">
          <text class="item-no">{{ row.mealDate }} · {{ row.mealTypeLabel || row.mealType }}</text>
          <u-tag :text="row.isCancelled ? '已取消' : '有效'" :type="row.isCancelled ? 'info' : 'success'" size="mini" />
        </view>
        <view class="item-line"><text class="label">单位</text><text class="value">{{ row.unitName || '-' }}</text></view>
        <view class="item-line"><text class="label">人数</text><text class="value">{{ row.mealCount }} 人</text></view>
        <view class="item-line"><text class="label">预约时间</text><text class="value">{{ formatTime(row.reservationTime) }}</text></view>
        <view class="item-line" v-if="row.remark"><text class="label">备注</text><text class="value">{{ row.remark }}</text></view>
        <view class="item-actions" v-if="canCancel(row)">
          <u-button size="mini" type="error" plain @click="handleCancel(row)">取消预约</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无预约记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 日期选择（禁过去日期：minDate=今天0点） -->
    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" :minDate="todayStart" @confirm="onDateConfirm"></u-datetime-picker>

    <!-- 单位选择 -->
    <u-picker v-model:show="showUnit" :columns="unitColumns" @confirm="onUnitConfirm"></u-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { getUserInfo } from '@/utils/auth'

/** 餐次开餐时间与标签色 */
const mealTimeMap = { BREAKFAST: 7, LUNCH: 12, DINNER: 18 }
const mealTagTypeMap = { BREAKFAST: 'warning', LUNCH: 'primary', DINNER: 'success' }

/** 当前登录用户单位（预填下拉默认值） */
const userInfo = getUserInfo()

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')

/** 今天 0 点时间戳（作为日期选择器最小可选项） */
const todayStart = (() => {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0).getTime()
})()

/** 预约表单 */
const formRef = ref()
const reserving = ref(false)
const form = reactive({
  mealDate: '',
  mealType: 'LUNCH',
  unitId: userInfo.unitId || null,
  unitName: userInfo.unitName || '',
  mealCount: 1,
  remark: ''
})

const rules = {
  mealDate: { required: true, message: '请选择就餐日期', trigger: ['change'] },
  mealType: { required: true, message: '请选择餐次', trigger: ['change'] },
  // 自定义校验：兼容数字/字符串 ID（async-validator 默认 string 类型会误拦截数字）
  unitId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择单位'))
      else callback()
    },
    trigger: ['change']
  },
  mealCount: { required: true, message: '请输入就餐人数', trigger: ['blur'] }
}

/** 日期选择 */
const showDate = ref(false)
const dateTs = ref(todayStart)
const onDateConfirm = (e) => {
  dateTs.value = e.value
  const d = new Date(e.value)
  form.mealDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  showDate.value = false
}

/** 单位下拉 */
const showUnit = ref(false)
const units = ref([])
const unitColumns = computed(() => [units.value.map((u) => ({ value: u.id, text: u.unitName }))])

const loadUnits = async () => {
  try {
    units.value = (await request({ url: '/api/sys/unit/list' })) || []
    // 预填：userInfo.unitName 可能为空，从列表找匹配
    if (!form.unitName && form.unitId) {
      const hit = units.value.find((u) => u.id === form.unitId)
      if (hit) form.unitName = hit.unitName
    }
    // 兜底：unitId 仍为空时默认选第一个单位，保证必填校验通过
    if (!form.unitId && units.value.length) {
      form.unitId = units.value[0].id
      form.unitName = units.value[0].unitName
    }
  } catch (e) {
    units.value = []
  }
}

const onUnitConfirm = (e) => {
  const item = e.value[0]
  form.unitId = item.value
  form.unitName = item.text
}

/** 提交预约 */
const submitReserve = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  reserving.value = true
  try {
    await request({
      url: '/api/st/meal/reserve',
      method: 'POST',
      data: {
        mealDate: form.mealDate,
        mealType: form.mealType,
        mealCount: Number(form.mealCount) || 1,
        unitId: form.unitId,
        remark: form.remark || undefined
      }
    })
    uni.showToast({ title: '预约成功', icon: 'success' })
    form.remark = ''
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    reserving.value = false
  }
}

/** 我的预约记录 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/st/meal/page', data: { page: page.value, size } })
    const rows = res.data || []
    list.value = reset ? rows : [...list.value, ...rows]
    total.value = res.total || 0
    if (rows.length < size) finished.value = true
    else page.value += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

onReachBottom(() => {
  loadList()
})

/** 是否可取消：未取消且当前时间在餐次开始前1小时之前 */
const canCancel = (row) => {
  if (row.isCancelled) return false
  const mealHour = mealTimeMap[String(row.mealType || '').toUpperCase()]
  if (mealHour == null) return false
  const [y, m, d] = String(row.mealDate).split('-').map(Number)
  const cutoff = new Date(y, m - 1, d, mealHour - 1, 0, 0)
  return new Date().getTime() < cutoff.getTime()
}

const handleCancel = (row) => {
  uni.showModal({
    title: '提示',
    content: `确定取消 ${row.mealDate} 的${row.mealTypeLabel}预约吗？`,
    confirmText: '确定',
    cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: '/api/st/meal/cancel', method: 'PUT', data: { reservationId: row.id } })
        uni.showToast({ title: '预约已取消', icon: 'success' })
        loadList(true)
      } catch (e) {
        // 错误已统一提示
      }
    }
  })
}

onMounted(() => {
  loadUnits()
  loadList()
})
</script>

<style>
.meal-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
</style>