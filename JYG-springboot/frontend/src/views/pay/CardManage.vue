<template>
  <div class="card-manage-page">
    <!-- 用户选择 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="用户ID">
          <el-input-number v-model="userId" :min="1" :controls="false" placeholder="输入用户ID" style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询账户</el-button>
          <el-button type="success" :disabled="!account" @click="openRecharge">充值</el-button>
          <el-button type="warning" :disabled="!account" @click="openConsume">消费</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 账户信息 -->
    <el-row v-if="account" :gutter="16" class="account-row">
      <el-col :span="6">
        <el-card shadow="never" class="account-card">
          <div class="account-label">用户名</div>
          <div class="account-value">{{ account.userName || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="account-card">
          <div class="account-label">账户类型</div>
          <div class="account-value">{{ accountTypeLabel(account.accountType) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="account-card">
          <div class="account-label">卡号</div>
          <div class="account-value">{{ account.cardNo || '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="account-card balance-card">
          <div class="account-label">余额</div>
          <div class="account-value balance">￥{{ formatAmount(account.balance) }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-alert v-else type="info" :closable="false" show-icon title="请输入用户ID并点击" description="查询账户" class="no-account" />

    <!-- 交易流水 -->
    <el-card shadow="never">
      <template #header>
        <div class="tx-header">
          <span>交易流水</span>
          <div class="tx-filters">
            <el-select v-model="query.transactionType" placeholder="全部类型" clearable style="width: 120px" @change="handleQuery">
              <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
            <el-date-picker v-model="timeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 300px" @change="handleQuery" />
          </div>
        </div>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="transactionNo" label="流水号" min-width="190" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="txTagType(row.transactionType)">{{ row.transactionTypeLabel || row.transactionType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">
            <span :style="{ color: Number(row.amount) >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ Number(row.amount) > 0 ? '+' : '' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="交易方式" width="90" align="center">
          <template #default="{ row }">{{ row.payMethodLabel || row.payMethod || '-' }}</template>
        </el-table-column>
        <el-table-column prop="bizOrderNo" label="关联业务单号" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.bizOrderNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.payStatus === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else type="danger">{{ row.payStatus || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ row }">
            <el-button v-if="canRefund(row)" v-hasRole="['BIZ_ADMIN']" link type="danger" @click="handleRefund(row)">退款</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeVisible" title="餐卡充值" width="440px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="用户">
          <span>{{ account?.userName || userId }}（ID: {{ userId }}）</span>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="rechargeForm.remark" type="textarea" :rows="2" maxlength="255" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="submitRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 消费对话框 -->
    <el-dialog v-model="consumeVisible" title="餐卡消费" width="440px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="用户">
          <span>{{ account?.userName || userId }}（ID: {{ userId }}）</span>
        </el-form-item>
        <el-form-item label="消费金额">
          <el-input-number v-model="consumeForm.amount" :min="0.01" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="业务单号">
          <el-input v-model="consumeForm.bizOrderNo" maxlength="50" placeholder="如预约订单号，选填" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="consumeForm.remark" type="textarea" :rows="2" maxlength="255" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumeVisible = false">取消</el-button>
        <el-button type="primary" :loading="consuming" @click="submitConsume">确认扣款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccount, recharge, consume, refund, getTransactionPage } from '@/api/pay'

/** 交易类型 */
const typeOptions = [
  { value: 'RECHARGE', label: '充值' },
  { value: 'CONSUME', label: '消费' },
  { value: 'REFUND', label: '退款' }
]
const txTagTypeMap = {
  RECHARGE: 'success',
  CONSUME: 'warning',
  REFUND: 'info',
  DEDUCT: 'danger'
}
const txTagType = (t) => txTagTypeMap[String(t || '').toUpperCase()] || 'info'
const accountTypeLabel = (t) => (String(t || '').toUpperCase() === 'MEAL_CARD' ? '餐卡' : t || '-')

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))

/** 用户与账户 */
const userId = ref(1)
const account = ref(null)
const accountLoading = ref(false)

const loadAccount = async () => {
  accountLoading.value = true
  try {
    account.value = await getAccount(userId.value)
    loadList()
  } catch (e) {
    account.value = null
  } finally {
    accountLoading.value = false
  }
}

const handleSearch = () => {
  loadAccount()
}

/** 流水查询 */
const query = reactive({
  transactionType: '',
  page: 1,
  size: 10
})
const timeRange = ref([])
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  if (!account.value) return
  loading.value = true
  try {
    const res = await getTransactionPage({
      userId: userId.value,
      transactionType: query.transactionType || undefined,
      startTime: timeRange.value && timeRange.value.length === 2 ? timeRange.value[0] : undefined,
      endTime: timeRange.value && timeRange.value.length === 2 ? timeRange.value[1] : undefined,
      page: query.page,
      size: query.size
    })
    list.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  query.page = 1
  loadList()
}

/** 充值 */
const rechargeVisible = ref(false)
const recharging = ref(false)
const rechargeForm = reactive({ amount: undefined, remark: '' })

const openRecharge = () => {
  rechargeForm.amount = undefined
  rechargeForm.remark = ''
  rechargeVisible.value = true
}

const submitRecharge = () => {
  if (rechargeForm.amount == null || rechargeForm.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }
  recharging.value = true
  try {
    recharge({
      userId: userId.value,
      amount: rechargeForm.amount,
      remark: rechargeForm.remark || undefined
    }).then(() => {
      ElMessage.success('充值成功')
      rechargeVisible.value = false
      loadAccount()
    }).finally(() => {
      recharging.value = false
    })
  } catch (e) {
    recharging.value = false
  }
}

/** 消费 */
const consumeVisible = ref(false)
const consuming = ref(false)
const consumeForm = reactive({ amount: undefined, bizOrderNo: '', remark: '' })

const openConsume = () => {
  consumeForm.amount = undefined
  consumeForm.bizOrderNo = ''
  consumeForm.remark = ''
  consumeVisible.value = true
}

const submitConsume = () => {
  if (consumeForm.amount == null || consumeForm.amount <= 0) {
    ElMessage.warning('请输入有效的消费金额')
    return
  }
  consuming.value = true
  try {
    consume({
      userId: userId.value,
      amount: consumeForm.amount,
      bizOrderNo: consumeForm.bizOrderNo || undefined,
      remark: consumeForm.remark || undefined
    }).then(() => {
      ElMessage.success('扣款成功')
      consumeVisible.value = false
      loadAccount()
    }).finally(() => {
      consuming.value = false
    })
  } catch (e) {
    consuming.value = false
  }
}

/** 退款：仅消费且支付成功的记录 */
const canRefund = (row) => String(row.transactionType || '').toUpperCase() === 'CONSUME' && row.payStatus === 'SUCCESS'

const handleRefund = (row) => {
  ElMessageBox.confirm(`确定对消费流水 ${row.transactionNo} 退款吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await refund(row.id, { remark: '前端退款操作' })
      ElMessage.success('退款成功')
      loadAccount()
    } catch (e) {
      // 错误已由拦截器统一提示
    }
  }).catch(() => {})
}

onMounted(() => {
  loadAccount()
})
</script>

<style scoped>
.query-card {
  margin-bottom: 16px;
}

.account-row {
  margin-bottom: 16px;
}

.account-card {
  text-align: center;
}

.account-label {
  font-size: 13px;
  color: #909399;
}

.account-value {
  margin-top: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.balance {
  color: #e6a23c;
  font-size: 24px;
}

.no-account {
  margin-bottom: 16px;
}

.tx-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tx-filters {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
