<template>
  <view class="st-page">
    <!-- 预警卡 -->
    <view class="card" v-if="alerts.length">
      <view class="card-title small">⚠️ 库存预警</view>
      <view class="alert-item" v-for="a in alerts" :key="a.id">
        <text class="alert-name">{{ a.materialName }}</text>
        <text class="alert-detail">当前 {{ a.currentStock ?? '-' }} / 安全 {{ a.safetyStock ?? '-' }} / 上限 {{ a.maxStock ?? '-' }}</text>
      </view>
    </view>

    <!-- 操作区 -->
    <view class="card">
      <view class="filter-row">
        <u-tag v-for="t in typeOptions" :key="t.value" :text="t.label" :type="query.recordType === t.value ? 'primary' : 'info'" size="mini" plain @click="toggleType(t.value)" />
      </view>
      <view class="op-row">
        <u-button size="mini" type="warning" plain @click="openStockOut">出库</u-button>
        <u-button size="mini" type="primary" plain @click="openAdjust">盘点调整</u-button>
      </view>
    </view>

    <!-- 库存流水列表 -->
    <view class="card">
      <view class="card-title"><text>📦 库存流水</text><text class="total">共 {{ total }} 条</text></view>
      <view class="list-item" v-for="row in list" :key="row.id">
        <view class="item-head">
          <text class="item-no">{{ row.materialName || '-' }}</text>
          <u-tag :text="typeText(row.recordType)" :type="typeTagType(row.recordType)" size="mini" />
        </view>
        <view class="item-line"><text class="label">变动量</text><text class="value">{{ row.changeQuantity ?? '-' }}</text></view>
        <view class="item-line"><text class="label">结存</text><text class="value">{{ row.beforeStock ?? '-' }} → {{ row.afterStock ?? '-' }}</text></view>
        <view class="item-line"><text class="label">时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-line" v-if="row.remark"><text class="label">备注</text><text class="value">{{ row.remark }}</text></view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无流水</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 出库弹窗 -->
    <u-popup :show="outVisible" mode="bottom" round :closeOnClickOverlay="true" @close="outVisible = false" height="70%">
      <view class="popup-panel">
        <view class="popup-title">领用出库</view>
        <view class="part-row" v-for="(it, i) in outItems" :key="i">
          <u-input v-model="it.materialText" placeholder="选择物资" :border="true" disabled class="part-name" @click="openOutPicker(i)" />
          <u-input v-model="it.quantity" type="number" placeholder="数量" :border="true" class="part-num" />
          <u-button v-if="outItems.length > 1" size="mini" type="error" plain @click="removeOutItem(i)">删</u-button>
        </view>
        <u-button size="mini" type="primary" plain @click="addOutItem">+ 添加物资</u-button>
        <u-input v-model="outRemark" placeholder="出库备注，选填" :border="true" class="remark-input" />
        <view class="popup-actions">
          <u-button @click="outVisible = false">取消</u-button>
          <u-button type="primary" :loading="outLoading" @click="submitStockOut">确定出库</u-button>
        </view>
      </view>
    </u-popup>
    <u-picker v-model:show="showOutPicker" :columns="materialColumns" @confirm="onOutMaterialConfirm"></u-picker>

    <!-- 盘点调整弹窗 -->
    <u-popup :show="adjustVisible" mode="bottom" round :closeOnClickOverlay="true" @close="adjustVisible = false" height="50%">
      <view class="popup-panel">
        <view class="popup-title">盘点调整</view>
        <u-input v-model="adjustForm.materialText" placeholder="选择物资" :border="true" disabled @click="showAdjustPicker = true" />
        <u-input v-model="adjustForm.newStock" type="number" placeholder="输入新库存数量" :border="true" class="remark-input" />
        <u-input v-model="adjustForm.remark" placeholder="调整原因，选填" :border="true" />
        <view class="popup-actions">
          <u-button @click="adjustVisible = false">取消</u-button>
          <u-button type="primary" :loading="adjustLoading" @click="submitAdjust">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 物资选择 -->
    <u-picker v-model:show="showAdjustPicker" :columns="materialColumns" @confirm="onAdjustMaterialConfirm"></u-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const typeOptions = [
  { value: '', label: '全部' },
  { value: 'IN', label: '入库' },
  { value: 'OUT', label: '出库' },
  { value: 'ADJUST', label: '调整' },
  { value: 'LOSS', label: '报损' }
]
const typeText = (t) => ({ IN: '入库', OUT: '出库', ADJUST: '调整', LOSS: '报损' }[String(t || '').toUpperCase()] || t || '-')
const typeTagType = (t) => ({ IN: 'success', OUT: 'warning', ADJUST: 'info', LOSS: 'error' }[String(t || '').toUpperCase()] || 'info')
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 预警 */
const alerts = ref([])
const loadAlerts = async () => {
  try {
    alerts.value = (await request({ url: '/api/st/inventory/alerts' })) || []
  } catch (e) {
    alerts.value = []
  }
}

/** 查询 */
const query = reactive({ recordType: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/st/inventory/page', data: { recordType: query.recordType || undefined, page: query.page, size: query.size } })
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

const toggleType = (v) => {
  query.recordType = query.recordType === v ? '' : v
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 物资下拉 */
const materials = ref([])
const materialColumns = computed(() => [materials.value.map((m) => ({ value: m.id, text: `${m.materialName}（${m.unit || ''}）` }))])
const loadMaterials = async () => {
  try {
    const res = await request({ url: '/api/st/material/page', data: { page: 1, size: 200 } })
    materials.value = res.data || []
  } catch (e) {
    materials.value = []
  }
}

/** 出库 */
const outVisible = ref(false)
const outLoading = ref(false)
const outItems = ref([])
const outRemark = ref('')
let outIdx = 0
const showOutPicker = ref(false)
const openOutPicker = (i) => { outIdx = i; showOutPicker.value = true }
const onOutMaterialConfirm = (e) => {
  outItems.value[outIdx].materialId = e.value[0].value
  outItems.value[outIdx].materialText = e.value[0].text
}

const openStockOut = () => {
  outItems.value = [{ materialId: null, materialText: '', quantity: '1' }]
  outRemark.value = ''
  outVisible.value = true
}
const addOutItem = () => outItems.value.push({ materialId: null, materialText: '', quantity: '1' })
const removeOutItem = (i) => outItems.value.splice(i, 1)

const submitStockOut = async () => {
  const invalid = outItems.value.some((i) => !i.materialId || i.quantity == null)
  if (invalid) { uni.showToast({ title: '请完整填写出库明细', icon: 'none' }); return }
  outLoading.value = true
  try {
    await request({
      url: '/api/st/inventory/stock-out', method: 'POST',
      data: { items: outItems.value.map((i) => ({ materialId: i.materialId, quantity: Number(i.quantity) })), remark: outRemark.value || undefined }
    })
    uni.showToast({ title: '出库成功', icon: 'success' })
    outVisible.value = false
    loadList(true); loadMaterials(); loadAlerts()
  } catch (e) {
    // 错误已统一提示
  } finally {
    outLoading.value = false
  }
}

/** 盘点调整 */
const adjustVisible = ref(false)
const adjustLoading = ref(false)
const adjustForm = reactive({ materialId: null, materialText: '', newStock: '', remark: '' })
const showAdjustPicker = ref(false)
const onAdjustMaterialConfirm = (e) => {
  adjustForm.materialId = e.value[0].value
  adjustForm.materialText = e.value[0].text
}
const openAdjust = () => {
  Object.assign(adjustForm, { materialId: null, materialText: '', newStock: '', remark: '' })
  adjustVisible.value = true
}
const submitAdjust = async () => {
  if (!adjustForm.materialId) { uni.showToast({ title: '请选择物资', icon: 'none' }); return }
  if (adjustForm.newStock === '') { uni.showToast({ title: '请输入新库存数量', icon: 'none' }); return }
  adjustLoading.value = true
  try {
    await request({
      url: '/api/st/inventory/adjust', method: 'POST',
      data: { materialId: adjustForm.materialId, newStock: Number(adjustForm.newStock), remark: adjustForm.remark || undefined }
    })
    uni.showToast({ title: '盘点调整完成', icon: 'success' })
    adjustVisible.value = false
    loadList(true); loadMaterials(); loadAlerts()
  } catch (e) {
    // 错误已统一提示
  } finally {
    adjustLoading.value = false
  }
}

onMounted(() => {
  loadList(true)
  loadMaterials()
  loadAlerts()
})
</script>

<style>
.st-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.card-title.small { font-size: 28rpx; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.op-row { display: flex; gap: 16rpx; }
.alert-item { background: #fef0f0; border: 1rpx solid #fbc4c4; border-radius: 8rpx; padding: 14rpx 18rpx; margin-bottom: 12rpx; display: flex; flex-direction: column; }
.alert-name { font-size: 28rpx; color: #c0392b; font-weight: 600; }
.alert-detail { font-size: 24rpx; color: #e74c3c; margin-top: 4rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.part-row { display: flex; align-items: center; gap: 10rpx; margin-bottom: 12rpx; }
.part-name { flex: 1; }
.part-num { width: 160rpx; }
.remark-input { margin: 20rpx 0; }
</style>