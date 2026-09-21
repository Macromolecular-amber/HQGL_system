<template>
  <view class="gc-page">
    <!-- 入仓申请表单 -->
    <view class="card">
      <view class="card-title">📦 资产入仓申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="资产名称" prop="assetName" required label-width="190rpx">
          <u-input v-model="form.assetName" placeholder="请输入资产名称" :border="false" />
        </u-form-item>
        <u-form-item label="分类" prop="categoryCode" required label-width="190rpx">
          <u-input v-model="form.categoryName" placeholder="请选择分类" :border="false" disabled @click="showCategory = true" />
        </u-form-item>
        <u-form-item label="规格型号" prop="specModel" label-width="190rpx">
          <u-input v-model="form.specModel" placeholder="请输入规格型号" :border="false" />
        </u-form-item>
        <u-form-item label="品牌" prop="brand" label-width="190rpx">
          <u-input v-model="form.brand" placeholder="请输入品牌" :border="false" />
        </u-form-item>
        <u-form-item label="原价值(元)" prop="originalValue" required label-width="190rpx">
          <u-input v-model="form.originalValue" type="digit" placeholder="请输入原价值" :border="false" />
        </u-form-item>
        <u-form-item label="购置日期" prop="purchaseDate" required label-width="190rpx">
          <u-input v-model="form.purchaseDate" placeholder="请选择购置日期" :border="false" disabled @click="showDate = true" />
        </u-form-item>
        <u-form-item label="使用年限(年)" prop="usefulLife" required label-width="190rpx">
          <u-input v-model="form.usefulLife" type="number" placeholder="请输入使用年限" :border="false" />
        </u-form-item>
        <u-form-item label="存放地点" prop="location" label-width="190rpx">
          <u-input v-model="form.location" placeholder="请输入存放地点" :border="false" />
        </u-form-item>
        <u-form-item label="权属单位" prop="ownerUnitId" required label-width="190rpx">
          <u-input v-model="form.ownerUnitName" placeholder="请选择权属单位" :border="false" disabled @click="showUnit = true" />
        </u-form-item>
        <u-form-item label="描述" prop="description" label-width="190rpx">
          <u-input v-model="form.description" type="textarea" placeholder="请输入资产描述" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
      <u-button plain @click="resetForm">重置</u-button>
    </view>

    <!-- 入仓申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 入仓申请列表</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.assetName }}</text>
          <u-tag :text="statusText(row.assetStatus)" :type="statusType(row.assetStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">编号</text><text class="value">{{ row.assetCode }}</text></view>
        <view class="item-line"><text class="label">分类</text><text class="value">{{ categoryName(row.categoryCode) }}</text></view>
        <view class="item-line"><text class="label">原价值</text><text class="value">{{ row.originalValue }} 元</text></view>
        <view class="item-line"><text class="label">申请单位</text><text class="value">{{ row.unitName || row.ownerUnitName || '-' }}</text></view>
        <view class="item-line"><text class="label">申请时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions" v-if="row.assetStatus === 'PENDING' && canAudit">
          <u-button size="mini" type="warning" plain @click.stop="openAudit(row)">审核</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无申请记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 分类选择 -->
    <u-picker v-model:show="showCategory" :columns="categoryColumns" @confirm="onCategoryConfirm"></u-picker>
    <!-- 权属单位选择 -->
    <u-picker v-model:show="showUnit" :columns="unitColumns" @confirm="onUnitConfirm"></u-picker>
    <!-- 购置日期 -->
    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" @confirm="onDateConfirm"></u-datetime-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">资产审核</view>
        <view class="popup-no">{{ auditForm.assetCode }} - {{ auditForm.assetName }}</view>
        <u-radio-group v-model="auditForm.auditResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="REJECT" name="REJECT">驳回</u-radio>
        </u-radio-group>
        <u-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审核意见" :border="true" />
        <view class="popup-actions">
          <u-button @click="auditVisible = false">取消</u-button>
          <u-button type="primary" :loading="auditing" @click="submitAudit">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">资产详情</view>
        <view class="detail-row"><text class="d-label">资产编号</text><text class="d-value">{{ detail.assetCode }}</text></view>
        <view class="detail-row"><text class="d-label">资产名称</text><text class="d-value">{{ detail.assetName }}</text></view>
        <view class="detail-row"><text class="d-label">分类</text><text class="d-value">{{ categoryName(detail.categoryCode) }}</text></view>
        <view class="detail-row"><text class="d-label">规格型号</text><text class="d-value">{{ detail.specModel || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">品牌</text><text class="d-value">{{ detail.brand || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">数量</text><text class="d-value">{{ detail.quantity }}</text></view>
        <view class="detail-row"><text class="d-label">原价值</text><text class="d-value">{{ detail.originalValue }} 元</text></view>
        <view class="detail-row"><text class="d-label">购置日期</text><text class="d-value">{{ detail.purchaseDate || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">使用年限</text><text class="d-value">{{ detail.usefulLife || '-' }} 年</text></view>
        <view class="detail-row"><text class="d-label">存放地点</text><text class="d-value">{{ detail.location || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请单位</text><text class="d-value">{{ detail.unitName || detail.ownerUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.assetStatus)" :type="statusType(detail.assetStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">描述</text><text class="d-value">{{ detail.description || '-' }}</text></view>
        <view class="detail-row" v-if="detail.auditUserName"><text class="d-label">审核人</text><text class="d-value">{{ detail.auditUserName }}</text></view>
        <view class="detail-row" v-if="detail.auditTime"><text class="d-label">审核时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
        <view class="detail-row" v-if="detail.auditRemark"><text class="d-label">审核意见</text><text class="d-value">{{ detail.auditRemark }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 资产分类（固定列表） */
const categories = [
  { code: 'JJ_01', name: '办公家具' },
  { code: 'IT_01', name: '办公设备' },
  { code: 'DQ_01', name: '电器设备' },
  { code: 'CL_01', name: '车辆' }
]

/** 状态映射 */
const statusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  IN_STOCK: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const categoryName = (code) => (categories.find((c) => c.code === code) || {}).name || code
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 审核权限 */
const canAudit = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER'])

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetName: '',
  categoryCode: '',
  categoryName: '',
  specModel: '',
  brand: '',
  originalValue: '',
  purchaseDate: '',
  usefulLife: '',
  location: '',
  ownerUnitId: null,
  ownerUnitName: '',
  description: '',
  photoUrls: []
})

const rules = {
  assetName: { required: true, message: '请输入资产名称', trigger: ['blur'] },
  categoryCode: { required: true, message: '请选择分类', trigger: ['change'] },
  originalValue: { required: true, message: '请输入原价值', trigger: ['blur'] },
  purchaseDate: { required: true, message: '请选择购置日期', trigger: ['change'] },
  usefulLife: { required: true, message: '请输入使用年限', trigger: ['blur'] },
  // 自定义校验：兼容数字/字符串 ID（async-validator 默认 string 类型会误拦截数字）
  ownerUnitId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择权属单位'))
      else callback()
    },
    trigger: ['change']
  }
}

/** 分类选择 */
const showCategory = ref(false)
const categoryColumns = computed(() => [categories.map((c) => ({ value: c.code, text: c.name }))])
const onCategoryConfirm = (e) => {
  const item = e.value[0]
  form.categoryCode = item.value
  form.categoryName = item.text
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

/** 权属单位下拉 */
const showUnit = ref(false)
const units = ref([])
const unitColumns = computed(() => [units.value.map((u) => ({ value: u.id, text: u.unitName }))])
const onUnitConfirm = (e) => {
  const item = e.value[0]
  form.ownerUnitId = item.value
  form.ownerUnitName = item.text
}

const loadUnits = async () => {
  try {
    units.value = (await request({ url: '/api/sys/unit/list' })) || []
  } catch (e) {
    units.value = []
  }
}

/** 提交申请 */
const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await request({
      url: '/api/gc/asset/apply',
      method: 'POST',
      data: {
        assetName: form.assetName,
        categoryCode: form.categoryCode,
        specModel: form.specModel || undefined,
        brand: form.brand || undefined,
        originalValue: Number(form.originalValue),
        purchaseDate: form.purchaseDate,
        usefulLife: Number(form.usefulLife),
        location: form.location || undefined,
        ownerUnitId: form.ownerUnitId,
        description: form.description || undefined,
        photoUrls: []
      }
    })
    uni.showToast({ title: '入仓申请提交成功', icon: 'success' })
    resetForm()
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.assetName = ''
  form.categoryCode = ''
  form.categoryName = ''
  form.specModel = ''
  form.brand = ''
  form.originalValue = ''
  form.purchaseDate = ''
  form.usefulLife = ''
  form.location = ''
  form.ownerUnitId = null
  form.ownerUnitName = ''
  form.description = ''
  formRef.value && formRef.value.resetFields()
}

/** 列表 */
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
    const res = await request({ url: '/api/gc/asset/page', data: { page: page.value, size } })
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
const auditForm = reactive({ id: null, assetCode: '', assetName: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  auditForm.id = row.id
  auditForm.assetCode = row.assetCode
  auditForm.assetName = row.assetName
  auditForm.auditResult = 'PASS'
  auditForm.auditRemark = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await request({
      url: '/api/gc/asset/audit',
      method: 'PUT',
      data: { id: auditForm.id, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark }
    })
    uni.showToast({ title: '审核完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    auditing.value = false
  }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gc/asset/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadUnits()
  loadList()
})
</script>

<style>
.gc-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
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
</style>