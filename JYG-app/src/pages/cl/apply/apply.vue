<template>
  <view class="apply-page">
    <!-- 用车申请表单 -->
    <view class="card">
      <view class="card-title">🚗 用车申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="用车事由" prop="purpose" required>
          <u-input v-model="form.purpose" placeholder="请填写用车事由" :border="false" />
        </u-form-item>
        <u-form-item label="目的地" prop="destination" required>
          <u-input v-model="form.destination" placeholder="请填写目的地" :border="false" />
        </u-form-item>
        <u-form-item label="开始时间" prop="startTime" required>
          <u-input v-model="form.startTime" placeholder="请选择开始时间" :border="false" disabled @click="showStart = true" />
        </u-form-item>
        <u-form-item label="结束时间" prop="endTime" required>
          <u-input v-model="form.endTime" placeholder="请选择结束时间" :border="false" disabled @click="showEnd = true" />
        </u-form-item>
        <u-form-item label="乘车人数" prop="passengerCount" required>
          <u-input v-model="form.passengerCount" type="number" placeholder="请输入乘车人数" :border="false" />
        </u-form-item>
        <u-form-item label="所需车型" prop="requiredVehicleType" required>
          <u-input v-model="form.requiredVehicleTypeLabel" placeholder="请选择所需车型" :border="false" disabled @click="showVehicleType = true" />
        </u-form-item>
        <u-form-item label="备注">
          <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
      <u-button plain @click="resetForm">重置</u-button>
    </view>

    <!-- 申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 申请记录</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.applyNo }}</text>
          <u-tag :text="row.statusLabel || row.applyStatus" :type="statusType(row.applyStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.purpose }}</text></view>
        <view class="item-line"><text class="label">目的地</text><text class="value">{{ row.destination }}</text></view>
        <view class="item-line"><text class="label">用车时间</text><text class="value">{{ formatPeriod(row.startTime, row.endTime) }}</text></view>
        <view class="item-line"><text class="label">车型</text><text class="value">{{ row.vehicleTypeLabel || row.requiredVehicleType || '-' }} · {{ row.passengerCount }}人</text></view>
        <view class="item-actions" v-if="row.applyStatus === 'PENDING'">
          <u-button size="mini" type="error" plain @click.stop="handleCancel(row)">取消</u-button>
          <u-button size="mini" type="warning" plain v-if="canAudit" @click.stop="openAudit(row)">审核</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无申请记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 车型选择（uview-plus 3.8 u-picker 单列） -->
    <u-picker v-model:show="showVehicleType" :columns="vehicleTypeColumns" @confirm="onVehicleTypeConfirm"></u-picker>

    <!-- 开始时间（:show 控制显示，v-model 绑定时间戳值） -->
    <u-datetime-picker :show="showStart" v-model="form.startTs" mode="datetime" @confirm="onStartConfirm"></u-datetime-picker>
    <!-- 结束时间 -->
    <u-datetime-picker :show="showEnd" v-model="form.endTs" mode="datetime" @confirm="onEndConfirm"></u-datetime-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">用车申请审批</view>
        <view class="popup-no">{{ auditForm.applyNo }}</view>
        <u-radio-group v-model="auditForm.auditResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="REJECT" name="REJECT">驳回</u-radio>
        </u-radio-group>
        <u-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审批意见" :border="true" />
        <view class="popup-actions">
          <u-button @click="auditVisible = false">取消</u-button>
          <u-button type="primary" :loading="auditing" @click="submitAudit">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">用车申请详情</view>
        <view class="detail-row"><text class="d-label">申请编号</text><text class="d-value">{{ detail.applyNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.statusLabel || detail.applyStatus" :type="statusType(detail.applyStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">申请人</text><text class="d-value">{{ detail.applicantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请单位</text><text class="d-value">{{ detail.applicantUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">用车事由</text><text class="d-value">{{ detail.purpose || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">目的地</text><text class="d-value">{{ detail.destination || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">所需车型</text><text class="d-value">{{ detail.vehicleTypeLabel || detail.requiredVehicleType || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">乘车人数</text><text class="d-value">{{ detail.passengerCount }}</text></view>
        <view class="detail-row"><text class="d-label">用车时间</text><text class="d-value">{{ formatPeriod(detail.startTime, detail.endTime) }}</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>

        <view class="sub-title">可调度车辆</view>
        <view class="vehicle-item" v-for="v in (detail.availableVehicles || [])" :key="v.id">
          {{ v.plateNumber }} · {{ v.brandModel }} · {{ v.vehicleTypeLabel }}
        </view>
        <view v-if="!(detail.availableVehicles || []).length" class="empty">暂无可用车辆</view>

        <view class="sub-title" v-if="detail.auditUserName || detail.auditTime">审批记录</view>
        <view v-if="detail.auditUserName || detail.auditTime">
          <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
          <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
          <view class="detail-row"><text class="d-label">审批结果</text><text class="d-value">{{ detail.applyStatus === 'APPROVED' ? '通过' : detail.applyStatus === 'REJECTED' ? '驳回' : '-' }}</text></view>
          <view class="detail-row"><text class="d-label">审批意见</text><text class="d-value">{{ detail.auditRemark || '-' }}</text></view>
        </view>
        <view v-else class="empty">暂无审批记录</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 车型选项（u-picker 单列 columns：keyName 默认 text） */
const vehicleTypeColumns = [
  [
    { value: 'SEDAN', text: '轿车' },
    { value: 'SUV', text: 'SUV' },
    { value: 'MPV', text: 'MPV' },
    { value: 'BUS', text: '客车' }
  ]
]

/** 状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' },
  CANCELLED: { text: '已取消', type: 'info' },
  DONE: { text: '已完成', type: 'info' }
}
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatPeriod = (start, end) => `${formatTime(start)} ~ ${formatTime(end)}`

/** 审批权限：BIZ_ADMIN / DIRECTOR（ADMIN 通吃） */
const canAudit = hasRole(['BIZ_ADMIN', 'DIRECTOR'])

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  purpose: '',
  destination: '',
  startTime: '',
  startTs: null,
  endTime: '',
  endTs: null,
  passengerCount: '',
  requiredVehicleType: '',
  requiredVehicleTypeLabel: '',
  remark: ''
})

const rules = {
  purpose: { required: true, message: '请填写用车事由', trigger: ['blur'] },
  destination: { required: true, message: '请填写目的地', trigger: ['blur'] },
  startTime: { required: true, message: '请选择开始时间', trigger: ['change'] },
  endTime: { required: true, message: '请选择结束时间', trigger: ['change'] },
  passengerCount: { required: true, message: '请输入乘车人数', trigger: ['blur'] },
  requiredVehicleType: { required: true, message: '请选择所需车型', trigger: ['change'] }
}

/** 时间戳 → yyyy-MM-ddTHH:mm:ss（与后端 LocalDateTime 格式一致） */
const formatDateTime = (ts) => {
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 车型选择器 */
const showVehicleType = ref(false)
const onVehicleTypeConfirm = (e) => {
  const item = e.value[0]
  form.requiredVehicleType = item.value
  form.requiredVehicleTypeLabel = item.text
}

/** 开始时间选择器 */
const showStart = ref(false)
const onStartConfirm = (e) => {
  form.startTs = e.value
  form.startTime = formatDateTime(e.value)
  showStart.value = false
}

/** 结束时间选择器 */
const showEnd = ref(false)
const onEndConfirm = (e) => {
  form.endTs = e.value
  form.endTime = formatDateTime(e.value)
  showEnd.value = false
}

const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  if (form.startTs !== null && form.endTs !== null && form.startTs >= form.endTs) {
    uni.showToast({ title: '开始时间必须早于结束时间', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/api/cl/apply/apply',
      method: 'POST',
      data: {
        purpose: form.purpose,
        destination: form.destination,
        startTime: form.startTime,
        endTime: form.endTime,
        passengerCount: Number(form.passengerCount),
        requiredVehicleType: form.requiredVehicleType,
        remark: form.remark || undefined
      }
    })
    uni.showToast({ title: '用车申请提交成功', icon: 'success' })
    resetForm()
    loadList(true)
  } catch (e) {
    // 错误已由 request 统一提示
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.purpose = ''
  form.destination = ''
  form.startTime = ''
  form.startTs = null
  form.endTime = ''
  form.endTs = null
  form.passengerCount = ''
  form.requiredVehicleType = ''
  form.requiredVehicleTypeLabel = ''
  form.remark = ''
  formRef.value && formRef.value.resetFields()
}

/** 申请列表（分页，上拉加载更多） */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) {
    page.value = 1
    finished.value = false
  }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/cl/apply/page', data: { page: page.value, size } })
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

/** 审核 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ applyId: null, applyNo: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  auditForm.applyId = row.id
  auditForm.applyNo = row.applyNo
  auditForm.auditResult = 'PASS'
  auditForm.auditRemark = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await request({
      url: '/api/cl/apply/audit',
      method: 'PUT',
      data: {
        applyId: auditForm.applyId,
        auditResult: auditForm.auditResult,
        auditRemark: auditForm.auditRemark
      }
    })
    uni.showToast({ title: '审批完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    auditing.value = false
  }
}

/** 取消 */
const handleCancel = (row) => {
  uni.showModal({
    title: '提示',
    content: `确认取消申请「${row.applyNo}」吗？`,
    confirmText: '确定',
    cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/cl/apply/cancel/${row.id}`, method: 'PUT' })
        uni.showToast({ title: '已取消', icon: 'success' })
        loadList(true)
      } catch (e) {
        // 错误已统一提示
      }
    }
  })
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/cl/apply/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style>
.apply-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
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
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 160rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
.sub-title { font-size: 30rpx; font-weight: 600; margin: 24rpx 0 12rpx; }
.vehicle-item { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx 20rpx; font-size: 26rpx; color: #333; margin-bottom: 10rpx; }
</style>