<template>
  <view class="gc-page">
    <!-- 归还申请表单 -->
    <view class="card">
      <view class="card-title">🔙 归还申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="借用单号" prop="borrowOrderId" required label-width="190rpx">
          <u-input v-model="form.borrowOrderNo" placeholder="请选择已通过的借用单" :border="false" disabled @click="showBorrow = true" />
        </u-form-item>
        <u-form-item label="归还资产" prop="assetIds" required label-width="190rpx" v-if="form.borrowOrderId">
          <view class="asset-toolbar" v-if="borrowAssets.length">
            <text class="asset-link" @click="selectAllAssets">全选</text>
            <text class="asset-link" @click="clearAssets">清空</text>
          </view>
          <view class="asset-option" v-for="a in borrowAssets" :key="a.assetId">
            <u-checkbox-group v-model="form.assetIds">
              <u-checkbox :name="a.assetId" shape="circle" activeColor="#409EFF" />
            </u-checkbox-group>
            <view class="asset-opt-info">
              <text class="asset-opt-name">{{ a.assetName }}</text>
              <text class="asset-opt-code">{{ a.assetCode }} · ×{{ a.borrowQuantity }}</text>
            </view>
          </view>
          <view v-if="assetLoading" class="empty-mini">加载中...</view>
        </u-form-item>
        <u-form-item label="归还时间" prop="planReturnTime" required label-width="190rpx">
          <u-input v-model="form.planReturnTime" placeholder="请选择计划归还时间" :border="false" disabled @click="showTime = true" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
    </view>

    <!-- 归还申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 归还申请列表</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.returnNo }}</text>
          <u-tag :text="statusText(row.returnStatus)" :type="statusType(row.returnStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">关联借用单</text><text class="value">{{ row.borrowOrderNo }}</text></view>
        <view class="item-line"><text class="label">资产数量</text><text class="value">{{ row.assetCount }}</text></view>
        <view class="item-line"><text class="label">归还时间</text><text class="value">{{ formatTime(row.planReturnTime) }}</text></view>
        <view class="item-actions" v-if="row.returnStatus === 'PENDING' && canAccept">
          <u-button size="mini" type="warning" plain @click.stop="openAccept(row)">验收</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无归还记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 借用单选择（已通过） -->
    <u-picker v-model:show="showBorrow" :columns="borrowColumns" @confirm="onBorrowConfirm"></u-picker>
    <!-- 计划归还时间 -->
    <u-datetime-picker :show="showTime" v-model="timeTs" mode="datetime" @confirm="onTimeConfirm"></u-datetime-picker>

    <!-- 验收弹窗 -->
    <u-popup :show="acceptVisible" mode="bottom" round :closeOnClickOverlay="true" @close="acceptVisible = false" height="80%">
      <view class="popup-panel">
        <view class="popup-title">归还验收</view>
        <view class="popup-no">{{ acceptForm.returnNo }}</view>
        <u-radio-group v-model="acceptForm.acceptResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="FAIL" name="FAIL">不通过</u-radio>
          <u-radio label="REPAIR" name="REPAIR">需维修</u-radio>
        </u-radio-group>
        <u-form-item label="损坏描述">
          <u-input v-model="acceptForm.damageInfo" type="textarea" placeholder="选填" :border="true" />
        </u-form-item>
        <u-form-item label="责任归属">
          <u-input v-model="acceptForm.responsibilityText" placeholder="请选择" :border="false" disabled @click="showResponsibility = true" />
        </u-form-item>
        <u-form-item label="维修费用(元)">
          <u-input v-model="acceptForm.repairCost" type="digit" placeholder="0.00" :border="false" />
        </u-form-item>
        <u-form-item label="赔偿金额(元)">
          <u-input v-model="acceptForm.compensationAmount" type="digit" placeholder="0.00" :border="false" />
        </u-form-item>
        <u-form-item label="验收意见">
          <u-input v-model="acceptForm.acceptRemark" type="textarea" placeholder="选填" :border="true" />
        </u-form-item>
        <view class="popup-actions">
          <u-button @click="acceptVisible = false">取消</u-button>
          <u-button type="primary" :loading="accepting" @click="submitAccept">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 责任归属选择 -->
    <u-picker v-model:show="showResponsibility" :columns="responsibilityColumns" @confirm="onResponsibilityConfirm"></u-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">归还单详情</view>
        <view class="detail-row"><text class="d-label">归还单号</text><text class="d-value">{{ detail.returnNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.returnStatus)" :type="statusType(detail.returnStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">关联借用单</text><text class="d-value">{{ detail.borrowOrderNo || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请人</text><text class="d-value">{{ detail.applicantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">资产数量</text><text class="d-value">{{ detail.assetCount }}</text></view>
        <view class="detail-row"><text class="d-label">计划归还</text><text class="d-value">{{ formatTime(detail.planReturnTime) }}</text></view>
        <view class="detail-row"><text class="d-label">实际归还</text><text class="d-value">{{ formatTime(detail.actualReturnTime) }}</text></view>

        <view class="sub-title">归还资产明细</view>
        <view class="vehicle-item" v-for="d in (detail.detailList || [])" :key="d.id">
          {{ d.assetCode }} · {{ d.assetName }} · ×{{ d.returnQuantity }}
        </view>

        <view class="sub-title" v-if="detail.acceptResult">验收记录</view>
        <view v-if="detail.acceptResult">
          <view class="detail-row"><text class="d-label">验收结果</text><text class="d-value">{{ acceptResultText(detail.acceptResult) }}</text></view>
          <view class="detail-row"><text class="d-label">验收时间</text><text class="d-value">{{ formatTime(detail.acceptTime) }}</text></view>
          <view class="detail-row"><text class="d-label">验收意见</text><text class="d-value">{{ detail.acceptRemark || '-' }}</text></view>
          <view class="detail-row" v-if="detail.damageInfo"><text class="d-label">损坏描述</text><text class="d-value">{{ detail.damageInfo }}</text></view>
          <view class="detail-row"><text class="d-label">责任</text><text class="d-value">{{ responsibilityText(detail.damageResponsibility) }}</text></view>
          <view class="detail-row"><text class="d-label">维修费用</text><text class="d-value">{{ detail.repairCost ?? '-' }}</text></view>
          <view class="detail-row"><text class="d-label">赔偿金额</text><text class="d-value">{{ detail.compensationAmount ?? '-' }}</text></view>
        </view>
        <view v-else class="empty">暂无验收记录</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 归还单状态映射 */
const statusMap = {
  PENDING: { text: '待验收', type: 'warning' },
  ACCEPTED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' },
  REPAIRING: { text: '维修中', type: 'primary' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const acceptResultText = (r) => ({ PASS: '通过', FAIL: '不通过', REPAIR: '需维修' }[r] || r)
const responsibilityText = (r) => ({ UNIT: '单位责任', PERSONAL: '个人责任', NATURAL: '自然损耗' }[r] || r || '-')

/** 验收权限 */
const canAccept = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER'])

/** 归还申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  borrowOrderId: null,
  borrowOrderNo: '',
  assetIds: [],
  planReturnTime: ''
})

const rules = {
  // 自定义校验：兼容数字/字符串 ID（async-validator 默认 string 类型会误拦截数字）
  borrowOrderId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择借用单'))
      else callback()
    },
    trigger: ['change']
  },
  assetIds: { required: true, message: '请选择归还资产', type: 'array', trigger: ['change'] },
  planReturnTime: { required: true, message: '请选择计划归还时间', trigger: ['change'] }
}

/** 已通过借用单下拉 */
const showBorrow = ref(false)
const borrowOrders = ref([])
const borrowColumns = computed(() => [borrowOrders.value.map((b) => ({ value: b.id, text: b.orderNo }))])

const loadBorrowOrders = async () => {
  try {
    const res = await request({ url: '/api/gc/borrow/page', data: { status: 'APPROVED', page: 1, size: 100 } })
    borrowOrders.value = res.data || []
  } catch (e) {
    borrowOrders.value = []
  }
}

const onBorrowConfirm = async (e) => {
  form.borrowOrderId = e.value[0].value
  form.borrowOrderNo = e.value[0].text
  form.assetIds = []
  assetLoading.value = true
  try {
    borrowAssets.value = (await request({ url: `/api/gc/borrow/${form.borrowOrderId}/assets` })) || []
    selectAllAssets()
  } catch (err) {
    borrowAssets.value = []
  } finally {
    assetLoading.value = false
  }
}

/** 借用资产选择 */
const assetLoading = ref(false)
const borrowAssets = ref([])

const selectAllAssets = () => {
  form.assetIds = borrowAssets.value.map((a) => a.assetId)
}
const clearAssets = () => {
  form.assetIds = []
}

/** 计划归还时间 */
const showTime = ref(false)
const timeTs = ref(Date.now())
const formatDateTime = (ts) => {
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
const onTimeConfirm = (e) => {
  timeTs.value = e.value
  form.planReturnTime = formatDateTime(e.value)
  showTime.value = false
}

/** 提交归还申请 */
const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await request({
      url: '/api/gc/return/apply',
      method: 'POST',
      data: { borrowOrderId: form.borrowOrderId, assetIds: form.assetIds, planReturnTime: form.planReturnTime }
    })
    uni.showToast({ title: '归还申请提交成功', icon: 'success' })
    resetForm()
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.borrowOrderId = null
  form.borrowOrderNo = ''
  form.assetIds = []
  form.planReturnTime = ''
  borrowAssets.value = []
  formRef.value && formRef.value.resetFields()
}

/** 归还申请列表 */
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
    const res = await request({ url: '/api/gc/return/page', data: { page: page.value, size } })
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

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({
  returnOrderId: null, returnNo: '', acceptResult: 'PASS',
  damageInfo: '', damageResponsibility: '', responsibilityText: '',
  repairCost: '', compensationAmount: '', acceptRemark: ''
})

const openAccept = (row) => {
  acceptForm.returnOrderId = row.id
  acceptForm.returnNo = row.returnNo
  acceptForm.acceptResult = 'PASS'
  acceptForm.damageInfo = ''
  acceptForm.damageResponsibility = ''
  acceptForm.responsibilityText = ''
  acceptForm.repairCost = ''
  acceptForm.compensationAmount = ''
  acceptForm.acceptRemark = ''
  acceptVisible.value = true
}

/** 责任归属 */
const showResponsibility = ref(false)
const responsibilityList = [
  { value: 'UNIT', text: '单位责任' },
  { value: 'PERSONAL', text: '个人责任' },
  { value: 'NATURAL', text: '自然损耗' }
]
const responsibilityColumns = computed(() => [responsibilityList.map((r) => ({ value: r.value, text: r.text }))])
const onResponsibilityConfirm = (e) => {
  acceptForm.damageResponsibility = e.value[0].value
  acceptForm.responsibilityText = e.value[0].text
}

const submitAccept = async () => {
  accepting.value = true
  try {
    await request({
      url: '/api/gc/return/accept',
      method: 'PUT',
      data: {
        returnOrderId: acceptForm.returnOrderId,
        acceptResult: acceptForm.acceptResult,
        damageInfo: acceptForm.damageInfo || undefined,
        damageResponsibility: acceptForm.damageResponsibility || undefined,
        repairCost: acceptForm.repairCost !== '' ? Number(acceptForm.repairCost) : undefined,
        compensationAmount: acceptForm.compensationAmount !== '' ? Number(acceptForm.compensationAmount) : undefined,
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
    detail.value = await request({ url: `/api/gc/return/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadBorrowOrders()
  loadList()
})
</script>

<style>
.gc-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.asset-toolbar { display: flex; gap: 32rpx; margin-bottom: 12rpx; }
.asset-link { color: #409eff; font-size: 26rpx; }
.asset-option { display: flex; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #f5f5f5; }
.asset-opt-info { flex: 1; display: flex; flex-direction: column; margin-left: 16rpx; }
.asset-opt-name { font-size: 28rpx; color: #333; }
.asset-opt-code { font-size: 24rpx; color: #999; margin-top: 4rpx; }
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