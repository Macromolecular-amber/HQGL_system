<template>
  <div class="transfer-apply-page">
    <!-- 调剂申请表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">调剂申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="调剂资产" prop="assetIds">
          <div class="asset-picker">
            <el-button @click="openAssetDialog">选择资产</el-button>
            <span class="placeholder">从在仓资产中多选</span>
          </div>
          <el-table v-if="selectedAssets.length" :data="selectedAssets" size="small" border max-height="220">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="assetCode" label="资产编号" width="190" show-overflow-tooltip />
            <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="specModel" label="规格型号" min-width="110">
              <template #default="{ row }">{{ row.specModel || '-' }}</template>
            </el-table-column>
            <el-table-column width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeAsset($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="尚未选择调剂资产" :image-size="60" />
        </el-form-item>
        <el-form-item label="接收单位" prop="receiveUnitId">
          <el-select v-model="form.receiveUnitId" placeholder="请选择接收单位" filterable style="width: 320px">
            <el-option v-for="u in receiveUnits" :key="u.id" :label="u.unitName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调剂事由" prop="applyReason">
          <el-input v-model="form.applyReason" type="textarea" :rows="2" maxlength="500" placeholder="请填写调剂事由" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 调剂申请列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">调剂申请列表</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="调剂单号" width="150" show-overflow-tooltip />
        <el-table-column prop="assetCount" label="资产数量" width="90" align="center" />
        <el-table-column prop="receiveUnitName" label="接收单位" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.receiveUnitName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="applyReason" label="事由" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">{{ statusText(row.orderStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 'PENDING'" link type="warning" @click="openAudit(row)">
              审核
            </el-button>
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

    <!-- 资产选择弹窗 -->
    <el-dialog v-model="assetDialogVisible" title="选择调剂资产" width="780px">
      <div class="dialog-search">
        <el-input
          v-model="assetSearch"
          placeholder="按资产名称/编号搜索"
          clearable
          :prefix-icon="Search"
          style="width: 260px"
        />
      </div>
      <el-table
        ref="assetTableRef"
        v-loading="assetLoading"
        :data="pagedAssets"
        row-key="id"
        border
        max-height="400"
        @selection-change="onAssetSelectionChange"
      >
        <el-table-column type="selection" width="45" reserve-selection />
        <el-table-column prop="assetCode" label="资产编号" width="190" show-overflow-tooltip />
        <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="originalValue" label="原值(元)" width="100" align="right" />
        <el-table-column prop="location" label="存放地点" min-width="110">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="assetQuery.page"
          v-model:page-size="assetQuery.size"
          :total="filteredAssets.length"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="onAssetPageChange"
          @current-change="onAssetPageChange"
        />
      </div>
      <template #footer>
        <el-button @click="assetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssets">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="调剂审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="调剂单号">
          <span>{{ auditForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="审批结果" required>
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="调剂单详情" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="调剂单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.orderStatus)">{{ statusText(detail.orderStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请单位">{{ detail.applicantUnitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="接收单位">{{ detail.receiveUnitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资产数量">{{ detail.assetCount }}</el-descriptions-item>
          <el-descriptions-item label="资产总值(元)">{{ detail.totalValue ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="调剂事由" :span="2">{{ detail.applyReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sub-title">调剂资产明细</h4>
        <el-table :data="detail.detailList || []" size="small" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="assetCode" label="资产编号" width="190" />
          <el-table-column prop="assetName" label="资产名称" min-width="130" />
          <el-table-column prop="specModel" label="规格型号" min-width="110" />
        </el-table>

        <h4 class="sub-title">审批记录</h4>
        <el-descriptions v-if="detail.auditUserName || detail.auditTime" :column="2" border>
          <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批结果">
            {{ detail.orderStatus === 'APPROVED' ? '通过' : detail.orderStatus === 'REJECTED' ? '驳回' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无审批记录" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { applyTransfer, auditTransfer, getTransferPage, getTransferDetail, getAvailableAssets } from '@/api/gc'
import { getUnitList, getCurrentUser } from '@/api/sys'

/** 调剂单状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' },
  DONE: { text: '已完成', type: 'info' }
}

const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetIds: [],
  receiveUnitId: undefined,
  applyReason: ''
})
const selectedAssets = ref([])

const rules = {
  assetIds: [
    {
      validator: (rule, value, callback) => {
        if (!value || !value.length) callback(new Error('请选择调剂资产'))
        else callback()
      },
      trigger: 'change'
    }
  ],
  receiveUnitId: [{ required: true, message: '请选择接收单位', trigger: 'change' }],
  applyReason: [{ required: true, message: '请填写调剂事由', trigger: 'blur' }]
}

/** 接收单位（排除本单位） */
const units = ref([])
const currentUnitId = ref(null)

const loadUnits = async () => {
  try {
    units.value = (await getUnitList()) || []
  } catch (e) {
    units.value = []
  }
}

const receiveUnits = computed(() => units.value.filter((u) => u.id !== currentUnitId.value))

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await applyTransfer({
        assetIds: form.assetIds,
        receiveUnitId: form.receiveUnitId,
        applyReason: form.applyReason
      })
      ElMessage.success('调剂申请提交成功')
      resetForm()
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  formRef.value.resetFields()
  form.assetIds = []
  selectedAssets.value = []
}

const removeAsset = (index) => {
  const removed = selectedAssets.value.splice(index, 1)[0]
  form.assetIds = selectedAssets.value.map((a) => a.id)
  assetTableRef.value && assetTableRef.value.toggleRowSelection(removed, false)
}

/** 资产选择弹窗 */
const assetDialogVisible = ref(false)
const assetTableRef = ref()
const assetLoading = ref(false)
const assetOptions = ref([])
const assetSearch = ref('')
const assetQuery = reactive({ page: 1, size: 10 })

const filteredAssets = computed(() => {
  const keyword = assetSearch.value.trim().toLowerCase()
  if (!keyword) return assetOptions.value
  return assetOptions.value.filter(
    (a) => a.assetName.toLowerCase().includes(keyword) || a.assetCode.toLowerCase().includes(keyword)
  )
})

const pagedAssets = computed(() => {
  const start = (assetQuery.page - 1) * assetQuery.size
  return filteredAssets.value.slice(start, start + assetQuery.size)
})

const loadAssets = async () => {
  assetLoading.value = true
  try {
    assetOptions.value = (await getAvailableAssets()) || []
  } catch (e) {
    assetOptions.value = []
  } finally {
    assetLoading.value = false
  }
}

const openAssetDialog = async () => {
  assetDialogVisible.value = true
  assetSearch.value = ''
  assetQuery.page = 1
  if (!assetOptions.value.length) {
    await loadAssets()
  }
}

const onAssetPageChange = () => {
  // 客户端分页，仅触发重渲染
}

const onAssetSelectionChange = (rows) => {
  selectedAssets.value = rows
}

const confirmAssets = () => {
  form.assetIds = selectedAssets.value.map((a) => a.id)
  assetDialogVisible.value = false
}

/** 调剂申请列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getTransferPage({ page: query.page, size: query.size })
    list.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 审核 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({
  orderId: null,
  orderNo: '',
  auditResult: 'PASS',
  auditRemark: ''
})

const openAudit = (row) => {
  Object.assign(auditForm, {
    orderId: row.id,
    orderNo: row.orderNo,
    auditResult: 'PASS',
    auditRemark: ''
  })
  auditVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.auditResult) {
    ElMessage.warning('请选择审批结果')
    return
  }
  auditing.value = true
  try {
    await auditTransfer({
      orderId: auditForm.orderId,
      auditResult: auditForm.auditResult,
      auditRemark: auditForm.auditRemark
    })
    ElMessage.success('审批完成')
    auditVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由拦截器统一提示
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
    detail.value = await getTransferDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(async () => {
  loadUnits()
  loadList()
  try {
    const user = await getCurrentUser()
    currentUnitId.value = user.unitId
  } catch (e) {
    currentUnitId.value = null
  }
})
</script>

<style scoped>
.card-title {
  font-weight: 600;
}

.form-card {
  margin-bottom: 16px;
}

.asset-picker {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.placeholder {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

.dialog-search {
  margin-bottom: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.sub-title {
  margin: 16px 0 8px;
  font-size: 14px;
  color: #606266;
}
</style>
