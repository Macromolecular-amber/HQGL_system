<template>
  <view class="borrow-page">
    <!-- 借用申请表单 -->
    <view class="card">
      <view class="card-title">🧺 借用申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="借用资产" prop="assetIds" required>
          <view class="asset-row" @click="openAssetDialog">
            <text class="asset-picker-text">{{ selectedAssets.length ? `已选 ${selectedAssets.length} 项` : '选择要借用的资产（仅限在仓资产）' }}</text>
          </view>
          <view class="asset-list" v-if="selectedAssets.length">
            <view class="asset-item" v-for="(a, idx) in selectedAssets" :key="a.id">
              <text class="asset-info">{{ a.assetCode }} · {{ a.assetName }}</text>
              <text class="asset-remove" @click="removeAsset(idx)">✕</text>
            </view>
          </view>
        </u-form-item>
        <u-form-item label="开始时间" prop="borrowStart" required>
          <u-input v-model="form.borrowStart" placeholder="请选择开始时间" :border="false" disabled @click="showStart = true" />
        </u-form-item>
        <u-form-item label="结束时间" prop="borrowEnd" required>
          <u-input v-model="form.borrowEnd" placeholder="请选择结束时间" :border="false" disabled @click="showEnd = true" />
        </u-form-item>
        <u-form-item label="借用事由" prop="borrowReason" required>
          <u-input v-model="form.borrowReason" type="textarea" placeholder="请填写借用事由" :border="false" />
        </u-form-item>
        <u-form-item label="备注">
          <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
      <u-button plain @click="resetForm">重置</u-button>
    </view>

    <!-- 借用申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 借用记录</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.orderNo }}</text>
          <u-tag :text="statusText(row.orderStatus)" :type="statusType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">资产</text><text class="value">{{ row.assetCount }} 项</text></view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.borrowReason }}</text></view>
        <view class="item-line"><text class="label">期限</text><text class="value">{{ formatPeriod(row.borrowStart, row.borrowEnd) }}</text></view>
        <view class="item-actions" v-if="row.orderStatus === 'PENDING' && canAudit">
          <u-button size="mini" type="warning" plain @click.stop="openAudit(row)">审核</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无借用记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 资产选择弹窗 -->
    <u-popup :show="assetDialogVisible" mode="bottom" round :closeOnClickOverlay="true" @close="assetDialogVisible = false" height="70%">
      <view class="popup-panel">
        <view class="popup-title">选择资产</view>
        <!-- u-checkbox-group 标准用法：v-model 绑定选中值数组，checkbox 用 :name 声明值 -->
        <u-checkbox-group v-model="selectedAssetIds">
          <view class="asset-option" v-for="a in assetOptions" :key="a.id">
            <u-checkbox :name="a.id" shape="circle" activeColor="#409EFF" />
            <view class="asset-opt-info">
              <text class="asset-opt-name">{{ a.assetName }}</text>
              <text class="asset-opt-code">{{ a.assetCode }} · {{ a.location || '-' }}</text>
            </view>
          </view>
        </u-checkbox-group>
        <view v-if="assetLoading" class="empty">加载中...</view>
        <view v-if="!assetLoading && assetOptions.length === 0" class="empty">暂无可借用资产</view>
        <view class="popup-actions">
          <u-button @click="assetDialogVisible = false">取消</u-button>
          <u-button type="primary" @click="confirmAssets">确定（已选 {{ selectedAssetIds.length }}）</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 开始/结束时间 -->
    <u-datetime-picker :show="showStart" v-model="form.borrowStartTs" mode="datetime" @confirm="onStartConfirm"></u-datetime-picker>
    <u-datetime-picker :show="showEnd" v-model="form.borrowEndTs" mode="datetime" @confirm="onEndConfirm"></u-datetime-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">借用审批</view>
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
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">借用单详情</view>
        <view class="detail-row"><text class="d-label">借用单号</text><text class="d-value">{{ detail.orderNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.orderStatus)" :type="statusType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">申请人</text><text class="d-value">{{ detail.applicantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请单位</text><text class="d-value">{{ detail.applicantUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">借用期限</text><text class="d-value">{{ formatPeriod(detail.borrowStart, detail.borrowEnd) }}</text></view>
        <view class="detail-row"><text class="d-label">借用事由</text><text class="d-value">{{ detail.borrowReason || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>

        <view class="sub-title">借用资产明细</view>
        <view class="vehicle-item" v-for="d in (detail.detailList || [])" :key="d.id">
          {{ d.assetCode }} · {{ d.assetName }} · {{ d.specModel || '-' }} · ×{{ d.borrowQuantity }}
        </view>
        <view v-if="!(detail.detailList || []).length" class="empty">无明细</view>

        <view class="sub-title" v-if="detail.auditUserName || detail.auditTime">审批记录</view>
        <view v-if="detail.auditUserName || detail.auditTime">
          <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
          <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
          <view class="detail-row"><text class="d-label">审批结果</text><text class="d-value">{{ detail.orderStatus === 'APPROVED' ? '通过' : detail.orderStatus === 'REJECTED' ? '驳回' : '-' }}</text></view>
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
import { hasRole, getUserInfo } from '@/utils/auth'

/** 状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' },
  DRAFT: { text: '草稿', type: 'info' },
  BORROWING: { text: '借用中', type: 'primary' },
  DONE: { text: '已归还', type: 'info' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatPeriod = (start, end) => `${formatTime(start)} ~ ${formatTime(end)}`

/** 审核权限：BIZ_ADMIN/WAREHOUSE/DIRECTOR/DEPT_MANAGER */
const canAudit = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER'])

/** 当前用户（借用申请人） */
const userInfo = getUserInfo()

/** 时间戳 → yyyy-MM-ddTHH:mm:ss */
const formatDateTime = (ts) => {
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetIds: [],
  borrowStart: '',
  borrowStartTs: null,
  borrowEnd: '',
  borrowEndTs: null,
  borrowReason: '',
  remark: ''
})
const selectedAssets = ref([])

const rules = {
  assetIds: { required: true, message: '请选择借用资产', trigger: ['change'] },
  borrowStart: { required: true, message: '请选择开始时间', trigger: ['change'] },
  borrowEnd: { required: true, message: '请选择结束时间', trigger: ['change'] },
  borrowReason: { required: true, message: '请填写借用事由', trigger: ['blur'] }
}

const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  if (form.borrowStartTs !== null && form.borrowEndTs !== null && form.borrowStartTs >= form.borrowEndTs) {
    uni.showToast({ title: '开始时间必须早于结束时间', icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await request({
      url: '/api/gc/borrow/apply',
      method: 'POST',
      data: {
        assetIds: form.assetIds,
        borrowStart: form.borrowStart,
        borrowEnd: form.borrowEnd,
        borrowReason: form.borrowReason,
        remark: form.remark || undefined,
        applicantId: userInfo.id,
        applicantUnitId: userInfo.unitId
      }
    })
    uni.showToast({ title: '借用申请提交成功', icon: 'success' })
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
  form.borrowStart = ''
  form.borrowStartTs = null
  form.borrowEnd = ''
  form.borrowEndTs = null
  form.borrowReason = ''
  form.remark = ''
  selectedAssets.value = []
  formRef.value && formRef.value.resetFields()
}

const removeAsset = (idx) => {
  const removed = selectedAssets.value.splice(idx, 1)[0]
  form.assetIds = selectedAssets.value.map((a) => a.id)
  const i = selectedAssetIds.value.indexOf(removed.id)
  if (i > -1) selectedAssetIds.value.splice(i, 1)
}

/** 开始时间 */
const showStart = ref(false)
const onStartConfirm = (e) => {
  form.borrowStartTs = e.value
  form.borrowStart = formatDateTime(e.value)
  showStart.value = false
}

/** 结束时间 */
const showEnd = ref(false)
const onEndConfirm = (e) => {
  form.borrowEndTs = e.value
  form.borrowEnd = formatDateTime(e.value)
  showEnd.value = false
}

/** 资产选择弹窗 */
const assetDialogVisible = ref(false)
const assetLoading = ref(false)
const assetOptions = ref([])
const selectedAssetIds = ref([])
const assetPage = ref(1)
const assetFinished = ref(false)

const loadAssets = async () => {
  if (assetFinished.value) return
  assetLoading.value = true
  try {
    // 在仓资产：status=IN_STOCK
    const res = await request({ url: '/api/gc/asset/list', data: { status: 'IN_STOCK', page: assetPage.value, size: 20 } })
    const rows = res.data || []
    assetOptions.value = [...assetOptions.value, ...rows]
    if (rows.length < 20) assetFinished.value = true
    else assetPage.value += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    assetLoading.value = false
  }
}

const openAssetDialog = () => {
  // 每次打开重置并载入已选
  assetPage.value = 1
  assetFinished.value = false
  assetOptions.value = []
  selectedAssetIds.value = form.assetIds.slice()
  assetDialogVisible.value = true
  loadAssets()
}

const confirmAssets = () => {
  // 用已选 ID 从已加载列表回填完整信息（可能跨页未加载完，但回传 ID 足够）
  const loadedMap = {}
  assetOptions.value.forEach((a) => { loadedMap[a.id] = a })
  selectedAssets.value = selectedAssetIds.value
    .map((id) => loadedMap[id])
    .filter(Boolean)
  form.assetIds = selectedAssets.value.map((a) => a.id)
  assetDialogVisible.value = false
}

/** 列表（分页上拉加载） */
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
    const res = await request({ url: '/api/gc/borrow/page', data: { page: page.value, size } })
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
      url: '/api/gc/borrow/audit',
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
    detail.value = await request({ url: `/api/gc/borrow/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style>
.borrow-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.asset-row { background: #f5f7fa; border-radius: 10rpx; padding: 20rpx; width: 100%; }
.asset-picker-text { color: #409eff; font-size: 26rpx; }
.asset-list { margin-top: 16rpx; }
.asset-item { display: flex; justify-content: space-between; align-items: center; background: #f0f7ff; border-radius: 8rpx; padding: 14rpx 20rpx; margin-bottom: 10rpx; }
.asset-info { font-size: 26rpx; color: #333; flex: 1; }
.asset-remove { color: #f56c6c; font-size: 30rpx; padding: 0 8rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.asset-option { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.asset-opt-info { flex: 1; display: flex; flex-direction: column; margin-left: 16rpx; }
.asset-opt-name { font-size: 28rpx; color: #333; }
.asset-opt-code { font-size: 24rpx; color: #999; margin-top: 4rpx; }
.asset-opt-check { margin-left: 16rpx; }
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