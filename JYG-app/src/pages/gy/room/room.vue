<template>
  <view class="gy-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.building" placeholder="按楼栋搜索" :showAction="true" actionText="查询" @search="handleQuery" @custom="handleQuery"></u-search>
      <view class="filter-row">
        <u-tag v-for="s in filterStatus" :key="s.value" :text="s.label" :type="query.roomStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
    </view>

    <!-- 房间列表 -->
    <view class="card">
      <view class="card-title">
        <text>🏠 公寓房间</text>
        <text class="total">共 {{ total }} 间</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.roomNo }}</text>
          <u-tag :text="row.roomStatusLabel || row.roomStatus" :type="statusType(row.roomStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">楼栋</text><text class="value">{{ row.building }} · {{ row.floor }}层</text></view>
        <view class="item-line"><text class="label">类型</text><text class="value">{{ row.roomTypeLabel || row.roomType }} · {{ row.layout || '-' }}</text></view>
        <view class="item-line"><text class="label">面积</text><text class="value">{{ row.area ?? '-' }} ㎡</text></view>
        <view class="item-line"><text class="label">入住人</text><text class="value">{{ row.currentOccupantName || '-' }}</text></view>
        <view class="item-actions" v-if="canManage">
          <u-button size="mini" type="primary" plain @click.stop="openEdit(row)">编辑</u-button>
          <u-button size="mini" type="error" plain @click.stop="handleDelete(row)">删除</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无房间</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增/编辑弹窗 -->
    <u-popup :show="formVisible" mode="bottom" round :closeOnClickOverlay="true" @close="formVisible = false" height="85%">
      <view class="popup-panel">
        <view class="popup-title">{{ form.id ? '编辑房间' : '新增房间' }}</view>
        <u-form :model="form" ref="formRef" :rules="rules">
          <u-form-item label="楼栋" prop="building" required label-width="180rpx">
            <u-input v-model="form.building" placeholder="如：专家公寓A栋" :border="false" />
          </u-form-item>
          <u-form-item label="楼层" prop="floor" required label-width="180rpx">
            <u-input v-model="form.floor" type="number" placeholder="楼层" :border="false" />
          </u-form-item>
          <u-form-item label="房间号" prop="roomNo" required label-width="180rpx">
            <u-input v-model="form.roomNo" placeholder="如：A-0501" :border="false" />
          </u-form-item>
          <u-form-item label="房间类型" prop="roomType" required label-width="180rpx">
            <u-input v-model="form.roomTypeText" placeholder="请选择" :border="false" disabled @click="showType = true" />
          </u-form-item>
          <u-form-item label="户型" label-width="180rpx">
            <u-input v-model="form.layout" placeholder="如：两室一厅" :border="false" />
          </u-form-item>
          <u-form-item label="面积(㎡)" label-width="180rpx">
            <u-input v-model="form.area" type="digit" placeholder="选填" :border="false" />
          </u-form-item>
          <u-form-item label="配套设施" label-width="180rpx">
            <u-input v-model="form.facilitiesText" type="textarea" placeholder='JSON格式，如 {"家具":["床"],"家电":["空调"]}' :border="false" />
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

    <!-- 房间类型 -->
    <u-picker v-model:show="showType" :columns="typeColumns" @confirm="onTypeConfirm"></u-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">房间详情</view>
        <view class="detail-row"><text class="d-label">楼栋</text><text class="d-value">{{ detail.building || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">楼层</text><text class="d-value">{{ detail.floor ?? '-' }}</text></view>
        <view class="detail-row"><text class="d-label">房间号</text><text class="d-value">{{ detail.roomNo || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">房间类型</text><text class="d-value">{{ detail.roomTypeLabel || detail.roomType }}</text></view>
        <view class="detail-row"><text class="d-label">户型</text><text class="d-value">{{ detail.layout || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">面积</text><text class="d-value">{{ detail.area ?? '-' }} ㎡</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.roomStatusLabel || detail.roomStatus" :type="statusType(detail.roomStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">当前入住人</text><text class="d-value">{{ detail.currentOccupantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">入住人数</text><text class="d-value">{{ detail.occupantCount ?? 0 }}</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
        <view class="sub-title">配套设施</view>
        <view v-if="detail.facilities" class="facilities-pre">{{ formatFacilities(detail.facilities) }}</view>
        <view v-else class="empty-mini">暂无配套设施</view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 房间类型 */
const roomTypes = [
  { value: 'expert_apartment', text: '专家公寓' },
  { value: 'talent_apartment', text: '人才公寓' }
]
const typeText = (v) => (roomTypes.find((t) => t.value === String(v || '').toLowerCase()) || {}).text || v

/** 状态 */
const statusOptions = [
  { value: 'idle', label: '空闲' },
  { value: 'occupied', label: '已入住' },
  { value: 'repairing', label: '维修中' },
  { value: 'reserved', label: '已预留' }
]
const statusTypeMap = { idle: 'success', occupied: 'primary', repairing: 'warning', reserved: 'info' }
const statusType = (s) => statusTypeMap[String(s || '').toLowerCase()] || 'info'
const filterStatus = [{ value: '', label: '全部' }, ...statusOptions]

/** 管理权限 */
const canManage = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DEPT_MANAGER'])

/** 查询 */
const query = reactive({ building: '', roomStatus: '', page: 1, size: 10 })
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
      url: '/api/gy/room/page',
      data: {
        building: query.building || undefined,
        roomStatus: query.roomStatus || undefined,
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

const handleQuery = () => { query.page = 1; finished.value = false; loadList(true) }
const toggleStatus = (v) => {
  query.roomStatus = query.roomStatus === v ? '' : v
  handleQuery()
}

onReachBottom(() => { loadList() })

/** 新增/编辑表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null, building: '', floor: '', roomNo: '', roomType: '', roomTypeText: '',
  layout: '', area: '', facilitiesText: '', remark: ''
})

const rules = {
  building: { required: true, message: '请输入楼栋', trigger: ['blur'] },
  floor: { required: true, message: '请输入楼层', trigger: ['blur'] },
  roomNo: { required: true, message: '请输入房间号', trigger: ['blur'] },
  roomType: { required: true, message: '请选择房间类型', trigger: ['change'] }
}

const openEdit = async (row) => {
  try {
    const d = await request({ url: `/api/gy/room/${row.id}` })
    Object.assign(form, {
      id: d.id, building: d.building, floor: d.floor != null ? String(d.floor) : '',
      roomNo: d.roomNo, roomType: String(d.roomType || '').toLowerCase(), roomTypeText: typeText(d.roomType),
      layout: d.layout || '', area: d.area != null ? String(d.area) : '',
      facilitiesText: d.facilities ? formatFacilities(d.facilities) : '',
      remark: d.remark || ''
    })
    formVisible.value = true
  } catch (e) {
    // 错误已统一提示
  }
}

/** 类型选择 */
const showType = ref(false)
const typeColumns = computed(() => [roomTypes.map((t) => ({ value: t.value, text: t.text }))])
const onTypeConfirm = (e) => {
  form.roomType = e.value[0].value
  form.roomTypeText = e.value[0].text
}

/** 配套设施解析：把数值计数（现存脏数据）归一化为后端 Map<String,List<String>> 形式 */
const parseFacilities = () => {
  const text = (form.facilitiesText || '').trim()
  if (!text) return undefined
  try {
    const obj = JSON.parse(text)
    if (obj && typeof obj === 'object' && !Array.isArray(obj)) {
      const normalized = {}
      for (const k of Object.keys(obj)) {
        const v = obj[k]
        normalized[k] = Array.isArray(v)
          ? v.map((x) => String(x))
          : (typeof v === 'number' ? [String(v)] : [String(v)])
      }
      return normalized
    }
    throw new Error('invalid')
  } catch (e) {
    uni.showToast({ title: '配套设施需为JSON对象格式', icon: 'none' })
    return null
  }
}

const submitForm = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  const facilities = parseFacilities()
  if (facilities === null) return
  saving.value = true
  try {
    await request({
      url: '/api/gy/room/save',
      method: 'POST',
      data: {
        id: form.id || undefined,
        building: form.building,
        floor: Number(form.floor),
        roomNo: form.roomNo,
        roomType: form.roomType,
        layout: form.layout || undefined,
        area: form.area !== '' ? Number(form.area) : undefined,
        facilities,
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
    title: '删除确认',
    content: `确定删除房间「${row.roomNo}」吗？`,
    confirmText: '删除',
    cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/gy/room/${row.id}`, method: 'DELETE' })
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
    detail.value = await request({ url: `/api/gy/room/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

const formatFacilities = (facilities) => {
  if (!facilities) return ''
  try {
    return JSON.stringify(JSON.parse(facilities), null, 2)
  } catch (e) {
    return facilities
  }
}

onMounted(() => {
  loadList(true)
})
</script>

<style>
.gy-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
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
.empty-mini { text-align: center; color: #999; padding: 20rpx 0; font-size: 24rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
.sub-title { font-size: 30rpx; font-weight: 600; margin: 24rpx 0 12rpx; }
.facilities-pre { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx 20rpx; font-size: 26rpx; line-height: 1.6; white-space: pre-wrap; word-break: break-all; }
</style>