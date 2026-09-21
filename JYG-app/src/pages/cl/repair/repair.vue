<template>
  <view class="cl-page">
    <!-- 搜索区 -->
    <view class="card">
      <view class="filter-row">
        <u-tag v-for="s in filterStatus" :key="s.value" :text="s.label" :type="query.orderStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
      <u-button size="mini" type="primary" plain @click="openApply">新增维修</u-button>
    </view>

    <!-- 维修单列表 -->
    <view class="card">
      <view class="card-title"><text>🔧 维修保养</text><text class="total">共 {{ total }} 单</text></view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.repairNo }}</text>
          <u-tag :text="row.statusLabel || row.orderStatus" :type="statusType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">车辆</text><text class="value">{{ row.plateNumber }} · {{ row.repairTypeLabel || row.repairType }}</text></view>
        <view class="item-line"><text class="label">故障</text><text class="value">{{ row.faultDesc }}</text></view>
        <view class="item-line"><text class="label">维修厂</text><text class="value">{{ row.repairShopName || '-' }}</text></view>
        <view class="item-line"><text class="label">预估/实际</text><text class="value">{{ row.estimatedCost ?? '-' }} / {{ row.actualCost ?? '-' }}</text></view>
        <view class="item-actions">
          <u-button v-if="row.orderStatus === 'PENDING' && canOperate" size="mini" type="warning" plain @click.stop="openAudit(row)">审核</u-button>
          <u-button v-if="row.orderStatus === 'APPROVED' && canOperate" size="mini" type="primary" plain @click.stop="openStart(row)">开始维修</u-button>
          <u-button v-if="row.orderStatus === 'REPAIRING' && canOperate" size="mini" type="success" plain @click.stop="openAccept(row)">验收</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无维修单</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增维修弹窗 -->
    <u-popup :show="applyVisible" mode="bottom" round :closeOnClickOverlay="true" @close="applyVisible = false" height="80%">
      <view class="popup-panel">
        <view class="popup-title">新增维修</view>
        <u-form :model="applyForm" ref="applyFormRef" :rules="applyRules">
          <u-form-item label="车辆" prop="vehicleId" required label-width="180rpx">
            <u-input v-model="applyForm.vehicleText" placeholder="请选择车辆" :border="false" disabled @click="showVehicle = true" />
          </u-form-item>
          <u-form-item label="维修类型" prop="repairType" required label-width="180rpx">
            <u-input v-model="applyForm.repairTypeText" placeholder="请选择" :border="false" disabled @click="showRepairType = true" />
          </u-form-item>
          <u-form-item label="紧急程度" label-width="180rpx">
            <u-input v-model="applyForm.urgencyText" placeholder="选填" :border="false" disabled @click="showUrgency = true" />
          </u-form-item>
          <u-form-item label="维修里程" prop="repairMileage" required label-width="180rpx">
            <u-input v-model="applyForm.repairMileage" type="digit" placeholder="km" :border="false" />
          </u-form-item>
          <u-form-item label="故障描述" prop="faultDesc" required label-width="180rpx">
            <u-input v-model="applyForm.faultDesc" type="textarea" placeholder="请描述故障情况" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="applyVisible = false">取消</u-button>
          <u-button type="primary" :loading="applying" @click="submitApply">提交申请</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 车辆选择 -->
    <u-picker v-model:show="showVehicle" :columns="vehicleColumns" @confirm="onVehicleConfirm"></u-picker>
    <!-- 维修类型/紧急程度 -->
    <u-picker v-model:show="showRepairType" :columns="repairTypeColumns" @confirm="onRepairTypeConfirm"></u-picker>
    <u-picker v-model:show="showUrgency" :columns="urgencyColumns" @confirm="onUrgencyConfirm"></u-picker>

    <!-- 审批弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">维修审批</view>
        <view class="popup-no">{{ auditForm.repairNo }}</view>
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

    <!-- 开始维修弹窗 -->
    <u-popup :show="startVisible" mode="bottom" round :closeOnClickOverlay="true" @close="startVisible = false" height="50%">
      <view class="popup-panel">
        <view class="popup-title">开始维修</view>
        <view class="popup-no">{{ startForm.repairNo }}</view>
        <u-form :model="startForm" ref="startFormRef" :rules="startRules">
          <u-form-item label="维修厂名称" prop="repairShopName" required label-width="180rpx">
            <u-input v-model="startForm.repairShopName" placeholder="请输入维修厂名称" :border="false" />
          </u-form-item>
          <u-form-item label="预估费用" label-width="180rpx">
            <u-input v-model="startForm.estimatedCost" type="digit" placeholder="元" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="startVisible = false">取消</u-button>
          <u-button type="primary" :loading="starting" @click="submitStart">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 验收弹窗 -->
    <u-popup :show="acceptVisible" mode="bottom" round :closeOnClickOverlay="true" @close="acceptVisible = false" height="85%">
      <view class="popup-panel">
        <view class="popup-title">维修验收</view>
        <view class="popup-no">{{ acceptForm.repairNo }}</view>
        <u-radio-group v-model="acceptForm.acceptResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="FAIL" name="FAIL">不通过</u-radio>
        </u-radio-group>
        <u-form-item label="实际费用" v-if="acceptForm.acceptResult === 'PASS'">
          <u-input v-model="acceptForm.actualCost" type="digit" placeholder="元" :border="false" />
        </u-form-item>
        <u-form-item label="工时费" v-if="acceptForm.acceptResult === 'PASS'">
          <u-input v-model="acceptForm.laborCost" type="digit" placeholder="元" :border="false" />
        </u-form-item>
        <u-form-item label="配件明细" v-if="acceptForm.acceptResult === 'PASS'">
          <view class="part-row" v-for="(p, i) in acceptForm.partsDetail" :key="i">
            <u-input v-model="p.name" placeholder="配件名称" :border="true" class="part-name" />
            <u-input v-model="p.quantity" type="number" placeholder="数量" :border="true" class="part-num" />
            <u-input v-model="p.price" type="digit" placeholder="单价" :border="true" class="part-num" />
            <u-button v-if="acceptForm.partsDetail.length > 1" size="mini" type="error" plain @click="removePart(i)">删</u-button>
          </view>
          <u-button size="mini" type="primary" plain @click="addPart">+ 添加配件</u-button>
        </u-form-item>
        <u-input v-model="acceptForm.acceptRemark" type="textarea" placeholder="验收意见，选填" :border="true" />
        <view class="popup-actions">
          <u-button @click="acceptVisible = false">取消</u-button>
          <u-button type="primary" :loading="accepting" @click="submitAccept">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">维修单详情</view>
        <view class="detail-row"><text class="d-label">维修单号</text><text class="d-value">{{ detail.repairNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.statusLabel || detail.orderStatus" :type="statusType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">车牌号</text><text class="d-value">{{ detail.plateNumber || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">维修类型</text><text class="d-value">{{ detail.repairTypeLabel || detail.repairType }}</text></view>
        <view class="detail-row"><text class="d-label">紧急程度</text><text class="d-value">{{ urgencyText(detail.urgencyLevel) }}</text></view>
        <view class="detail-row"><text class="d-label">维修里程</text><text class="d-value">{{ detail.repairMileage ?? '-' }} km</text></view>
        <view class="detail-row"><text class="d-label">维修厂</text><text class="d-value">{{ detail.repairShopName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">预估费用</text><text class="d-value">{{ detail.estimatedCost ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">实际费用</text><text class="d-value">{{ detail.actualCost ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">工时费</text><text class="d-value">{{ detail.laborCost ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">开始维修</text><text class="d-value">{{ formatTime(detail.repairStart) }}</text></view>
        <view class="detail-row"><text class="d-label">完成时间</text><text class="d-value">{{ formatTime(detail.repairEnd) }}</text></view>
        <view class="detail-row"><text class="d-label">故障描述</text><text class="d-value">{{ detail.faultDesc || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
        <view class="detail-row"><text class="d-label">验收时间</text><text class="d-value">{{ formatTime(detail.acceptTime) }}</text></view>
        <view class="sub-title">配件明细</view>
        <view class="vehicle-item" v-for="d in (detail.partsDetail || [])" :key="d.id">
          {{ d.name }} · ×{{ d.quantity }} · {{ d.price }} 元 = {{ ((d.quantity || 0) * (d.price || 0)).toFixed(2) }} 元
        </view>
        <view v-if="!(detail.partsDetail || []).length" class="empty-mini">暂无配件明细</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

const repairTypes = [{ value: 'MAINTENANCE', text: '保养' }, { value: 'REPAIR', text: '维修' }]
const urgencies = [{ value: 'HIGH', text: '高' }, { value: 'MEDIUM', text: '中' }, { value: 'LOW', text: '低' }]
const urgencyText = (u) => (u ? (urgencies.find((x) => x.value === u) || {}).text || u : '-')

const statusOptions = [
  { value: 'PENDING', label: '待审批' }, { value: 'APPROVED', label: '已批准' },
  { value: 'REPAIRING', label: '维修中' }, { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' }
]
const filterStatus = [{ value: '', label: '全部' }, ...statusOptions]
const statusTypeMap = { PENDING: 'warning', APPROVED: 'success', REPAIRING: 'primary', COMPLETED: 'success', REJECTED: 'error', DONE: 'success' }
const statusType = (s) => statusTypeMap[s] || 'info'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 操作权限 */
const canOperate = hasRole(['BIZ_ADMIN', 'WAREHOUSE'])

/** 查询 */
const query = reactive({ orderStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/cl/repair/page', data: { orderStatus: query.orderStatus || undefined, page: query.page, size: query.size } })
    const rows = res.data || []
    list.value = reset ? rows : [...list.value, ...rows]
    total.value = res.total || 0
    if (rows.length < query.size) finished.value = true
    else query.page += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

const toggleStatus = (v) => {
  query.orderStatus = query.orderStatus === v ? '' : v
  query.page = 1; finished.value = false
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 车辆下拉 */
const vehicles = ref([])
const loadVehicles = async () => {
  try {
    const res = await request({ url: '/api/cl/vehicle/page', data: { page: 1, size: 100 } })
    vehicles.value = res.data || []
  } catch (e) {
    vehicles.value = []
  }
}

/** 新增维修 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({
  vehicleId: null, vehicleText: '', repairType: '', repairTypeText: '',
  urgencyLevel: '', urgencyText: '', repairMileage: '', faultDesc: ''
})

const applyRules = {
  vehicleId: { validator: (r, v, c) => (v == null || v === '' ? c(new Error('请选择车辆')) : c()), trigger: ['change'] },
  repairType: { required: true, message: '请选择维修类型', trigger: ['change'] },
  repairMileage: { required: true, message: '请输入维修里程', trigger: ['blur'] },
  faultDesc: { required: true, message: '请填写故障描述', trigger: ['blur'] }
}

const openApply = () => {
  Object.assign(applyForm, { vehicleId: null, vehicleText: '', repairType: '', repairTypeText: '', urgencyLevel: '', urgencyText: '', repairMileage: '', faultDesc: '' })
  applyVisible.value = true
}

const showVehicle = ref(false)
const vehicleColumns = computed(() => [vehicles.value.map((v) => ({ value: v.id, text: `${v.plateNumber}（${v.vehicleTypeLabel || ''}）` }))])
const onVehicleConfirm = (e) => {
  applyForm.vehicleId = e.value[0].value
  applyForm.vehicleText = e.value[0].text
}

const showRepairType = ref(false)
const repairTypeColumns = computed(() => [repairTypes.map((t) => ({ value: t.value, text: t.text }))])
const onRepairTypeConfirm = (e) => {
  applyForm.repairType = e.value[0].value
  applyForm.repairTypeText = e.value[0].text
}

const showUrgency = ref(false)
const urgencyColumns = computed(() => [urgencies.map((u) => ({ value: u.value, text: u.text }))])
const onUrgencyConfirm = (e) => {
  applyForm.urgencyLevel = e.value[0].value
  applyForm.urgencyText = e.value[0].text
}

const submitApply = async () => {
  const valid = await applyFormRef.value.validate()
  if (!valid) return
  applying.value = true
  try {
    await request({
      url: '/api/cl/repair/apply',
      method: 'POST',
      data: {
        vehicleId: applyForm.vehicleId,
        repairType: applyForm.repairType,
        faultDesc: applyForm.faultDesc,
        urgencyLevel: applyForm.urgencyLevel || undefined,
        repairMileage: Number(applyForm.repairMileage)
      }
    })
    uni.showToast({ title: '维修申请提交成功', icon: 'success' })
    applyVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    applying.value = false
  }
}

/** 审批 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ repairId: null, repairNo: '', auditResult: 'PASS', auditRemark: '' })
const openAudit = (row) => {
  Object.assign(auditForm, { repairId: row.id, repairNo: row.repairNo, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}
const submitAudit = async () => {
  auditing.value = true
  try {
    await request({ url: '/api/cl/repair/audit', method: 'PUT', data: { repairId: auditForm.repairId, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark } })
    uni.showToast({ title: '审批完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    auditing.value = false
  }
}

/** 开始维修 */
const startVisible = ref(false)
const starting = ref(false)
const startFormRef = ref()
const startForm = reactive({ repairId: null, repairNo: '', repairShopName: '', estimatedCost: '' })
const startRules = { repairShopName: { required: true, message: '请输入维修厂名称', trigger: ['blur'] } }

const openStart = (row) => {
  Object.assign(startForm, { repairId: row.id, repairNo: row.repairNo, repairShopName: '', estimatedCost: '' })
  startVisible.value = true
}
const submitStart = async () => {
  const valid = await startFormRef.value.validate()
  if (!valid) return
  starting.value = true
  try {
    await request({
      url: '/api/cl/repair/start', method: 'PUT',
      data: { repairId: startForm.repairId, repairShopName: startForm.repairShopName, estimatedCost: startForm.estimatedCost !== '' ? Number(startForm.estimatedCost) : undefined }
    })
    uni.showToast({ title: '已开始维修', icon: 'success' })
    startVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    starting.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({ repairId: null, repairNo: '', acceptResult: 'PASS', actualCost: '', laborCost: '', partsDetail: [{ name: '', quantity: '1', price: '' }], acceptRemark: '' })

const openAccept = (row) => {
  Object.assign(acceptForm, { repairId: row.id, repairNo: row.repairNo, acceptResult: 'PASS', actualCost: '', laborCost: '', partsDetail: [{ name: '', quantity: '1', price: '' }], acceptRemark: '' })
  acceptVisible.value = true
}

const addPart = () => acceptForm.partsDetail.push({ name: '', quantity: '1', price: '' })
const removePart = (i) => acceptForm.partsDetail.splice(i, 1)

const submitAccept = async () => {
  if (acceptForm.acceptResult === 'PASS' && acceptForm.actualCost === '') {
    uni.showToast({ title: '请输入实际费用', icon: 'none' })
    return
  }
  accepting.value = true
  try {
    const isPass = acceptForm.acceptResult === 'PASS'
    await request({
      url: '/api/cl/repair/accept', method: 'PUT',
      data: {
        repairId: acceptForm.repairId,
        acceptResult: acceptForm.acceptResult,
        actualCost: isPass ? Number(acceptForm.actualCost) : 0,
        laborCost: isPass && acceptForm.laborCost !== '' ? Number(acceptForm.laborCost) : undefined,
        partsDetail: isPass ? acceptForm.partsDetail.filter((p) => p.name).map((p) => ({ name: p.name, quantity: Number(p.quantity) || 1, price: Number(p.price) || 0 })) : undefined,
        acceptRemark: acceptForm.acceptRemark || undefined
      }
    })
    uni.showToast({ title: '验收完成', icon: 'success' })
    acceptVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    accepting.value = false
  }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/cl/repair/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
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
.empty-mini { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
.sub-title { font-size: 30rpx; font-weight: 600; margin: 24rpx 0 12rpx; }
.vehicle-item { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx 20rpx; font-size: 26rpx; color: #333; margin-bottom: 10rpx; }
.part-row { display: flex; align-items: center; gap: 10rpx; margin-bottom: 12rpx; }
.part-name { flex: 1; }
.part-num { width: 130rpx; }
</style>