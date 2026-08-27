<template>
  <div class="return-apply-page">
    <!-- 归还申请表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">归还申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="借用单号" prop="borrowOrderId">
          <el-select
            v-model="form.borrowOrderId"
            placeholder="请选择已通过的借用单"
            filterable
            style="width: 320px"
            @change="onBorrowChange"
          >
            <el-option v-for="b in borrowOrders" :key="b.id" :label="b.orderNo" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="归还资产" prop="assetIds">
          <template v-if="form.borrowOrderId">
            <div class="asset-toolbar">
              <el-button link type="primary" @click="selectAllAssets">全选</el-button>
              <el-button link type="info" @click="clearAssets">清空</el-button>
            </div>
            <el-table
              ref="assetTableRef"
              v-loading="assetLoading"
              :data="borrowAssets"
              row-key="assetId"
              size="small"
              border
              max-height="220"
              @selection-change="onAssetSelectionChange"
            >
              <el-table-column type="selection" width="45" reserve-selection />
              <el-table-column prop="assetCode" label="资产编号" width="190" show-overflow-tooltip />
              <el-table-column prop="assetName" label="资产名称" min-width="130" show-overflow-tooltip />
              <el-table-column prop="specModel" label="规格型号" min-width="110">
                <template #default="{ row }">{{ row.specModel || '-' }}</template>
              </el-table-column>
              <el-table-column prop="borrowQuantity" label="借用数量" width="80" align="center" />
            </el-table>
          </template>
          <span v-else class="placeholder">请先选择借用单</span>
        </el-form-item>
        <el-form-item label="计划归还时间" prop="planReturnTime">
          <el-date-picker
            v-model="form.planReturnTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择计划归还时间"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 归还申请列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">归还申请列表</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="returnNo" label="归还单号" width="150" show-overflow-tooltip />
        <el-table-column prop="borrowOrderNo" label="关联借用单号" width="150" show-overflow-tooltip />
        <el-table-column prop="assetCount" label="资产数量" width="90" align="center" />
        <el-table-column label="归还时间" width="160">
          <template #default="{ row }">{{ formatTime(row.planReturnTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.returnStatus)">{{ statusText(row.returnStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.returnStatus === 'PENDING'" link type="warning" @click="openAccept(row)">
              验收
            </el-button>
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
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

    <!-- 验收对话框 -->
    <el-dialog v-model="acceptVisible" title="归还验收" width="560px">
      <el-form :model="acceptForm" label-width="100px">
        <el-form-item label="归还单号">
          <span>{{ acceptForm.returnNo }}</span>
        </el-form-item>
        <el-form-item label="验收结果" required>
          <el-radio-group v-model="acceptForm.acceptResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
            <el-radio value="REPAIR">需维修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="损坏描述">
          <el-input v-model="acceptForm.damageInfo" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="责任归属">
          <el-select v-model="acceptForm.damageResponsibility" placeholder="请选择" clearable style="width: 200px">
            <el-option label="单位责任" value="UNIT" />
            <el-option label="个人责任" value="PERSONAL" />
            <el-option label="自然损耗" value="NATURAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number
            v-model="acceptForm.repairCost"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="元"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="赔偿金额">
          <el-input-number
            v-model="acceptForm.compensationAmount"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="元"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptForm.acceptRemark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="accepting" @click="submitAccept">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="归还单详情" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="归还单号">{{ detail.returnNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.returnStatus)">{{ statusText(detail.returnStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联借用单号">{{ detail.borrowOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请单位">{{ detail.applicantUnitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资产数量">{{ detail.assetCount }}</el-descriptions-item>
          <el-descriptions-item label="计划归还时间">{{ formatTime(detail.planReturnTime) }}</el-descriptions-item>
          <el-descriptions-item label="实际归还时间">{{ formatTime(detail.actualReturnTime) }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sub-title">归还资产明细</h4>
        <el-table :data="detail.detailList || []" size="small" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="assetCode" label="资产编号" width="190" />
          <el-table-column prop="assetName" label="资产名称" min-width="130" />
          <el-table-column prop="specModel" label="规格型号" min-width="110" />
          <el-table-column prop="returnQuantity" label="归还数量" width="80" align="center" />
        </el-table>

        <h4 class="sub-title">验收记录</h4>
        <el-descriptions v-if="detail.acceptResult" :column="2" border>
          <el-descriptions-item label="验收结果">{{ acceptResultText(detail.acceptResult) }}</el-descriptions-item>
          <el-descriptions-item label="验收人">{{ detail.acceptUserIds || '-' }}</el-descriptions-item>
          <el-descriptions-item label="验收时间">{{ formatTime(detail.acceptTime) }}</el-descriptions-item>
          <el-descriptions-item label="损坏责任">
            {{ responsibilityText(detail.damageResponsibility) }}
          </el-descriptions-item>
          <el-descriptions-item label="维修费用">{{ detail.repairCost ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="赔偿金额">{{ detail.compensationAmount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="损坏描述" :span="2">{{ detail.damageInfo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="验收意见" :span="2">{{ detail.acceptRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无验收记录" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBorrowPage, getBorrowedAssets, applyReturn, acceptReturn, getReturnPage, getReturnDetail } from '@/api/gc'

/** 归还单状态映射 */
const statusMap = {
  PENDING: { text: '待验收', type: 'warning' },
  ACCEPTED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' },
  REPAIRING: { text: '维修中', type: 'primary' }
}

const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const acceptResultText = (r) => ({ PASS: '通过', FAIL: '不通过', REPAIR: '需维修' }[r] || r)
const responsibilityText = (r) => ({ UNIT: '单位责任', PERSONAL: '个人责任', NATURAL: '自然损耗' }[r] || r || '-')

/** 归还申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  borrowOrderId: undefined,
  assetIds: [],
  planReturnTime: ''
})

const rules = {
  borrowOrderId: [{ required: true, message: '请选择借用单', trigger: 'change' }],
  assetIds: [
    {
      validator: (rule, value, callback) => {
        if (!value || !value.length) callback(new Error('请选择归还资产'))
        else callback()
      },
      trigger: 'change'
    }
  ],
  planReturnTime: [{ required: true, message: '请选择计划归还时间', trigger: 'change' }]
}

/** 已通过的借用单下拉 */
const borrowOrders = ref([])

const loadBorrowOrders = async () => {
  try {
    const res = await getBorrowPage({ status: 'APPROVED', page: 1, size: 100 })
    borrowOrders.value = res.data || []
  } catch (e) {
    borrowOrders.value = []
  }
}

/** 借用单资产选择 */
const assetTableRef = ref()
const assetLoading = ref(false)
const borrowAssets = ref([])

const onBorrowChange = async () => {
  form.assetIds = []
  borrowAssets.value = []
  if (!form.borrowOrderId) return
  assetLoading.value = true
  try {
    borrowAssets.value = (await getBorrowedAssets(form.borrowOrderId)) || []
    // 默认全选
    selectAllAssets()
  } catch (e) {
    borrowAssets.value = []
  } finally {
    assetLoading.value = false
  }
}

const selectAllAssets = () => {
  borrowAssets.value.forEach((row) => {
    assetTableRef.value && assetTableRef.value.toggleRowSelection(row, true)
  })
}

const clearAssets = () => {
  assetTableRef.value && assetTableRef.value.clearSelection()
}

const onAssetSelectionChange = (rows) => {
  form.assetIds = rows.map((r) => r.assetId)
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await applyReturn({
        borrowOrderId: form.borrowOrderId,
        assetIds: form.assetIds,
        planReturnTime: form.planReturnTime
      })
      ElMessage.success('归还申请提交成功')
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
  borrowAssets.value = []
  assetTableRef.value && assetTableRef.value.clearSelection()
}

/** 归还申请列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getReturnPage({ page: query.page, size: query.size })
    list.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({
  returnOrderId: null,
  returnNo: '',
  acceptResult: 'PASS',
  damageInfo: '',
  damageResponsibility: '',
  repairCost: undefined,
  compensationAmount: undefined,
  acceptRemark: ''
})

const openAccept = (row) => {
  Object.assign(acceptForm, {
    returnOrderId: row.id,
    returnNo: row.returnNo,
    acceptResult: 'PASS',
    damageInfo: '',
    damageResponsibility: '',
    repairCost: undefined,
    compensationAmount: undefined,
    acceptRemark: ''
  })
  acceptVisible.value = true
}

const submitAccept = async () => {
  if (!acceptForm.acceptResult) {
    ElMessage.warning('请选择验收结果')
    return
  }
  accepting.value = true
  try {
    await acceptReturn({
      returnOrderId: acceptForm.returnOrderId,
      acceptResult: acceptForm.acceptResult,
      damageInfo: acceptForm.damageInfo || undefined,
      damageResponsibility: acceptForm.damageResponsibility || undefined,
      repairCost: acceptForm.repairCost ?? undefined,
      compensationAmount: acceptForm.compensationAmount ?? undefined,
      acceptRemark: acceptForm.acceptRemark || undefined
    })
    ElMessage.success('验收完成')
    acceptVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由拦截器统一提示
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
    detail.value = await getReturnDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadBorrowOrders()
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

.asset-toolbar {
  margin-bottom: 8px;
}

.placeholder {
  color: #909399;
  font-size: 13px;
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
