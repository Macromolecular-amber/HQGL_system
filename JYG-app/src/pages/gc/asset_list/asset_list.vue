<template>
  <view class="gc-page">
    <!-- 搜索区 -->
    <view class="card">
      <u-search v-model="query.assetName" placeholder="按资产名称搜索" :showAction="true" actionText="查询" @search="handleQuery" @custom="handleQuery"></u-search>
      <view class="filter-row">
        <u-tag v-for="s in statusOptions" :key="s.value" :text="s.label" :type="query.assetStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
      <view class="filter-row">
        <u-tag v-for="c in categories" :key="c.code" :text="c.name" :type="query.categoryCode === c.code ? 'primary' : 'info'" size="mini" plain @click="toggleCategory(c.code)" />
      </view>
    </view>

    <!-- 资产列表 -->
    <view class="card">
      <view class="card-title"><text>🏷️ 资产列表</text><text class="total">共 {{ total }} 项</text></view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.assetName }}</text>
          <u-tag :text="statusText(row.assetStatus)" :type="statusType(row.assetStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">编号</text><text class="value">{{ row.assetCode }} · {{ row.categoryName || '-' }}</text></view>
        <view class="item-line"><text class="label">原值</text><text class="value">￥{{ row.originalValue }} · {{ row.location || '-' }}</text></view>
        <view class="item-line"><text class="label">权属单位</text><text class="value">{{ row.ownerUnitName || '-' }}</text></view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无资产</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">资产详情</view>
        <view class="detail-row"><text class="d-label">资产编号</text><text class="d-value">{{ detail.assetCode }}</text></view>
        <view class="detail-row"><text class="d-label">资产名称</text><text class="d-value">{{ detail.assetName }}</text></view>
        <view class="detail-row"><text class="d-label">分类</text><text class="d-value">{{ detail.categoryName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">规格型号</text><text class="d-value">{{ detail.specModel || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">品牌</text><text class="d-value">{{ detail.brand || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">数量</text><text class="d-value">{{ detail.quantity }}</text></view>
        <view class="detail-row"><text class="d-label">原值</text><text class="d-value">￥{{ detail.originalValue }}</text></view>
        <view class="detail-row"><text class="d-label">当前净值</text><text class="d-value">￥{{ detail.currentValue }}</text></view>
        <view class="detail-row"><text class="d-label">累计折旧</text><text class="d-value">￥{{ detail.accumulatedDepreciation }}</text></view>
        <view class="detail-row"><text class="d-label">购置日期</text><text class="d-value">{{ detail.purchaseDate || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">使用年限</text><text class="d-value">{{ detail.usefulLife || '-' }} 年</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.assetStatus)" :type="statusType(detail.assetStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">存放地点</text><text class="d-value">{{ detail.location || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">权属单位</text><text class="d-value">{{ detail.unitName || detail.ownerUnitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">二维码</text><text class="d-value">{{ detail.qrCodeUrl || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">RFID</text><text class="d-value">{{ detail.rfidTag || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">描述</text><text class="d-value">{{ detail.description || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">备注</text><text class="d-value">{{ detail.remark || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">入仓时间</text><text class="d-value">{{ formatTime(detail.inStockTime) }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'

const categories = [
  { code: 'JJ_01', name: '办公家具' },
  { code: 'IT_01', name: '办公设备' },
  { code: 'DQ_01', name: '电器设备' },
  { code: 'CL_01', name: '车辆' }
]
const statusOptions = [
  { value: '', label: '全部' },
  { value: 'IN_STOCK', label: '在仓' },
  { value: 'BORROWED', label: '已借用' },
  { value: 'TRANSFERRED', label: '已调剂' },
  { value: 'DISPOSED', label: '已处置' },
  { value: 'REPAIRING', label: '维修中' }
]
const statusMap = {
  PENDING: { text: '待审核', type: 'warning' }, IN_STOCK: { text: '在仓', type: 'success' },
  REJECTED: { text: '已驳回', type: 'error' }, BORROWED: { text: '已借用', type: 'primary' },
  TRANSFERRED: { text: '已调剂', type: 'info' }, DISPOSED: { text: '已处置', type: 'info' }, REPAIRING: { text: '维修中', type: 'error' }
}
const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 查询 */
const query = reactive({ assetName: '', categoryCode: '', assetStatus: '', page: 1, size: 10 })
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
      url: '/api/gc/asset/list',
      data: {
        assetName: query.assetName || undefined,
        categoryCode: query.categoryCode || undefined,
        assetStatus: query.assetStatus || undefined,
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
const toggleStatus = (v) => { query.assetStatus = query.assetStatus === v ? '' : v; loadList(true) }
const toggleCategory = (v) => { query.categoryCode = query.categoryCode === v ? '' : v; loadList(true) }

onReachBottom(() => { loadList() })

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gc/asset/detail/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => { loadList(true) })
</script>

<style>
.gc-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 16rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 140rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 24rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>