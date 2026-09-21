<template>
  <view class="cl-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.plateNumber" placeholder="按车牌号搜索" :showAction="true" actionText="查询" @search="handleQuery" @custom="handleQuery"></u-search>
      <view class="filter-row">
        <u-tag v-for="s in filterTypes" :key="s.value" :text="s.label" :type="query.vehicleStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
    </view>

    <!-- 车辆列表 -->
    <view class="card">
      <view class="card-title">
        <text>🚗 车辆档案</text>
        <text class="total">共 {{ total }} 辆</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.plateNumber }}</text>
          <u-tag :text="row.vehicleStatusLabel || row.vehicleStatus" :type="statusType(row.vehicleStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">品牌型号</text><text class="value">{{ row.brandModel }}</text></view>
        <view class="item-line"><text class="label">类型</text><text class="value">{{ row.vehicleTypeLabel || row.vehicleType || '-' }} · {{ row.seatCount }}座</text></view>
        <view class="item-line"><text class="label">所属单位</text><text class="value">{{ row.unitName || '-' }}</text></view>
        <view class="item-actions" v-if="canManage">
          <u-button size="mini" type="primary" plain @click.stop="openEdit(row)">编辑</u-button>
          <u-button size="mini" type="error" plain @click.stop="handleDelete(row)">删除</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无车辆</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增/编辑弹窗 -->
    <u-popup :show="formVisible" mode="bottom" round :closeOnClickOverlay="true" @close="formVisible = false" height="88%">
      <view class="popup-panel">
        <view class="popup-title">{{ form.id ? '编辑车辆' : '新增车辆' }}</view>
        <u-form :model="form" ref="formRef" :rules="rules">
          <u-form-item label="车牌号" prop="plateNumber" required label-width="180rpx">
            <u-input v-model="form.plateNumber" placeholder="如 甘B·00001" :border="false" />
          </u-form-item>
          <u-form-item label="品牌型号" prop="brandModel" required label-width="180rpx">
            <u-input v-model="form.brandModel" placeholder="如 丰田考斯特" :border="false" />
          </u-form-item>
          <u-form-item label="车辆类型" prop="vehicleType" required label-width="180rpx">
            <u-input v-model="form.vehicleTypeText" placeholder="请选择" :border="false" disabled @click="showType = true" />
          </u-form-item>
          <u-form-item label="座位数" prop="seatCount" required label-width="180rpx">
            <u-input v-model="form.seatCount" type="number" placeholder="座位数" :border="false" />
          </u-form-item>
          <u-form-item label="发动机号" label-width="180rpx">
            <u-input v-model="form.engineNo" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="车架号" label-width="180rpx">
            <u-input v-model="form.frameNo" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="排量(L)" label-width="180rpx">
            <u-input v-model="form.displacement" type="digit" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="颜色" label-width="180rpx">
            <u-input v-model="form.color" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="购置日期" label-width="180rpx">
            <u-input v-model="form.purchaseDate" placeholder="选填" :border="false" disabled @click="showDate = true" />
          </u-form-item>
          <u-form-item label="购置价格(元)" label-width="180rpx">
            <u-input v-model="form.purchasePrice" type="digit" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="所属单位" prop="unitId" required label-width="180rpx">
            <u-input v-model="form.unitName" placeholder="请选择" :border="false" disabled @click="showUnit = true" />
          </u-form-item>
          <u-form-item label="备注" label-width="180rpx">
            <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="formVisible = false">取消</u-button>
          <u-button type="primary" :loading="saving" @click="submitForm">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 车辆类型 -->
    <u-picker v-model:show="showType" :columns="typeColumns" @confirm="onTypeConfirm"></u-picker>
    <!-- 所属单位 -->
    <u-picker v-model:show="showUnit" :columns="unitColumns" @confirm="onUnitConfirm"></u-picker>
    <!-- 购置日期 -->
    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" @confirm="onDateConfirm"></u-datetime-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">车辆详情</view>
        <view class="detail-row"><text class="d-label">车牌号</text><text class="d-value">{{ detail.plateNumber }}</text></view>
        <view class="detail-row"><text class="d-label">品牌型号</text><text class="d-value">{{ detail.brandModel }}</text></view>
        <view class="detail-row"><text class="d-label">车辆类型</text><text class="d-value">{{ detail.vehicleTypeLabel || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.vehicleStatusLabel || detail.vehicleStatus" :type="statusType(detail.vehicleStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">发动机号</text><text class="d-value">{{ detail.engineNo || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">车架号</text><text class="d-value">{{ detail.frameNo || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">座位数</text><text class="d-value">{{ detail.seatCount }}</text></view>
        <view class="detail-row"><text class="d-label">排量</text><text class="d-value">{{ detail.displacement ?? '-' }} L</text></view>
        <view class="detail-row"><text class="d-label">颜色</text><text class="d-value">{{ detail.color || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">购置日期</text><text class="d-value">{{ detail.purchaseDate || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">购置价格</text><text class="d-value">{{ detail.purchasePrice ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">所属单位</text><text class="d-value">{{ detail.unitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">当前里程</text><text class="d-value">{{ detail.currentMileage ?? '-' }} km</text></view>
        <view class="detail-row"><text class="d-label">保险公司</text><text class="d-value">{{ detail.insuranceCompany || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">保险止期</text><text class="d-value">{{ detail.insuranceEnd || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 车辆类型 */
const vehicleTypes = [
  { value: 'SEDAN', text: '轿车' },
  { value: 'SUV', text: 'SUV' },
  { value: 'MPV', text: 'MPV' },
  { value: 'BUS', text: '客车' }
]
const typeText = (v) => (vehicleTypes.find((t) => t.value === v) || {}).text || v

/** 状态标签色 */
const statusTypeMap = {
  AVAILABLE: 'success', ON_DUTY: 'primary', REPAIRING: 'error',
  MAINTAINING: 'warning', WAIT_SCRAP: 'info', SCRAPPED: 'info'
}
const statusType = (s) => statusTypeMap[s] || 'info'

/** 状态快捷筛选 */
const filterTypes = [
  { value: '', label: '全部' },
  { value: 'AVAILABLE', label: '可用' },
  { value: 'ON_DUTY', label: '出车中' },
  { value: 'REPAIRING', label: '维修中' }
]

/** 管理权限 */
const canManage = hasRole(['BIZ_ADMIN', 'WAREHOUSE'])

/** 查询 */
const query = reactive({ plateNumber: '', vehicleStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({
      url: '/api/cl/vehicle/page',
      data: {
        plateNumber: query.plateNumber || undefined,
        vehicleStatus: query.vehicleStatus || undefined,
        page: query.page, size: query.size
      }
    })
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

const handleQuery = () => { loadList(true) }
const toggleStatus = (v) => {
  query.vehicleStatus = query.vehicleStatus === v ? '' : v
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 新增/编辑表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null, plateNumber: '', brandModel: '', vehicleType: '', vehicleTypeText: '',
  engineNo: '', frameNo: '', seatCount: '', displacement: '', color: '',
  purchaseDate: '', purchasePrice: '', unitId: null, unitName: '', remark: ''
})

const rules = {
  plateNumber: { required: true, message: '请输入车牌号', trigger: ['blur'] },
  brandModel: { required: true, message: '请输入品牌型号', trigger: ['blur'] },
  vehicleType: { required: true, message: '请选择车辆类型', trigger: ['change'] },
  seatCount: { required: true, message: '请输入座位数', trigger: ['blur'] },
  unitId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择所属单位'))
      else callback()
    },
    trigger: ['change']
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, plateNumber: '', brandModel: '', vehicleType: '', vehicleTypeText: '',
    engineNo: '', frameNo: '', seatCount: '', displacement: '', color: '',
    purchaseDate: '', purchasePrice: '', unitId: null, unitName: '', remark: ''
  })
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id, plateNumber: row.plateNumber, brandModel: row.brandModel,
    vehicleType: row.vehicleType || '', vehicleTypeText: typeText(row.vehicleType),
    engineNo: row.engineNo || '', frameNo: row.frameNo || '',
    seatCount: row.seatCount != null ? String(row.seatCount) : '',
    displacement: row.displacement != null ? String(row.displacement) : '',
    color: row.color || '', purchaseDate: row.purchaseDate || '',
    purchasePrice: row.purchasePrice != null ? String(row.purchasePrice) : '',
    unitId: row.unitId || null, unitName: row.unitName || '', remark: row.remark || ''
  })
  formVisible.value = true
}

/** 类型选择 */
const showType = ref(false)
const typeColumns = computed(() => [vehicleTypes.map((t) => ({ value: t.value, text: t.text }))])
const onTypeConfirm = (e) => {
  form.vehicleType = e.value[0].value
  form.vehicleTypeText = e.value[0].text
}

/** 单位选择 */
const showUnit = ref(false)
const units = ref([])
const unitColumns = computed(() => [units.value.map((u) => ({ value: u.id, text: u.unitName }))])
const onUnitConfirm = (e) => {
  form.unitId = e.value[0].value
  form.unitName = e.value[0].text
}

const loadUnits = async () => {
  try {
    units.value = (await request({ url: '/api/sys/unit/list' })) || []
  } catch (e) {
    units.value = []
  }
}

/** 购置日期 */
const showDate = ref(false)
const dateTs = ref(Date.now())
const pad = (n) => String(n).padStart(2, '0')
const onDateConfirm = (e) => {
  dateTs.value = e.value
  const d = new Date(e.value)
  form.purchaseDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  showDate.value = false
}

const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  try {
    await request({
      url: '/api/cl/vehicle/save',
      method: 'POST',
      data: {
        id: form.id || undefined,
        plateNumber: form.plateNumber,
        brandModel: form.brandModel,
        vehicleType: form.vehicleType,
        engineNo: form.engineNo || undefined,
        frameNo: form.frameNo || undefined,
        seatCount: Number(form.seatCount),
        displacement: form.displacement !== '' ? Number(form.displacement) : undefined,
        color: form.color || undefined,
        purchaseDate: form.purchaseDate || undefined,
        purchasePrice: form.purchasePrice !== '' ? Number(form.purchasePrice) : undefined,
        unitId: form.unitId,
        remark: form.remark || undefined
      }
    })
    uni.showToast({ title: form.id ? '编辑成功' : '新增成功', icon: 'success' })
    formVisible.value = false
    handleQuery()
  } catch (e) {
    // 错误已统一提示
  } finally {
    saving.value = false
  }
}

/** 删除 */
const handleDelete = (row) => {
  uni.showModal({
    title: '提示',
    content: `确认删除车辆「${row.plateNumber}」吗？`,
    confirmText: '删除',
    cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/cl/vehicle/${row.id}`, method: 'DELETE' })
        uni.showToast({ title: '删除成功', icon: 'success' })
        handleQuery()
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
    detail.value = await request({ url: `/api/cl/vehicle/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadUnits()
  loadList(true)
})
</script>

<style>
.cl-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>