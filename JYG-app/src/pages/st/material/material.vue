<template>
  <view class="st-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.materialName" placeholder="按物资名称搜索" :showAction="true" actionText="查询" @search="handleQuery" @custom="handleQuery"></u-search>
      <view class="filter-row">
        <u-tag v-for="c in categoryOptions" :key="c.value" :text="c.label" :type="query.category === c.value ? 'primary' : 'info'" size="mini" plain @click="toggleCategory(c.value)" />
      </view>
    </view>

    <!-- 物资列表 -->
    <view class="card">
      <view class="card-title"><text>🥬 物资档案</text><text class="total">共 {{ total }} 项</text></view>
      <u-button v-if="canManage" size="mini" type="primary" plain class="add-btn" @click="openSave()">新增物资</u-button>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.materialName }}</text>
          <u-tag :text="categoryName(row.category)" :type="categoryTagType(row.category)" size="mini" />
        </view>
        <view class="item-line"><text class="label">编码</text><text class="value">{{ row.materialCode }} · {{ row.spec || '-' }}</text></view>
        <view class="item-line"><text class="label">单位</text><text class="value">{{ row.unit }}</text></view>
        <view class="item-line"><text class="label">当前库存</text><text class="value">{{ row.currentStock ?? '-' }}（安全 {{ row.safetyStock ?? '-' }} / 上限 {{ row.maxStock ?? '-' }}）</text></view>
        <view class="item-line"><text class="label">当前价格</text><text class="value">￥{{ row.currentPrice == null ? '-' : row.currentPrice }}</text></view>
        <view class="item-actions" v-if="canManage">
          <u-button size="mini" type="primary" plain @click.stop="openSave(row)">编辑</u-button>
          <u-button size="mini" type="error" plain @click.stop="handleDelete(row)">删除</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无物资</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增/编辑弹窗 -->
    <u-popup :show="saveVisible" mode="bottom" round :closeOnClickOverlay="true" @close="saveVisible = false" height="88%">
      <view class="popup-panel">
        <view class="popup-title">{{ form.id ? '编辑物资' : '新增物资' }}</view>
        <u-form :model="form" ref="formRef" :rules="rules">
          <u-form-item label="物资编码" prop="materialCode" required label-width="190rpx">
            <u-input v-model="form.materialCode" placeholder="请输物资编码" :border="false" />
          </u-form-item>
          <u-form-item label="物资名称" prop="materialName" required label-width="190rpx">
            <u-input v-model="form.materialName" placeholder="请输物资名称" :border="false" />
          </u-form-item>
          <u-form-item label="分类" prop="category" required label-width="190rpx">
            <u-input v-model="form.categoryText" placeholder="请选择分类" :border="false" disabled @click="showCategory = true" />
          </u-form-item>
          <u-form-item label="规格" label-width="190rpx">
            <u-input v-model="form.spec" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="计量单位" prop="unit" required label-width="190rpx">
            <u-input v-model="form.unit" placeholder="如：kg、瓶" :border="false" />
          </u-form-item>
          <u-form-item label="保质期(天)" label-width="190rpx">
            <u-input v-model="form.shelfLife" type="number" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="安全库存" label-width="190rpx">
            <u-input v-model="form.safetyStock" type="number" placeholder="0" :border="false" />
          </u-form-item>
          <u-form-item label="库存上限" label-width="190rpx">
            <u-input v-model="form.maxStock" type="number" placeholder="1000" :border="false" />
          </u-form-item>
          <u-form-item label="当前价格" label-width="190rpx">
            <u-input v-model="form.currentPrice" type="digit" placeholder="元" :border="false" />
          </u-form-item>
          <u-form-item label="备注" label-width="190rpx">
            <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="saveVisible = false">取消</u-button>
          <u-button type="primary" :loading="saving" @click="submitForm">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 分类 -->
    <u-picker v-model:show="showCategory" :columns="categoryColumns" @confirm="onCategoryConfirm"></u-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="70%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">物资详情</view>
        <view class="detail-row"><text class="d-label">编码</text><text class="d-value">{{ detail.materialCode }}</text></view>
        <view class="detail-row"><text class="d-label">名称</text><text class="d-value">{{ detail.materialName }}</text></view>
        <view class="detail-row"><text class="d-label">分类</text><text class="d-value">{{ categoryName(detail.category) }}</text></view>
        <view class="detail-row"><text class="d-label">规格</text><text class="d-value">{{ detail.spec || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">单位</text><text class="d-value">{{ detail.unit }}</text></view>
        <view class="detail-row"><text class="d-label">保质期</text><text class="d-value">{{ detail.shelfLife ?? '-' }} 天</text></view>
        <view class="detail-row"><text class="d-label">安全库存</text><text class="d-value">{{ detail.safetyStock ?? '-' }}</text></view>
        <view class="detail-row"><text class="d-label">库存上限</text><text class="d-value">{{ detail.maxStock ?? '-' }}</text></view>
        <view class="detail-row"><text class="d-label">当前库存</text><text class="d-value">{{ detail.currentStock ?? '-' }}</text></view>
        <view class="detail-row"><text class="d-label">当前价格</text><text class="d-value">￥{{ detail.currentPrice == null ? '-' : detail.currentPrice }}</text></view>
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

const categoryOptions = [
  { value: 'FRESH_INGREDIENTS', text: '生鲜食材' },
  { value: 'CONDIMENT', text: '调味品' },
  { value: 'DAILY_GOODS', text: '日用品' }
]
const categoryName = (c) => (categoryOptions.find((x) => x.value === String(c || '').toUpperCase()) || {}).text || c
const categoryTagType = (c) => {
  const map = { FRESH_INGREDIENTS: 'success', CONDIMENT: 'warning', DAILY_GOODS: 'primary' }
  return map[String(c || '').toUpperCase()] || 'primary'
}

const canManage = hasRole(['BIZ_ADMIN', 'WAREHOUSE'])

/** 查询 */
const query = reactive({ materialName: '', category: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/st/material/page', data: { materialName: query.materialName || undefined, category: query.category || undefined, page: query.page, size: query.size } })
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
const toggleCategory = (v) => {
  query.category = query.category === v ? '' : v
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 表单 */
const saveVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null, materialCode: '', materialName: '', category: '', categoryText: '',
  spec: '', unit: '', shelfLife: '', safetyStock: '', maxStock: '', currentPrice: '', remark: ''
})
const rules = {
  materialCode: { required: true, message: '请输入物资编码', trigger: ['blur'] },
  materialName: { required: true, message: '请输入物资名称', trigger: ['blur'] },
  category: { required: true, message: '请选择分类', trigger: ['change'] },
  unit: { required: true, message: '请输入计量单位', trigger: ['blur'] }
}

const openSave = (row) => {
  Object.assign(form, {
    id: row ? row.id : null,
    materialCode: row ? row.materialCode : '',
    materialName: row ? row.materialName : '',
    category: row ? String(row.category || '').toUpperCase() : '',
    categoryText: row ? categoryName(row.category) : '',
    spec: row ? row.spec || '' : '',
    unit: row ? row.unit : '',
    shelfLife: row && row.shelfLife != null ? String(row.shelfLife) : '',
    safetyStock: row ? String(row.safetyStock ?? 0) : '0',
    maxStock: row ? String(row.maxStock ?? 1000) : '1000',
    currentPrice: row && row.currentPrice != null ? String(row.currentPrice) : '',
    remark: row ? row.remark || '' : ''
  })
  saveVisible.value = true
}

const showCategory = ref(false)
const categoryColumns = computed(() => [categoryOptions.map((c) => ({ value: c.value, text: c.text }))])
const onCategoryConfirm = (e) => {
  form.category = e.value[0].value
  form.categoryText = e.value[0].text
}

const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  try {
    await request({
      url: '/api/st/material/save', method: 'POST',
      data: {
        id: form.id || undefined,
        materialCode: form.materialCode,
        materialName: form.materialName,
        category: form.category,
        spec: form.spec || undefined,
        unit: form.unit,
        shelfLife: form.shelfLife !== '' ? Number(form.shelfLife) : undefined,
        safetyStock: form.safetyStock !== '' ? Number(form.safetyStock) : 0,
        maxStock: form.maxStock !== '' ? Number(form.maxStock) : 1000,
        currentPrice: form.currentPrice !== '' ? Number(form.currentPrice) : undefined,
        remark: form.remark || undefined
      }
    })
    uni.showToast({ title: form.id ? '编辑成功' : '新增成功', icon: 'success' })
    saveVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    saving.value = false
  }
}

/** 删除 */
const handleDelete = (row) => {
  uni.showModal({
    title: '提示', content: `确定删除物资「${row.materialName}」吗？`, confirmText: '确定', cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/st/material/${row.id}`, method: 'DELETE' })
        uni.showToast({ title: '删除成功', icon: 'success' })
        loadList(true)
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
    detail.value = await request({ url: `/api/st/material/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadList(true)
})
</script>

<style>
.st-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 170rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.add-btn { margin-bottom: 16rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>