<template>
  <view class="st-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.orderNo" placeholder="按采购单号搜索" :showAction="true" actionText="查询" @search="handleQuery" @custom="handleQuery"></u-search>
      <view class="filter-row">
        <u-tag v-for="s in filterStatus" :key="s.value" :text="s.label" :type="query.orderStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
      <u-button size="mini" type="primary" plain @click="openApply">新增采购</u-button>
    </view>

    <!-- 采购单列表 -->
    <view class="card">
      <view class="card-title"><text>🛒 采购管理</text><text class="total">共 {{ total }} 单</text></view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.orderNo }}</text>
          <u-tag :text="row.orderStatus" :type="statusTagType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">供应商</text><text class="value">{{ row.supplierName || '-' }}</text></view>
        <view class="item-line"><text class="label">物资数</text><text class="value">{{ row.itemCount ?? '-' }}</text></view>
        <view class="item-line"><text class="label">事由</text><text class="value">{{ row.purchaseReason }}</text></view>
        <view class="item-line"><text class="label">申请时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions">
          <u-button v-if="isDraft(row) && canApply" size="mini" type="warning" plain @click.stop="openAudit(row)">提交审批</u-button>
          <u-button v-if="isApproved(row) && canOperate" size="mini" type="success" plain @click.stop="openAccept(row)">验收</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无采购单</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增采购弹窗 -->
    <u-popup :show="applyVisible" mode="bottom" round :closeOnClickOverlay="true" @close="applyVisible = false" height="88%">
      <view class="popup-panel">
        <view class="popup-title">新增采购</view>
        <u-form :model="applyForm" ref="applyFormRef" :rules="applyRules">
          <u-form-item label="采购事由" prop="purchaseReason" required label-width="180rpx">
            <u-input v-model="applyForm.purchaseReason" type="textarea" placeholder="请填写采购事由" :border="false" />
          </u-form-item>
          <u-form-item label="供应商" label-width="180rpx">
            <u-input v-model="applyForm.supplierName" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="sub-title">物资明细</view>
        <view class="part-row" v-for="(it, i) in applyForm.items" :key="i">
          <u-input v-model="it.materialText" placeholder="选择物资" :border="true" disabled class="part-name" @click="openMaterialPicker(i)" />
          <u-input v-model="it.quantity" type="number" placeholder="数量" :border="true" class="part-num" />
          <u-input v-model="it.unitPrice" type="digit" placeholder="单价" :border="true" class="part-num" />
          <u-button v-if="applyForm.items.length > 1" size="mini" type="error" plain @click="removeItem(i)">删</u-button>
        </view>
        <u-button size="mini" type="primary" plain @click="addItem">+ 添加物资</u-button>
        <view class="total-line">合计：￥{{ applyTotal.toFixed(2) }}</view>
        <view class="popup-actions">
          <u-button @click="applyVisible = false">取消</u-button>
          <u-button type="primary" :loading="applying" @click="submitApply">提交</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 物资选择 -->
    <u-picker v-model:show="showMaterial" :columns="materialColumns" @confirm="onMaterialConfirm"></u-picker>

    <!-- 审核弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">采购审批</view>
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

    <!-- 验收弹窗 -->
    <u-popup :show="acceptVisible" mode="bottom" round :closeOnClickOverlay="true" @close="acceptVisible = false" height="60%">
      <view class="popup-panel">
        <view class="popup-title">采购验收</view>
        <view class="popup-no">{{ acceptForm.orderNo }}</view>
        <u-radio-group v-model="acceptForm.acceptResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="FAIL" name="FAIL">不通过</u-radio>
        </u-radio-group>
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
        <view class="popup-title">采购单详情</view>
        <view class="detail-row"><text class="d-label">采购单号</text><text class="d-value">{{ detail.orderNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.orderStatus" :type="statusTagType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">供应商</text><text class="d-value">{{ detail.supplierName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">事由</text><text class="d-value">{{ detail.purchaseReason }}</text></view>
        <view class="detail-row"><text class="d-label">申请时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>
        <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
        <view class="detail-row"><text class="d-label">验收人</text><text class="d-value">{{ detail.acceptUsers || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">验收时间</text><text class="d-value">{{ formatTime(detail.acceptTime) }}</text></view>
        <view class="sub-title">物资明细</view>
        <view class="vehicle-item" v-for="d in (detail.items || [])" :key="d.id">
          {{ d.materialCode }} · {{ d.materialName }} · ×{{ d.quantity }} · ￥{{ formatAmount(d.subtotal) }}
        </view>
        <view v-if="!(detail.items || []).length" class="empty-mini">暂无明细</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

const statusOptions = [
  { value: 'DRAFT', label: '草稿' }, { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已通过' }, { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' }, { value: 'EXPIRED', label: '已过期' }
]
const filterStatus = [{ value: '', label: '全部' }, ...statusOptions]
const statusTagTypeMap = { DRAFT: 'info', PENDING: 'warning', APPROVED: 'primary', COMPLETED: 'success', REJECTED: 'error', RECEIVED: 'success', EXPIRED: 'error' }
const statusTagType = (s) => statusTagTypeMap[String(s || '').toUpperCase()] || 'info'
const isDraft = (row) => String(row.orderStatus || '').toUpperCase() === 'DRAFT'
const isApproved = (row) => String(row.orderStatus || '').toUpperCase() === 'APPROVED'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))

const canApply = true
const canOperate = hasRole(['BIZ_ADMIN', 'WAREHOUSE'])

/** 查询 */
const query = reactive({ orderNo: '', orderStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/st/purchase/page', data: { orderNo: query.orderNo || undefined, orderStatus: query.orderStatus || undefined, page: query.page, size: query.size } })
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
  query.orderStatus = query.orderStatus === v ? '' : v
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 物资下拉 */
const materials = ref([])
const loadMaterials = async () => {
  try {
    const res = await request({ url: '/api/st/material/page', data: { page: 1, size: 200 } })
    materials.value = res.data || []
  } catch (e) {
    materials.value = []
  }
}

/** 新增采购 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({ purchaseReason: '', supplierName: '', items: [] })
const applyRules = { purchaseReason: { required: true, message: '请填写采购事由', trigger: ['blur'] } }
const subtotal = (row) => (Number(row.quantity || 0) * Number(row.unitPrice || 0))
const applyTotal = computed(() => applyForm.items.reduce((sum, r) => sum + subtotal(r), 0))

const addItem = () => applyForm.items.push({ materialId: null, materialText: '', quantity: '1', unitPrice: '' })
const removeItem = (i) => applyForm.items.splice(i, 1)

const openApply = () => {
  applyForm.purchaseReason = ''
  applyForm.supplierName = ''
  applyForm.items = []
  addItem()
  applyVisible.value = true
}

/** 物资选择（按行索引回填） */
const showMaterial = ref(false)
const materialColumns = computed(() => [materials.value.map((m) => ({ value: m.id, text: `${m.materialName}（${m.unit}）` }))])
let pickingIndex = 0
const openMaterialPicker = (i) => {
  pickingIndex = i
  if (!materials.value.length) loadMaterials()
  showMaterial.value = true
}
const onMaterialConfirm = (e) => {
  applyForm.items[pickingIndex].materialId = e.value[0].value
  applyForm.items[pickingIndex].materialText = e.value[0].text
}

const submitApply = async () => {
  const valid = await applyFormRef.value.validate()
  if (!valid) return
  if (!applyForm.items.length) { uni.showToast({ title: '请至少添加一项物资', icon: 'none' }); return }
  const invalid = applyForm.items.some((i) => !i.materialId || i.quantity == null || i.unitPrice == null)
  if (invalid) { uni.showToast({ title: '请完整填写物资明细', icon: 'none' }); return }
  applying.value = true
  try {
    await request({
      url: '/api/st/purchase/apply', method: 'POST',
      data: {
        purchaseReason: applyForm.purchaseReason,
        supplierName: applyForm.supplierName || undefined,
        items: applyForm.items.map((i) => ({ materialId: i.materialId, quantity: Number(i.quantity), unitPrice: Number(i.unitPrice) }))
      }
    })
    uni.showToast({ title: '提交成功', icon: 'success' })
    applyVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    applying.value = false
  }
}

/** 审核 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ orderId: null, orderNo: '', auditResult: 'PASS', auditRemark: '' })
const openAudit = (row) => {
  Object.assign(auditForm, { orderId: row.id, orderNo: row.orderNo, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}
const submitAudit = async () => {
  auditing.value = true
  try {
    await request({ url: '/api/st/purchase/audit', method: 'PUT', data: { orderId: auditForm.orderId, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark } })
    uni.showToast({ title: '审批完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    auditing.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({ orderId: null, orderNo: '', acceptResult: 'PASS', acceptRemark: '' })
const openAccept = (row) => {
  Object.assign(acceptForm, { orderId: row.id, orderNo: row.orderNo, acceptResult: 'PASS', acceptRemark: '' })
  acceptVisible.value = true
}
const submitAccept = async () => {
  accepting.value = true
  try {
    await request({
      url: '/api/st/purchase/accept', method: 'PUT',
      data: { orderId: acceptForm.orderId, acceptStatus: acceptForm.acceptResult, acceptRemark: acceptForm.acceptRemark || undefined }
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
    detail.value = await request({ url: `/api/st/purchase/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadMaterials()
  loadList(true)
})
</script>

<style>
.st-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
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
.total-line { text-align: right; color: #409eff; font-size: 30rpx; font-weight: 600; margin: 20rpx 0; }
</style>