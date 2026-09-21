<template>
  <view class="gc-page">
    <!-- 处置申请表单 -->
    <view class="card">
      <view class="card-title">🗑️ 资产处置申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="处置资产" prop="assetIds" required label-width="190rpx">
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
        <u-form-item label="处置方式" prop="disposeMethod" required label-width="190rpx">
          <u-input v-model="form.disposeMethodText" placeholder="请选择处置方式" :border="false" disabled @click="showMethod = true" />
        </u-form-item>
        <u-form-item label="评估机构" label-width="190rpx">
          <u-input v-model="form.appraisalOrg" placeholder="选填" :border="false" />
        </u-form-item>
        <u-form-item label="评估价值(元)" label-width="190rpx">
          <u-input v-model="form.appraisalValue" type="digit" placeholder="选填" :border="false" />
        </u-form-item>
        <u-form-item label="处置事由" prop="applyReason" required label-width="190rpx">
          <u-input v-model="form.applyReason" type="textarea" placeholder="请填写处置事由" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
      <u-button plain @click="resetForm">重置</u-button>
    </view>

    <!-- 处置申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 处置申请列表</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.orderNo }}</text>
          <u-tag :text="statusText(row.orderStatus)" :type="statusType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">处置方式</text><text class="value">{{ disposeMethodText(row) }}</text></view>
        <view class="item-line"><text class="label">资产数量</text><text class="value">{{ row.assetCount }}</text></view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.applyReason }}</text></view>
        <view class="item-line"><text class="label">申请时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions" v-if="row.orderStatus === 'PENDING' && canAudit">
          <u-button size="mini" type="warning" plain @click.stop="openAudit(row)">审核</u-button>
        </view>
        <view class="item-actions" v-else-if="row.orderStatus === 'APPROVED'">
          <u-button size="mini" type="success" plain @click.stop="openIncome(row)">录入收益</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无处置记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 资产选择弹窗 -->
    <u-popup :show="assetDialogVisible" mode="bottom" round :closeOnClickOverlay="true" @close="assetDialogVisible = false" height="70%">
      <view class="popup-panel">
        <view class="popup-title">选择处置资产</view>
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

    <!-- 处置方式选择 -->
    <u-picker v-model:show="showMethod" :columns="methodColumns" @confirm="onMethodConfirm"></u-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">处置审批</view>
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

    <!-- 录入收益弹窗 -->
    <u-popup :show="incomeVisible" mode="bottom" round :closeOnClickOverlay="true" @close="incomeVisible = false">
      <view class="popup-panel">
        <view class="popup-title">录入处置收益</view>
        <view class="popup-no">{{ incomeForm.orderNo }}</view>
        <u-form :model="incomeForm" ref="incomeFormRef" :rules="incomeRules">
          <u-form-item label="处置收入(元)" prop="incomeAmount" required label-width="190rpx">
            <u-input v-model="incomeForm.incomeAmount" type="digit" placeholder="0.00" :border="false" />
          </u-form-item>
          <u-form-item label="处置费用(元)" prop="expenseAmount" required label-width="190rpx">
            <u-input v-model="incomeForm.expenseAmount" type="digit" placeholder="评估费、拍卖佣金等" :border="false" />
          </u-form-item>
          <u-form-item label="备注" label-width="190rpx">
            <u-input v-model="incomeForm.remark" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="incomeVisible = false">取消</u-button>
          <u-button type="primary" :loading="recording" @click="submitIncome">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">处置单详情</view>
        <view class="detail-row"><text class="d-label">处置单号</text><text class="d-value">{{ detail.orderNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.orderStatus)" :type="statusType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">申请单位</text><text class="d-value">{{ detail.applicantUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">处置方式</text><text class="d-value">{{ detail.disposeMethodLabel || detail.disposeMethod || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">资产数量</text><text class="d-value">{{ detail.assetCount }}</text></view>
        <view class="detail-row"><text class="d-label">资产总值</text><text class="d-value">{{ detail.totalValue ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">评估机构</text><text class="d-value">{{ detail.appraisalOrg || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">评估价值</text><text class="d-value">{{ detail.appraisalValue ?? '-' }} 元</text></view>
        <view class="detail-row"><text class="d-label">处置事由</text><text class="d-value">{{ detail.applyReason || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>

        <view class="sub-title">处置资产明细</view>
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

        <view class="sub-title">收益记录</view>
        <view v-if="detail.incomeAmount !== null && detail.incomeAmount !== undefined">
          <view class="detail-row"><text class="d-label">处置收入</text><text class="d-value">{{ detail.incomeAmount }} 元</text></view>
          <view class="detail-row"><text class="d-label">处置费用</text><text class="d-value">{{ detail.expenseAmount ?? 0 }} 元</text></view>
          <view class="detail-row"><text class="d-label">净收益</text><text class="d-value">{{ detail.netProfit ?? '-' }} 元</text></view>
          <view class="detail-row"><text class="d-label">完成时间</text><text class="d-value">{{ formatTime(detail.execTime) }}</text></view>
          <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
        </view>
        <view v-else class="empty-mini">尚未录入收益</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 处置单状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' },
  COMPLETED: { text: '已完成', type: 'info' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 处置方式 */
const disposeMethods = [
  { value: 'AUCTION', text: '拍卖' },
  { value: 'SCRAP', text: '报废' },
  { value: 'DONATE', text: '捐赠' }
]
const disposeMethodText = (row) => {
  if (row.disposeMethodLabel) return row.disposeMethodLabel
  const hit = disposeMethods.find((m) => m.value === row.disposeMethod)
  return hit ? hit.text : row.disposeMethod || '-'
}

/** 审核权限（与 Web 端 GC_WH 一致） */
const canAudit = hasRole(['ADMIN', 'BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR'])

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetIds: [],
  disposeMethod: '',
  disposeMethodText: '',
  appraisalOrg: '',
  appraisalValue: '',
  applyReason: ''
})
const selectedAssets = ref([])

const rules = {
  assetIds: {
    validator: (rule, value, callback) => {
      if (!value || !value.length) callback(new Error('请选择处置资产'))
      else callback()
    },
    trigger: ['change']
  },
  disposeMethod: { required: true, message: '请选择处置方式', trigger: ['change'] },
  applyReason: { required: true, message: '请填写处置事由', trigger: ['blur'] }
}

/** 处置方式选择 */
const showMethod = ref(false)
const methodColumns = computed(() => [disposeMethods.map((m) => ({ value: m.value, text: m.text }))])
const onMethodConfirm = (e) => {
  form.disposeMethod = e.value[0].value
  form.disposeMethodText = e.value[0].text
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
      url: '/api/gc/transfer/dispose/apply',
      method: 'POST',
      data: {
        assetIds: form.assetIds,
        disposeMethod: form.disposeMethod,
        applyReason: form.applyReason,
        appraisalOrg: form.appraisalOrg || undefined,
        appraisalValue: form.appraisalValue !== '' ? Number(form.appraisalValue) : undefined
      }
    })
    uni.showToast({ title: '处置申请提交成功', icon: 'success' })
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
  form.disposeMethod = ''
  form.disposeMethodText = ''
  form.appraisalOrg = ''
  form.appraisalValue = ''
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
    const res = await request({ url: '/api/gc/transfer/dispose/page', data: { page: page.value, size } })
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
      url: '/api/gc/transfer/dispose/audit',
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

/** 录入收益 */
const incomeVisible = ref(false)
const recording = ref(false)
const incomeFormRef = ref()
const incomeForm = reactive({ orderId: null, orderNo: '', incomeAmount: '', expenseAmount: '', remark: '' })

const incomeRules = {
  incomeAmount: { required: true, message: '请输入处置收入', trigger: ['blur'] },
  expenseAmount: { required: true, message: '请输入处置费用', trigger: ['blur'] }
}

const openIncome = (row) => {
  incomeForm.orderId = row.id
  incomeForm.orderNo = row.orderNo
  incomeForm.incomeAmount = ''
  incomeForm.expenseAmount = ''
  incomeForm.remark = ''
  incomeVisible.value = true
}

const submitIncome = async () => {
  const valid = await incomeFormRef.value.validate()
  if (!valid) return
  recording.value = true
  try {
    await request({
      url: '/api/gc/transfer/dispose/income',
      method: 'PUT',
      data: {
        orderId: incomeForm.orderId,
        incomeAmount: Number(incomeForm.incomeAmount),
        expenseAmount: Number(incomeForm.expenseAmount),
        remark: incomeForm.remark || undefined
      }
    })
    uni.showToast({ title: '收益录入成功', icon: 'success' })
    incomeVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    recording.value = false
  }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gc/transfer/dispose/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
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