<template>
  <view class="cl-page">
    <!-- 待派车申请（调度操作员） -->
    <view class="card" v-if="isDispatchOperator">
      <view class="card-title">⏳ 待派车申请</view>
      <view class="list-item" v-for="row in pendingList" :key="row.id">
        <view class="item-head">
          <text class="item-no">{{ row.applyNo }}</text>
          <u-tag :text="row.vehicleTypeLabel || '-'" size="mini" />
        </view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.purpose }}</text></view>
        <view class="item-line"><text class="label">目的地</text><text class="value">{{ row.destination || '-' }}</text></view>
        <view class="item-line"><text class="label">用车时间</text><text class="value">{{ formatPeriod(row.startTime, row.endTime) }}</text></view>
        <view class="item-line"><text class="label">申请人</text><text class="value">{{ row.applicantName }} · {{ row.passengerCount }}人</text></view>
        <view class="item-actions" v-if="canDispatch">
          <u-button size="mini" type="primary" plain @click.stop="openDispatch(row)">派车</u-button>
        </view>
      </view>
      <view v-if="!pendingLoading && !pendingList.length" class="empty">暂无待派车申请</view>
      <view v-if="pendingLoading" class="empty">加载中...</view>
    </view>

    <!-- 派单记录 -->
    <view class="card">
      <view class="card-title"><text>🚗 派单记录</text><text class="total">共 {{ total }} 单</text></view>
      <view class="list-item" v-for="row in dispatchList" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.dispatchNo }}</text>
          <u-tag :text="row.statusLabel || row.dispatchStatus" :type="statusType(row.dispatchStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">车牌</text><text class="value">{{ row.plateNumber }} · {{ row.driverName || '-' }}</text></view>
        <view class="item-line"><text class="label">计划时间</text><text class="value">{{ formatPeriod(row.scheduledStart, row.scheduledEnd) }}</text></view>
        <view class="item-line"><text class="label">实际结束</text><text class="value">{{ formatTime(row.actualEnd) }} · {{ row.actualMileage ?? '-' }}km</text></view>
        <view class="item-actions" v-if="canDispatch">
          <u-button v-if="row.dispatchStatus === 'WAITING' || row.dispatchStatus === 'ONGOING'" size="mini" type="warning" plain @click.stop="openReturn(row)">归还</u-button>
        </view>
      </view>
      <view v-if="!dispatchLoading && !dispatchList.length" class="empty">暂无派单</view>
      <view v-if="dispatchLoading" class="empty">加载中...</view>
    </view>

    <!-- 派车弹窗 -->
    <u-popup :show="dispatchVisible" mode="bottom" round :closeOnClickOverlay="true" @close="dispatchVisible = false" height="85%">
      <view class="popup-panel" v-if="currentApply">
        <view class="popup-title">车辆派车</view>
        <view class="popup-no">{{ currentApply.applyNo }} · {{ currentApply.purpose }}</view>
        <u-form :model="dispatchForm" ref="dispatchFormRef" :rules="dispatchRules">
          <u-form-item label="调度车辆" prop="vehicleId" required label-width="180rpx">
            <u-input v-model="dispatchForm.vehicleText" placeholder="请选择车辆" :border="false" disabled @click="showVehicle = true" />
          </u-form-item>
          <u-form-item label="驾驶员" prop="driverId" required label-width="180rpx">
            <u-input v-model="dispatchForm.driverText" placeholder="请选择驾驶员" :border="false" disabled @click="showDriver = true" />
          </u-form-item>
          <u-form-item label="计划出车" prop="scheduledStart" required label-width="180rpx">
            <u-input v-model="dispatchForm.scheduledStart" placeholder="请选择" :border="false" disabled @click="showStart = true" />
          </u-form-item>
          <u-form-item label="计划返回" prop="scheduledEnd" required label-width="180rpx">
            <u-input v-model="dispatchForm.scheduledEnd" placeholder="请选择" :border="false" disabled @click="showEnd = true" />
          </u-form-item>
          <u-form-item label="备注" label-width="180rpx">
            <u-input v-model="dispatchForm.remark" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="dispatchVisible = false">取消</u-button>
          <u-button type="primary" :loading="dispatching" @click="submitDispatch">确认派车</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 可用车辆 -->
    <u-picker v-model:show="showVehicle" :columns="vehicleColumns" @confirm="onVehicleConfirm"></u-picker>
    <!-- 驾驶员 -->
    <u-picker v-model:show="showDriver" :columns="driverColumns" @confirm="onDriverConfirm"></u-picker>
    <!-- 计划时间 -->
    <u-datetime-picker :show="showStart" v-model="startTs" mode="datetime" @confirm="onStartConfirm"></u-datetime-picker>
    <u-datetime-picker :show="showEnd" v-model="endTs" mode="datetime" @confirm="onEndConfirm"></u-datetime-picker>

    <!-- 归还弹窗 -->
    <u-popup :show="returnVisible" mode="bottom" round :closeOnClickOverlay="true" @close="returnVisible = false" height="55%">
      <view class="popup-panel">
        <view class="popup-title">车辆归还</view>
        <view class="popup-no">{{ returnForm.dispatchNo }} · {{ returnForm.plateNumber }}</view>
        <u-form :model="returnForm" ref="returnFormRef" :rules="returnRules">
          <u-form-item label="实际结束" prop="actualEnd" required label-width="180rpx">
            <u-input v-model="returnForm.actualEnd" placeholder="请选择" :border="false" disabled @click="showReturnEnd = true" />
          </u-form-item>
          <u-form-item label="实际里程(km)" prop="actualMileage" required label-width="180rpx">
            <u-input v-model="returnForm.actualMileage" type="digit" placeholder="请输入" :border="false" />
          </u-form-item>
          <u-form-item label="备注" label-width="180rpx">
            <u-input v-model="returnForm.remark" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="returnVisible = false">取消</u-button>
          <u-button type="primary" :loading="returning" @click="submitReturn">确认归还</u-button>
        </view>
      </view>
    </u-popup>
    <u-datetime-picker :show="showReturnEnd" v-model="returnEndTs" mode="datetime" @confirm="onReturnEndConfirm"></u-datetime-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">派单详情</view>
        <view class="detail-row"><text class="d-label">派单编号</text><text class="d-value">{{ detail.dispatchNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.statusLabel || detail.dispatchStatus" :type="statusType(detail.dispatchStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">申请人</text><text class="d-value">{{ detail.applicantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">事由</text><text class="d-value">{{ detail.purpose || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">目的地</text><text class="d-value">{{ detail.destination || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">车牌</text><text class="d-value">{{ detail.plateNumber || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">驾驶员</text><text class="d-value">{{ detail.driverName || '-' }}（{{ detail.driverPhone || '-' }}）</text></view>
        <view class="detail-row"><text class="d-label">计划时间</text><text class="d-value">{{ formatPeriod(detail.scheduledStart, detail.scheduledEnd) }}</text></view>
        <view class="detail-row"><text class="d-label">实际结束</text><text class="d-value">{{ formatTime(detail.actualEnd) }}</text></view>
        <view class="detail-row"><text class="d-label">实际里程</text><text class="d-value">{{ detail.actualMileage ?? '-' }} km</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { getUserInfo, getUserRoles, hasRole } from '@/utils/auth'

const userInfo = getUserInfo()
const roles = getUserRoles()
const isDispatchOperator = computed(() => roles.includes('ADMIN') || roles.includes('BIZ_ADMIN') || roles.includes('WAREHOUSE'))
const canDispatch = hasRole(['BIZ_ADMIN', 'WAREHOUSE'])

const statusMap = { WAITING: { type: 'warning' }, ONGOING: { type: 'primary' }, RETURNED: { type: 'success' }, CANCELLED: { type: 'info' } }
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatPeriod = (s, e) => `${formatTime(s)} ~ ${formatTime(e)}`
const formatDateTime = (ts) => {
  const d = new Date(ts); const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 待派车 */
const pendingLoading = ref(false)
const pendingList = ref([])
const loadPendingList = async () => {
  if (!isDispatchOperator.value) return
  pendingLoading.value = true
  try {
    const res = await request({ url: '/api/cl/apply/page', data: { applyStatus: 'APPROVED', page: 1, size: 50 } })
    pendingList.value = res.data || []
  } catch (e) {
    pendingList.value = []
  } finally {
    pendingLoading.value = false
  }
}

/** 派单记录 */
const dispatchLoading = ref(false)
const dispatchList = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const finished = ref(false)

const loadDispatchList = async (reset = false) => {
  if (reset) { page.value = 1; finished.value = false }
  if (finished.value) return
  dispatchLoading.value = true
  try {
    const params = { page: page.value, size }
    if (!isDispatchOperator.value && roles.includes('DRIVER')) params.driverId = userInfo.id
    const res = await request({ url: '/api/cl/dispatch/page', data: params })
    const rows = res.data || []
    dispatchList.value = reset ? rows : [...dispatchList.value, ...rows]
    total.value = res.total || 0
    if (rows.length < size) finished.value = true
    else page.value += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    dispatchLoading.value = false
  }
}

onReachBottom(() => { loadDispatchList() })

/** 派车 */
const dispatchVisible = ref(false)
const dispatching = ref(false)
const dispatchFormRef = ref()
const currentApply = ref(null)
const vehicles = ref([])
const drivers = ref([])
const dispatchForm = reactive({ applyId: null, vehicleId: null, vehicleText: '', driverId: null, driverText: '', scheduledStart: '', scheduledEnd: '', remark: '' })
const dispatchRules = {
  vehicleId: { validator: (r, v, c) => (v == null || v === '' ? c(new Error('请选择调度车辆')) : c()), trigger: ['change'] },
  driverId: { validator: (r, v, c) => (v == null || v === '' ? c(new Error('请选择驾驶员')) : c()), trigger: ['change'] },
  scheduledStart: { required: true, message: '请选择计划出车时间', trigger: ['change'] },
  scheduledEnd: { required: true, message: '请选择计划返回时间', trigger: ['change'] }
}

const openDispatch = (row) => {
  currentApply.value = row
  Object.assign(dispatchForm, { applyId: row.id, vehicleId: null, vehicleText: '', driverId: null, driverText: '', scheduledStart: '', scheduledEnd: '', remark: '' })
  dispatchVisible.value = true
  loadOptions()
}

const loadOptions = async () => {
  try { vehicles.value = (await request({ url: '/api/cl/vehicle/available' })) || [] } catch (e) { vehicles.value = [] }
  try { drivers.value = (await request({ url: '/api/sys/user/drivers' })) || [] } catch (e) { drivers.value = [] }
}

const showVehicle = ref(false)
const vehicleColumns = computed(() => [vehicles.value.map((v) => ({ value: v.id, text: `${v.plateNumber}（${v.vehicleTypeLabel || ''}）` }))])
const onVehicleConfirm = (e) => { dispatchForm.vehicleId = e.value[0].value; dispatchForm.vehicleText = e.value[0].text }
const showDriver = ref(false)
const driverColumns = computed(() => [drivers.value.map((d) => ({ value: d.id, text: `${d.realName}（${d.phone || ''}）` }))])
const onDriverConfirm = (e) => { dispatchForm.driverId = e.value[0].value; dispatchForm.driverText = e.value[0].text }

const showStart = ref(false)
const showEnd = ref(false)
const startTs = ref(Date.now())
const endTs = ref(Date.now())
const onStartConfirm = (e) => { startTs.value = e.value; dispatchForm.scheduledStart = formatDateTime(e.value); showStart.value = false }
const onEndConfirm = (e) => { endTs.value = e.value; dispatchForm.scheduledEnd = formatDateTime(e.value); showEnd.value = false }

const submitDispatch = async () => {
  const valid = await dispatchFormRef.value.validate()
  if (!valid) return
  if (dispatchForm.scheduledStart >= dispatchForm.scheduledEnd) {
    uni.showToast({ title: '出车时间必须早于返回时间', icon: 'none' })
    return
  }
  dispatching.value = true
  try {
    await request({
      url: '/api/cl/dispatch/dispatch', method: 'POST',
      data: { applyId: dispatchForm.applyId, vehicleId: dispatchForm.vehicleId, driverId: dispatchForm.driverId, scheduledStart: dispatchForm.scheduledStart, scheduledEnd: dispatchForm.scheduledEnd, remark: dispatchForm.remark || undefined }
    })
    uni.showToast({ title: '派车成功', icon: 'success' })
    dispatchVisible.value = false
    loadPendingList(); loadDispatchList(true)
  } catch (e) { /* 错误已统一提示 */ } finally { dispatching.value = false }
}

/** 归还 */
const returnVisible = ref(false)
const returning = ref(false)
const returnFormRef = ref()
const returnForm = reactive({ dispatchId: null, dispatchNo: '', plateNumber: '', actualEnd: '', actualMileage: '', remark: '' })
const returnRules = {
  actualEnd: { required: true, message: '请选择实际结束时间', trigger: ['change'] },
  actualMileage: { required: true, message: '请输入实际里程', trigger: ['blur'] }
}
const showReturnEnd = ref(false)
const returnEndTs = ref(Date.now())
const onReturnEndConfirm = (e) => { returnEndTs.value = e.value; returnForm.actualEnd = formatDateTime(e.value); showReturnEnd.value = false }
const openReturn = (row) => {
  Object.assign(returnForm, { dispatchId: row.id, dispatchNo: row.dispatchNo, plateNumber: row.plateNumber, actualEnd: '', actualMileage: '', remark: '' })
  returnVisible.value = true
}
const submitReturn = async () => {
  const valid = await returnFormRef.value.validate()
  if (!valid) return
  returning.value = true
  try {
    await request({ url: '/api/cl/dispatch/return', method: 'PUT', data: { dispatchId: returnForm.dispatchId, actualEnd: returnForm.actualEnd, actualMileage: Number(returnForm.actualMileage), remark: returnForm.remark || undefined } })
    uni.showToast({ title: '归还成功', icon: 'success' })
    returnVisible.value = false
    loadDispatchList(true)
  } catch (e) { /* 错误已统一提示 */ } finally { returning.value = false }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/cl/dispatch/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadPendingList()
  loadDispatchList(true)
})
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
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>