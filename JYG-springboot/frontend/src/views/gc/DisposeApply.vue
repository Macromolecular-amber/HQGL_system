<template>
  <div class="dispose-apply-page">
    <!-- 处置申请表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">处置申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="处置资产" prop="assetIds">
          <div class="asset-picker">
            <el-button @click="openAssetDialog">选择资产</el-button>
            <span class="placeholder">从在仓资产中多选</span>
          </div>
          <el-table v-if="selectedAssets.length" :data="selectedAssets" size="small" border max-height="200">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="assetCode" label="资产编号" width="190" show-overflow-tooltip />
            <el-table-column prop="assetName" label="资产名称" min-width="130" show-overflow-tooltip />
            <el-table-column prop="specModel" label="规格型号" min-width="100">
              <template #default="{ row }">{{ row.specModel || '-' }}</template>
            </el-table-column>
            <el-table-column width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeAsset($index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="尚未选择处置资产" :image-size="60" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="处置方式" prop="disposeMethod">
              <el-select v-model="form.disposeMethod" placeholder="请选择" style="width: 100%">
                <el-option v-for="m in disposeMethods" :key="m.value" :label="m.label" :value="m.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评估机构" prop="appraisalOrg">
              <el-input v-model="form.appraisalOrg" placeholder="选填" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评估价值" prop="appraisalValue">
              <el-input-number
                v-model="form.appraisalValue"
                :min="0"
                :precision="2"
                :controls="false"
                placeholder="元"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="评估报告">
              <el-upload
                v-model:file-list="reportFileList"
                :auto-upload="false"
                :limit="1"
                accept=".pdf,.jpg,.png"
                :on-change="onReportChange"
                :on-remove="() => (form.appraisalReportUrl = '')"
              >
                <el-button>选择文件</el-button>
                <template #tip>
                  <div class="upload-tip">暂存文件名，上传接口接入后替换为真实URL</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="处置事由" prop="applyReason">
              <el-input v-model="form.applyReason" type="textarea" :rows="2" maxlength="500" placeholder="请填写处置事由" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 处置申请列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">处置申请列表</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="处置单号" width="150" show-overflow-tooltip />
        <el-table-column prop="assetCount" label="资产数量" width="90" align="center" />
        <el-table-column label="处置方式" width="100">
          <template #default="{ row }">{{ row.disposeMethodLabel || row.disposeMethod || '-' }}</template>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 'PENDING'" link type="warning" @click="openAudit(row)">
              审核
            </el-button>
            <el-button v-if="row.orderStatus === 'APPROVED'" link type="success" @click="openIncome(row)">
              录入收益
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
    <el-dialog v-model="assetDialogVisible" title="选择处置资产" width="780px">
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
        <el-table-column prop="assetName" label="资产名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="originalValue" label="原值(元)" width="100" align="right" />
        <el-table-column prop="location" label="存放地点" min-width="100">
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
    <el-dialog v-model="auditVisible" title="处置审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="处置单号">
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

    <!-- 录入收益对话框 -->
    <el-dialog v-model="incomeVisible" title="录入处置收益" width="480px">
      <el-form :model="incomeForm" :rules="incomeRules" ref="incomeFormRef" label-width="100px">
        <el-form-item label="处置单号">
          <span>{{ incomeForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="处置收入" prop="incomeAmount">
          <el-input-number
            v-model="incomeForm.incomeAmount"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="元"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="处置费用" prop="expenseAmount">
          <el-input-number
            v-model="incomeForm.expenseAmount"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="元（评估费、拍卖佣金、运输费等）"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="incomeForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="incomeVisible = false">取消</el-button>
        <el-button type="primary" :loading="recording" @click="submitIncome">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="处置单详情" width="720px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="处置单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.orderStatus)">{{ statusText(detail.orderStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请单位">{{ detail.applicantUnitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处置方式">
            {{ detail.disposeMethodLabel || detail.disposeMethod || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="资产数量">{{ detail.assetCount }}</el-descriptions-item>
          <el-descriptions-item label="资产总值(元)">{{ detail.totalValue ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="评估机构">{{ detail.appraisalOrg || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评估价值(元)">{{ detail.appraisalValue ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="评估报告">{{ detail.appraisalReportUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处置事由" :span="2">{{ detail.applyReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sub-title">处置资产明细</h4>
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
            {{ detail.orderStatus === 'APPROVED' || detail.orderStatus === 'COMPLETED' ? '通过'
              : detail.orderStatus === 'REJECTED' ? '驳回' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无审批记录" :image-size="60" />

        <h4 class="sub-title">收益记录</h4>
        <el-descriptions v-if="detail.incomeAmount !== null && detail.incomeAmount !== undefined" :column="3" border>
          <el-descriptions-item label="处置收入">{{ detail.incomeAmount }}</el-descriptions-item>
          <el-descriptions-item label="处置费用">{{ detail.expenseAmount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="净收益">{{ detail.netProfit ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ formatTime(detail.execTime) }}</el-descriptions-item>
          <el-descriptions-item label="收益备注">{{ detail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="尚未录入收益" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  applyDispose,
  auditDispose,
  recordIncome,
  getDisposePage,
  getDisposeDetail,
  getAvailableAssets
} from '@/api/gc'

/** 处置单状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' },
  COMPLETED: { text: '已完成', type: 'info' }
}

const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 处置方式 */
const disposeMethods = [
  { value: 'AUCTION', label: '拍卖' },
  { value: 'SCRAP', label: '报废' },
  { value: 'DONATE', label: '捐赠' }
]

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetIds: [],
  disposeMethod: '',
  appraisalOrg: '',
  appraisalValue: undefined,
  appraisalReportUrl: '',
  applyReason: ''
})
const selectedAssets = ref([])
const reportFileList = ref([])

const rules = {
  assetIds: [
    {
      validator: (rule, value, callback) => {
        if (!value || !value.length) callback(new Error('请选择处置资产'))
        else callback()
      },
      trigger: 'change'
    }
  ],
  disposeMethod: [{ required: true, message: '请选择处置方式', trigger: 'change' }],
  applyReason: [{ required: true, message: '请填写处置事由', trigger: 'blur' }]
}

const onReportChange = (file) => {
  // 暂存文件名作为URL占位，接入文件上传接口后替换为真实URL
  form.appraisalReportUrl = file.name
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await applyDispose({
        assetIds: form.assetIds,
        disposeMethod: form.disposeMethod,
        applyReason: form.applyReason,
        appraisalOrg: form.appraisalOrg || undefined,
        appraisalValue: form.appraisalValue ?? undefined,
        appraisalReportUrl: form.appraisalReportUrl || undefined
      })
      ElMessage.success('处置申请提交成功')
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
  form.appraisalReportUrl = ''
  selectedAssets.value = []
  reportFileList.value = []
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

const onAssetPageChange = () => {}

const onAssetSelectionChange = (rows) => {
  selectedAssets.value = rows
}

const confirmAssets = () => {
  form.assetIds = selectedAssets.value.map((a) => a.id)
  assetDialogVisible.value = false
}

/** 处置申请列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getDisposePage({ page: query.page, size: query.size })
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
    await auditDispose({
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

/** 录入收益 */
const incomeVisible = ref(false)
const recording = ref(false)
const incomeFormRef = ref()
const incomeForm = reactive({
  orderId: null,
  orderNo: '',
  incomeAmount: undefined,
  expenseAmount: undefined,
  remark: ''
})

const incomeRules = {
  incomeAmount: [{ required: true, message: '请输入处置收入', trigger: 'blur' }],
  expenseAmount: [{ required: true, message: '请输入处置费用', trigger: 'blur' }]
}

const openIncome = (row) => {
  Object.assign(incomeForm, {
    orderId: row.id,
    orderNo: row.orderNo,
    incomeAmount: undefined,
    expenseAmount: undefined,
    remark: ''
  })
  incomeVisible.value = true
}

const submitIncome = () => {
  incomeFormRef.value.validate(async (valid) => {
    if (!valid) return
    recording.value = true
    try {
      await recordIncome({
        orderId: incomeForm.orderId,
        incomeAmount: incomeForm.incomeAmount,
        expenseAmount: incomeForm.expenseAmount,
        remark: incomeForm.remark || undefined
      })
      ElMessage.success('收益录入成功')
      incomeVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      recording.value = false
    }
  })
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getDisposeDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadList()
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

.upload-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
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
