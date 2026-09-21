<template>
  <view class="gc-page">
    <!-- 调剂申请表单 -->
    <view class="card">
      <view class="card-title">🔄 资产调剂申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="调剂资产" prop="assetIds" required label-width="190rpx">
          <u-button size="mini" plain type="primary" @click="openAssetDialog">选择资产</u-button>
          <text class="picker-tip" v-if="!selectedAssets.length">从在仓资产中多选</text>
          <view class="asset-option" v-for="a in selectedAssets" :key="a.id">
            <view class="asset-opt-info">
              <text class="asset-opt-name">{{ a.assetName }}</text>
              <text class="asset-opt-code">{{ a.assetCode }} · {{ a.specModel || '-' }}</text>
            </view>
            <text class="asset-remove" @click="removeAsset(a.id)">移除</text>
          </view>
        </u-form-item>
        <u-form-item label="接收单位" prop="receiveUnitId" required label-width="190rpx">
          <u-input v-model="form.receiveUnitName" placeholder="请选择接收单位" :border="false" disabled @click="showUnit = true" />
        </u-form-item>
        <u-form-item label="调剂事由" prop="applyReason" required label-width="190rpx">
          <u-input v-model="form.applyReason" type="textarea" placeholder="请填写调剂事由" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
      <u-button plain @click="resetForm">重置</u-button>
    </view>

    <!-- 调剂申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 调剂申请列表</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.orderNo }}</text>
          <u-tag :text="statusText(row.orderStatus)" :type="statusType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">接收单位</text><text class="value">{{ row.receiveUnitName || '-' }}</text></view>
        <view class="item-line"><text class="label">资产数量</text><text class="value">{{ row.assetCount }}</text></view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.applyReason }}</text></view>
        <view class="item-line"><text class="label">申请时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions" v-if="row.orderStatus === 'PENDING' && canAudit">
          <u-button size="mini" type="warning" plain @click.stop="openAudit(row)">审核</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无调剂记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 资产选择弹窗 -->
    <u-popup :show="assetDialogVisible" mode="bottom" round :closeOnClickOverlay="true" @close="assetDialogVisible = false" height="70%">
      <view class="popup-panel">
        <view class="popup-title">选择调剂资产</view>
        <u-input v-model="assetSearch" placeholder="按资产名称/编号搜索" prefixIcon="search" :border="true" clearable />
        <scroll-view scroll-y class="asset-scroll">
          <view class="asset-option" v-for="a in filteredAssets" :key="a.id">
            <u-checkbox-group v-model="form.assetIds">
              <u-checkbox :name="a.id" shape="circle" activeColor="#409EFF" />
            </u-checkbox-group>
            <view class="asset-opt-info">
              <text class="asset-opt-name">{{ a.assetName }}</text>
              <text class="asset-opt-code">{{ a.assetCode }} · {{ a.categoryName || '' }} · {{ a.originalValue || '' }}</text>
            </view>
          </view>
          <view v-if="assetLoading" class="empty-mini">加载中...</view>
          <view v-else-if="!filteredAssets.length" class="empty-mini">无可选资产</view>
        </scroll-view>
        <view class="popup-actions">
          <u-button @click="assetDialogVisible = false">取消</u-button>
          <u-button type="primary" @click="confirmAssets">确定（已选 {{ form.assetIds.length }}）</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 接收单位选择（排除本单位） -->
    <u-picker v-model:show="showUnit" :columns="unitColumns" @confirm="onUnitConfirm"></u-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">调剂审批</view>
        <view class="popup-no">{{ auditForm.orderNo }}</view>
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
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">调剂单详情</view>
        <view class="detail-row"><text class="d-label">调剂单号</text><text class="d-value">{{ detail.orderNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.orderStatus)" :type="statusType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">申请单位</text><text class="d-value">{{ detail.applicantUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">接收单位</text><text class="d-value">{{ detail.receiveUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">资产数量</text><text class="d-value">{{ detail.assetCount }}</text></view>
        <view class="detail-row"><text class="d-label">资产总值</text><text class="d-value">{{ detail.totalValue ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">调剂事由</text><text class="d-value">{{ detail.applyReason || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>

        <view class="sub-title">调剂资产明细</view>
        <view class="vehicle-item" v-for="d in (detail.detailList || [])" :key="d.id">
          {{ d.assetCode }} · {{ d.assetName }} · {{ d.specModel || '-' }}
        </view>

        <view class="sub-title">审批记录</view>
        <view v-if="detail.auditUserName || detail.auditTime">
          <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
          <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
          <view class="detail-row"><text class="d-label">审批意见</text><text class="d-value">{{ detail.auditRemark || '-' }}</text></view>
        </view>
        <view v-else class="empty-mini">暂无审批记录</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole, getUserInfo } from '@/utils/auth'

/** 调剂单状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' },
  DONE: { text: '已完成', type: 'info' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 审核权限（与 Web 端 GC_WH 一致） */
const canAudit = hasRole(['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'])

/** 当前用户单位（接收单位需排除本单位） */
const userInfo = getUserInfo()
const currentUnitId = ref(userInfo.unitId || null)

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetIds: [],
  receiveUnitId: null,
  receiveUnitName: '',
  applyReason: ''
})
const selectedAssets = ref([])

const rules = {
  assetIds: {
    validator: (rule, value, callback) => {
      if (!value || !value.length) callback(new Error('请选择调剂资产'))
      else callback()
    },
    trigger: ['change']
  },
  // 自定义校验：兼容数字/字符串 ID
  receiveUnitId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择接收单位'))
      else callback()
    },
    trigger: ['change']
  },
  applyReason: { required: true, message: '请填写调剂事由', trigger: ['blur'] }
}

/** 接收单位下拉（排除本单位） */
const showUnit = ref(false)
const units = ref([])
const receiveUnits = computed(() => units.value.filter((u) => u.id !== currentUnitId.value))
const unitColumns = computed(() => [receiveUnits.value.map((u) => ({ value: u.id, text: u.unitName }))])

const loadUnits = async () => {
  try {
    units.value = (await request({ url: '/api/sys/unit/list' })) || []
  } catch (e) {
    units.value = []
  }
}

const onUnitConfirm = (e) => {
  form.receiveUnitId = e.value[0].value
  form.receiveUnitName = e.value[0].text
}

/** 资产选择弹窗 */
const assetDialogVisible = ref(false)
const assetLoading = ref(false)
const assetOptions = ref([])
const assetSearch = ref('')

const filteredAssets = computed(() => {
  const kw = assetSearch.value.trim().toLowerCase()
  if (!kw) return assetOptions.value
  return assetOptions.value.filter(
    (a) => (a.assetName || '').toLowerCase().includes(kw) || (a.assetCode || '').toLowerCase().includes(kw)
  )
})

const loadAssets = async () => {
  assetLoading.value = true
  try {
    assetOptions.value = (await request({ url: '/api/gc/transfer/available-assets' })) || []
  } catch (e) {
    assetOptions.value = []
  } finally {
    assetLoading.value = false
  }
}

const openAssetDialog = async () => {
  assetDialogVisible.value = true
  assetSearch.value = ''
  if (!assetOptions.value.length) await loadAssets()
}

const removeAsset = (id) => {
  const i = form.assetIds.indexOf(id)
  if (i > -1) form.assetIds.splice(i, 1)
  selectedAssets.value = selectedAssets.value.filter((a) => a.id !== id)
}

const confirmAssets = () => {
  selectedAssets.value = (assetOptions.value || []).filter((a) => form.assetIds.includes(a.id))
  // 同步一次计数（checkbox 组件状态偶发滞后）
  const checked = (assetOptions.value || []).filter((a) => form.assetIds.includes(a.id))
  form.assetIds = checked.map((a) => a.id)
  assetDialogVisible.value = false
}

/** 提交申请 */
const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await request({
      url: '/api/gc/transfer/apply',
      method: 'POST',
      data: {
        assetIds: form.assetIds,
        receiveUnitId: form.receiveUnitId,
        applyReason: form.applyReason
      }
    })
    uni.showToast({ title: '调剂申请提交成功', icon: 'success' })
    resetForm()
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.assetIds = []
  form.receiveUnitId = null
  form.receiveUnitName = ''
  form.applyReason = ''
  selectedAssets.value = []
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
    const res = await request({ url: '/api/gc/transfer/page', data: { page: page.value, size } })
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
const auditForm = reactive({ orderId: null, orderNo: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  auditForm.orderId = row.id
  auditForm.orderNo = row.orderNo
  auditForm.auditResult = 'PASS'
  auditForm.auditRemark = ''
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await request({
      url: '/api/gc/transfer/audit',
      method: 'PUT',
      data: { orderId: auditForm.orderId, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark }
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

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gc/transfer/${row.id}` })
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
.picker-tip { font-size: 24rpx; color: #999; margin-left: 16rpx; }
.asset-option { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.asset-opt-info { flex: 1; display: flex; flex-direction: column; margin-left: 16rpx; }
.asset-opt-name { font-size: 28rpx; color: #333; }
.asset-opt-code { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.asset-remove { color: #f56c6c; font-size: 26rpx; margin-left: 16rpx; }
.asset-scroll { max-height: 600rpx; margin: 20rpx 0; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.empty-mini { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 170rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
.sub-title { font-size: 30rpx; font-weight: 600; margin: 24rpx 0 12rpx; }
.vehicle-item { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx 20rpx; font-size: 26rpx; color: #333; margin-bottom: 10rpx; }
</style>