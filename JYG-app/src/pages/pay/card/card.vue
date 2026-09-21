<template>
  <view class="pay-page">
    <!-- 用户查询 -->
    <view class="card">
      <view class="card-title">💳 餐卡账户</view>
      <u-input v-model="userId" type="number" placeholder="输入用户ID" :border="true" />
      <view class="op-row">
        <u-button size="mini" type="primary" plain @click="handleSearch">查询账户</u-button>
        <u-button size="mini" type="success" plain :disabled="!account" @click="openRecharge">充值</u-button>
        <u-button size="mini" type="warning" plain :disabled="!account" @click="openConsume">消费</u-button>
      </view>
    </view>

    <!-- 账户信息 -->
    <view class="card" v-if="account">
      <view class="account-grid">
        <view class="a-cell"><text class="a-label">用户名</text><text class="a-value">{{ account.userName || '-' }}</text></view>
        <view class="a-cell"><text class="a-label">账户类型</text><text class="a-value">{{ accountTypeLabel(account.accountType) }}</text></view>
        <view class="a-cell"><text class="a-label">卡号</text><text class="a-value">{{ account.cardNo || '-' }}</text></view>
        <view class="a-cell"><text class="a-label">余额</text><text class="a-value balance">￥{{ formatAmount(account.balance) }}</text></view>
      </view>
    </view>
    <view class="card" v-else>
      <view class="empty">请输入用户ID并点击"查询账户"</view>
    </view>

    <!-- 交易流水 -->
    <view class="card">
      <view class="card-title"><text>📃 交易流水</text><text class="total">共 {{ total }} 条</text></view>
      <view class="filter-row">
        <u-tag v-for="t in typeOptions" :key="t.value" :text="t.label" :type="query.transactionType === t.value ? 'primary' : 'info'" size="mini" plain @click="toggleType(t.value)" />
      </view>
      <view class="list-item" v-for="row in list" :key="row.id">
        <view class="item-head">
          <text class="item-no">{{ row.transactionNo }}</text>
          <u-tag :text="row.transactionTypeLabel || row.transactionType" :type="txTagType(row.transactionType)" size="mini" />
        </view>
        <view class="item-line"><text class="label">金额</text><text class="value" :style="{ color: Number(row.amount) >= 0 ? '#67c23a' : '#f56c6c' }">{{ Number(row.amount) > 0 ? '+' : '' }}{{ row.amount }}</text></view>
        <view class="item-line"><text class="label">方式</text><text class="value">{{ row.payMethodLabel || row.payMethod || '-' }} · {{ row.payStatus === 'SUCCESS' ? '成功' : row.payStatus || '-' }}</text></view>
        <view class="item-line"><text class="label">时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions" v-if="canRefund(row) && canRefundOp">
          <u-button size="mini" type="error" plain @click.stop="handleRefund(row)">退款</u-button>
        </view>
      </view>
      <view v-if="!loading && !list.length" class="empty">暂无流水</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 充值弹窗 -->
    <u-popup :show="rechargeVisible" mode="bottom" round :closeOnClickOverlay="true" @close="rechargeVisible = false">
      <view class="popup-panel">
        <view class="popup-title">餐卡充值</view>
        <view class="popup-no">{{ account?.userName || userId }}（ID: {{ userId }}）</view>
        <u-input v-model="rechargeForm.amount" type="digit" placeholder="充值金额" :border="true" class="field" />
        <u-input v-model="rechargeForm.remark" placeholder="备注，选填" :border="true" class="field" />
        <view class="popup-actions">
          <u-button @click="rechargeVisible = false">取消</u-button>
          <u-button type="primary" :loading="recharging" @click="submitRecharge">确认充值</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 消费弹窗 -->
    <u-popup :show="consumeVisible" mode="bottom" round :closeOnClickOverlay="true" @close="consumeVisible = false">
      <view class="popup-panel">
        <view class="popup-title">餐卡消费</view>
        <view class="popup-no">{{ account?.userName || userId }}（ID: {{ userId }}）</view>
        <u-input v-model="consumeForm.amount" type="digit" placeholder="消费金额" :border="true" class="field" />
        <u-input v-model="consumeForm.bizOrderNo" placeholder="业务单号，选填" :border="true" class="field" />
        <u-input v-model="consumeForm.remark" placeholder="备注，选填" :border="true" class="field" />
        <view class="popup-actions">
          <u-button @click="consumeVisible = false">取消</u-button>
          <u-button type="primary" :loading="consuming" @click="submitConsume">确认扣款</u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

const typeOptions = [
  { value: '', label: '全部' },
  { value: 'RECHARGE', label: '充值' },
  { value: 'CONSUME', label: '消费' },
  { value: 'REFUND', label: '退款' }
]
const txTagType = (t) => ({ RECHARGE: 'success', CONSUME: 'warning', REFUND: 'info', DEDUCT: 'error' }[String(t || '').toUpperCase()] || 'info')
const accountTypeLabel = (t) => (String(t || '').toUpperCase() === 'MEAL_CARD' ? '餐卡' : t || '-')
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))
const canRefundOp = hasRole(['BIZ_ADMIN'])

const userId = ref('1')
const account = ref(null)

const loadAccount = async () => {
  try {
    account.value = await request({ url: `/api/pay/account/${userId.value}` })
    loadList(true)
  } catch (e) {
    account.value = null
  }
}
const handleSearch = () => { loadAccount() }

/** 流水 */
const query = reactive({ transactionType: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (!account.value) return
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({
      url: '/api/pay/transactions/page',
      data: { userId: userId.value, transactionType: query.transactionType || undefined, page: query.page, size: query.size }
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

const toggleType = (v) => { query.transactionType = query.transactionType === v ? '' : v; loadList(true) }
onReachBottom(() => { loadList() })

/** 充值 */
const rechargeVisible = ref(false)
const recharging = ref(false)
const rechargeForm = reactive({ amount: '', remark: '' })
const openRecharge = () => { rechargeForm.amount = ''; rechargeForm.remark = ''; rechargeVisible.value = true }
const submitRecharge = async () => {
  if (rechargeForm.amount === '' || Number(rechargeForm.amount) <= 0) { uni.showToast({ title: '请输入有效的充值金额', icon: 'none' }); return }
  recharging.value = true
  try {
    await request({ url: '/api/pay/recharge', method: 'POST', data: { userId: Number(userId.value), amount: Number(rechargeForm.amount), remark: rechargeForm.remark || undefined } })
    uni.showToast({ title: '充值成功', icon: 'success' })
    rechargeVisible.value = false
    loadAccount()
  } catch (e) { /* 错误已统一提示 */ } finally { recharging.value = false }
}

/** 消费 */
const consumeVisible = ref(false)
const consuming = ref(false)
const consumeForm = reactive({ amount: '', bizOrderNo: '', remark: '' })
const openConsume = () => { consumeForm.amount = ''; consumeForm.bizOrderNo = ''; consumeForm.remark = ''; consumeVisible.value = true }
const submitConsume = async () => {
  if (consumeForm.amount === '' || Number(consumeForm.amount) <= 0) { uni.showToast({ title: '请输入有效的消费金额', icon: 'none' }); return }
  consuming.value = true
  try {
    await request({ url: '/api/pay/consume', method: 'POST', data: { userId: Number(userId.value), amount: Number(consumeForm.amount), bizOrderNo: consumeForm.bizOrderNo || undefined, remark: consumeForm.remark || undefined } })
    uni.showToast({ title: '扣款成功', icon: 'success' })
    consumeVisible.value = false
    loadAccount()
  } catch (e) { /* 错误已统一提示 */ } finally { consuming.value = false }
}

/** 退款 */
const canRefund = (row) => String(row.transactionType || '').toUpperCase() === 'CONSUME' && row.payStatus === 'SUCCESS'
const handleRefund = (row) => {
  uni.showModal({
    title: '提示', content: `确定对消费流水 ${row.transactionNo} 退款吗？`, confirmText: '确定', cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await request({ url: `/api/pay/refund/${row.id}`, method: 'POST', data: { remark: '前端退款操作' } })
        uni.showToast({ title: '退款成功', icon: 'success' })
        loadAccount()
      } catch (e) { /* 错误已统一提示 */ }
    }
  })
}

onMounted(() => { loadAccount() })
</script>

<style>
.pay-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.op-row { display: flex; gap: 16rpx; margin-top: 20rpx; }
.account-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12rpx; }
.a-cell { background: #f5f7fa; border-radius: 8rpx; padding: 16rpx; }
.a-label { display: block; font-size: 22rpx; color: #999; }
.a-value { display: block; font-size: 28rpx; color: #333; font-weight: 600; margin-top: 6rpx; }
.a-value.balance { color: #e6a23c; font-size: 34rpx; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 16rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 26rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; justify-content: flex-end; margin-top: 10rpx; }
.empty { text-align: center; color: #999; padding: 40rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.field { margin-bottom: 20rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
</style>