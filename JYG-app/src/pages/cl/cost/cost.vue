<template>
  <view class="cl-page">
    <!-- 搜索区 -->
    <view class="card">
      <view class="filter-row">
        <u-tag v-for="s in filterStatus" :key="s.value" :text="s.label" :type="query.approvalStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
      <u-button size="mini" type="primary" plain @click="openAdd">费用登记</u-button>
    </view>

    <!-- 费用明细列表 -->
    <view class="card">
      <view class="card-title"><text>💰 费用管理</text><text class="total">共 {{ total }} 条</text></view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.plateNumber || '-' }}</text>
          <u-tag :text="costTypeName(row.costType)" :type="row.costType === 'FUEL' ? 'warning' : 'primary'" size="mini" />
        </view>
        <view class="item-line"><text class="label">金额</text><text class="value">￥{{ row.costAmount }}</text></view>
        <view class="item-line"><text class="label">发生时间</text><text class="value">{{ formatTime(row.costTime) }}</text></view>
        <view class="item-line"><text class="label">状态</text><text class="value"><u-tag :text="row.approvalStatusLabel || row.approvalStatus" :type="statusType(row.approvalStatus)" size="mini" /></text></view>
        <view class="item-line" v-if="row.costDesc"><text class="label">描述</text><text class="value">{{ row.costDesc }}</text></view>
        <view class="item-actions">
          <u-button v-if="canOperate && row.approvalStatus === 'PENDING'" size="mini" type="warning" plain @click.stop="openAudit(row)">审批</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无费用记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增/编辑弹窗 -->
    <u-popup :show="formVisible" mode="bottom" round :closeOnClickOverlay="true" @close="formVisible = false" height="85%">
      <view class="popup-panel">
        <view class="popup-title">{{ form.id ? '编辑费用' : '费用登记' }}</view>
        <u-form :model="form" ref="formRef" :rules="rules">
          <u-form-item label="车辆" prop="vehicleId" required label-width="180rpx">
            <u-input v-model="form.vehicleText" placeholder="请选择车辆" :border="false" disabled @click="showVehicle = true" />
          </u-form-item>
          <u-form-item label="费用类型" prop="costType" required label-width="180rpx">
            <u-input v-model="form.costTypeText" placeholder="请选择" :border="false" disabled @click="showCostType = true" />
          </u-form-item>
          <u-form-item label="金额(元)" prop="costAmount" required label-width="180rpx">
            <u-input v-model="form.costAmount" type="digit" placeholder="请输入金额" :border="false" />
          </u-form-item>
          <u-form-item label="发生时间" prop="costTime" required label-width="180rpx">
            <u-input v-model="form.costTime" placeholder="请选择" :border="false" disabled @click="showTime = true" />
          </u-form-item>
          <u-form-item label="加油里程" v-if="form.costType === 'FUEL'" label-width="180rpx">
            <u-input v-model="form.currentMileage" type="digit" placeholder="km" :border="false" />
          </u-form-item>
          <u-form-item label="加油量(L)" v-if="form.costType === 'FUEL'" label-width="180rpx">
            <u-input v-model="form.fuelQuantity" type="digit" placeholder="L" :border="false" />
          </u-form-item>
          <u-form-item label="单据号" label-width="180rpx">
            <u-input v-model="form.bizOrderNo" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="描述" label-width="180rpx">
            <u-input v-model="form.costDesc" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="formVisible = false">取消</u-button>
          <u-button type="primary" :loading="saving" @click="submitForm">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 车辆/费用类型 -->
    <u-picker v-model:show="showVehicle" :columns="vehicleColumns" @confirm="onVehicleConfirm"></u-picker>
    <u-picker v-model:show="showCostType" :columns="costTypeColumns" @confirm="onCostTypeConfirm"></u-picker>
    <!-- 发生时间 -->
    <u-datetime-picker :show="showTime" v-model="timeTs" mode="datetime" @confirm="onTimeConfirm"></u-datetime-picker>

    <!-- 审批弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">费用审批</view>
        <view class="popup-no">{{ auditForm.plateNumber }} · ￥{{ auditForm.costAmount }}</view>
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
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="75%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">费用详情</view>
        <view class="detail-row"><text class="d-label">车辆</text><text class="d-value">{{ detail.plateNumber || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">类型</text><text class="d-value">{{ costTypeName(detail.costType) }}</text></view>
        <view class="detail-row"><text class="d-label">金额</text><text class="d-value">￥{{ detail.costAmount }}</text></view>
        <view class="detail-row"><text class="d-label">发生时间</text><text class="d-value">{{ formatTime(detail.costTime) }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.approvalStatusLabel || detail.approvalStatus" :type="statusType(detail.approvalStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">加油里程</text><text class="d-value">{{ detail.currentMileage ?? '-' }} km</text></view>
        <view class="detail-row"><text class="d-label">加油量</text><text class="d-value">{{ detail.fuelQuantity ?? '-' }} L</text></view>
        <view class="detail-row"><text class="d-label">单据号</text><text class="d-value">{{ detail.bizOrderNo || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">描述</text><text class="d-value">{{ detail.costDesc || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.approvalUserId || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.approvalTime) }}</text></view>
        <view class="detail-row"><text class="d-label">审批意见</text><text class="d-value">{{ detail.approvalRemark || '-' }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

const costTypes = [
  { value: 'FUEL', text: '加油' }, { value: 'REPAIR', text: '维修' }, { value: 'INSURANCE', text: '保险' },
  { value: 'TOLL', text: '过路费' }, { value: 'ETC', text: 'ETC' }, { value: 'PARKING', text: '停车' }, { value: 'OTHER', text: '其他' }
]
const costTypeName = (t) => (costTypes.find((x) => x.value === t) || {}).text || t || '-'
const statusOptions = [{ value: 'PENDING', label: '待审批' }, { value: 'APPROVED', label: '已通过' }, { value: 'REJECTED', label: '已驳回' }]
const filterStatus = [{ value: '', label: '全部' }, ...statusOptions]
const statusType = (s) => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'error' }[s] || 'info')
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatDateTime = (ts) => { const d = new Date(ts); const p = (n) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}` }

const canOperate = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'])

/** 车辆 */
const vehicles = ref([])
const loadVehicles = async () => {
  try {
    const res = await request({ url: '/api/cl/vehicle/page', data: { page: 1, size: 100 } })
    vehicles.value = res.data || []
  } catch (e) { vehicles.value = [] }
}

/** 查询 */
const query = reactive({ approvalStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/cl/cost/page', data: { approvalStatus: query.approvalStatus || undefined, page: query.page, size: query.size } })
    const rows = res.data || []
    list.value = reset ? rows : [...list.value, ...rows]
    total.value = res.total || 0
    if (rows.length < query.size) finished.value = true
    else query.page += 1
  } catch (e) { /* 错误已统一提示 */ } finally { loading.value = false }
}

const toggleStatus = (v) => { query.approvalStatus = query.approvalStatus === v ? '' : v; loadList(true) }
onReachBottom(() => { loadList() })

/** 表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({ id: null, vehicleId: null, vehicleText: '', costType: '', costTypeText: '', costAmount: '', costTime: '', currentMileage: '', fuelQuantity: '', bizOrderNo: '', costDesc: '' })
const rules = {
  vehicleId: { validator: (r, v, c) => (v == null || v === '' ? c(new Error('请选择车辆')) : c()), trigger: ['change'] },
  costType: { required: true, message: '请选择费用类型', trigger: ['change'] },
  costAmount: { required: true, message: '请输入费用金额', trigger: ['blur'] },
  costTime: { required: true, message: '请选择发生时间', trigger: ['change'] }
}

const openAdd = () => {
  Object.assign(form, { id: null, vehicleId: null, vehicleText: '', costType: '', costTypeText: '', costAmount: '', costTime: '', currentMileage: '', fuelQuantity: '', bizOrderNo: '', costDesc: '' })
  formVisible.value = true
}

const showVehicle = ref(false)
const vehicleColumns = computed(() => [vehicles.value.map((v) => ({ value: v.id, text: `${v.plateNumber}（${v.brandModel || v.vehicleTypeLabel || ''}）` }))])
const onVehicleConfirm = (e) => { form.vehicleId = e.value[0].value; form.vehicleText = e.value[0].text }
const showCostType = ref(false)
const costTypeColumns = computed(() => [costTypes.map((c) => ({ value: c.value, text: c.text }))])
const onCostTypeConfirm = (e) => { form.costType = e.value[0].value; form.costTypeText = e.value[0].text }

const showTime = ref(false)
const timeTs = ref(Date.now())
const onTimeConfirm = (e) => { timeTs.value = e.value; form.costTime = formatDateTime(e.value); showTime.value = false }

const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  try {
    const payload = {
      vehicleId: form.vehicleId,
      costType: form.costType,
      costAmount: Number(form.costAmount),
      costTime: form.costTime,
      costDesc: form.costDesc || undefined,
      bizOrderNo: form.bizOrderNo || undefined,
      currentMileage: form.costType === 'FUEL' && form.currentMileage !== '' ? Number(form.currentMileage) : undefined,
      fuelQuantity: form.costType === 'FUEL' && form.fuelQuantity !== '' ? Number(form.fuelQuantity) : undefined
    }
    if (form.id) await request({ url: `/api/cl/cost/update/${form.id}`, method: 'PUT', data: payload })
    else await request({ url: '/api/cl/cost/save', method: 'POST', data: payload })
    uni.showToast({ title: form.id ? '编辑成功' : '费用登记成功', icon: 'success' })
    formVisible.value = false
    loadList(true)
  } catch (e) { /* 错误已统一提示 */ } finally { saving.value = false }
}

/** 审批 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ costId: null, plateNumber: '', costAmount: null, auditResult: 'PASS', auditRemark: '' })
const openAudit = (row) => {
  Object.assign(auditForm, { costId: row.id, plateNumber: row.plateNumber, costAmount: row.costAmount, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}
const submitAudit = async () => {
  auditing.value = true
  try {
    await request({ url: '/api/cl/cost/audit', method: 'PUT', data: { costId: auditForm.costId, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark } })
    uni.showToast({ title: '审批完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) { /* 错误已统一提示 */ } finally { auditing.value = false }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = async (row) => {
  detailVisible.value = true
  try { detail.value = await request({ url: `/api/cl/cost/${row.id}` }) } catch (e) { detailVisible.value = false }
}

onMounted(() => {
  loadVehicles()
  loadList(true)
})
</script>

<style>
.cl-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>