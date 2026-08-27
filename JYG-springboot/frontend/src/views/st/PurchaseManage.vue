<template>
  <div class="purchase-manage-page">
    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="采购单号">
          <el-input v-model="query.orderNo" placeholder="单号模糊查询" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.orderStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="query.supplierName" placeholder="供应商模糊查询" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openApply">新增采购</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 采购单列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="orderNo" label="采购单号" width="140" show-overflow-tooltip />
        <el-table-column prop="supplierName" label="供应商" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.supplierName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="materialCount" label="物资种类" width="90" align="center" />
        <el-table-column label="总金额" width="120" align="right">
          <template #default="{ row }">￥{{ formatAmount(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="有效期" min-width="240">
          <template #default="{ row }">{{ formatTime(row.effectiveStart) }} ~ {{ formatTime(row.effectiveEnd) }}</template>
        </el-table-column>
        <el-table-column label="是否过期" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isExpired" type="danger" size="small">已过期</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)">{{ row.statusLabel || row.orderStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="isDraft(row)" v-hasRole="['BIZ_ADMIN']" link type="warning" @click="openAudit(row)">审核</el-button>
            <el-button v-if="isApproved(row)" v-hasRole="['BIZ_ADMIN']" link type="success" @click="openAccept(row)">验收</el-button>
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

    <!-- 新增采购对话框 -->
    <el-dialog v-model="applyVisible" title="新增采购" width="760px" :close-on-click-modal="false">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="90px">
        <el-form-item label="采购事由" prop="purchaseReason">
          <el-input v-model="applyForm.purchaseReason" type="textarea" :rows="2" maxlength="500" placeholder="请说明采购用途" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="applyForm.supplierName" maxlength="100" placeholder="手动录入供应商名称" style="width: 100%" />
        </el-form-item>
        <el-form-item label="物资明细">
          <el-table :data="applyForm.items" border size="small">
            <el-table-column label="物资" min-width="200">
              <template #default="{ row }">
                <el-select v-model="row.materialId" placeholder="请选择物资" filterable style="width: 100%">
                  <el-option v-for="m in materials" :key="m.id" :label="`${m.materialName}（${m.spec || '-'}）`" :value="m.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="0.01" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="110" align="right">
              <template #default="{ row }">￥{{ formatAmount(subtotal(row)) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeItem($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="item-actions">
            <el-button type="primary" plain size="small" @click="addItem">+ 添加物资</el-button>
            <span class="total-amount">合计：￥{{ formatAmount(applyTotal) }}</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="采购单审核" width="500px" :close-on-click-modal="false">
      <el-descriptions :column="1" border size="small" class="audit-info">
        <el-descriptions-item label="采购单号">{{ auditForm.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ auditForm.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">￥{{ formatAmount(auditForm.totalAmount) }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px" class="audit-form">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" maxlength="500" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 验收对话框 -->
    <el-dialog v-model="acceptVisible" title="采购单验收" width="620px" :close-on-click-modal="false">
      <el-descriptions :column="1" border size="small" class="audit-info">
        <el-descriptions-item label="采购单号">{{ acceptForm.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ acceptForm.supplierName || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px" class="audit-form">
        <el-form-item label="验收结果">
          <el-radio-group v-model="acceptForm.acceptResult">
            <el-radio value="PASS">通过（入库）</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="acceptForm.acceptResult === 'PASS'" label="实收明细">
          <el-table :data="acceptForm.receivedItems" border size="small">
            <el-table-column prop="materialName" label="物资名称" min-width="140" />
            <el-table-column prop="materialCode" label="编码" width="130" show-overflow-tooltip />
            <el-table-column label="计划数量" width="100" align="right">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column label="实收数量" width="130">
              <template #default="{ row }">
                <el-input-number v-model="row.receivedQuantity" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptForm.acceptRemark" type="textarea" :rows="2" maxlength="500" placeholder="请输入验收意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="accepting" @click="submitAccept">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="采购单详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="采购单号">{{ detail.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.orderStatus)">{{ detail.statusLabel || detail.orderStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detail.supplierName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物资种类">{{ detail.materialCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="总金额">￥{{ formatAmount(detail.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="是否过期">
          <el-tag v-if="detail.isExpired" type="danger" size="small">已过期</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="采购事由" :span="2">{{ detail.purchaseReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生效时间">{{ formatTime(detail.effectiveStart) }}</el-descriptions-item>
        <el-descriptions-item label="失效时间">{{ formatTime(detail.effectiveEnd) }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收人">{{ detail.acceptUsers || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收时间">{{ formatTime(detail.acceptTime) }}</el-descriptions-item>
        <el-descriptions-item label="验收结果">
          {{ acceptStatusLabel(detail.acceptStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="验收意见">{{ detail.acceptRemark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="sub-title">物资明细</h4>
      <el-table v-if="detail" :data="detail.items || []" size="small" border>
        <el-table-column prop="materialCode" label="物资编码" width="130" show-overflow-tooltip />
        <el-table-column prop="materialName" label="物资名称" min-width="130" />
        <el-table-column prop="spec" label="规格" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.spec || '-' }}</template>
        </el-table-column>
        <el-table-column label="数量" width="100" align="right">
          <template #default="{ row }">{{ row.quantity }}</template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">￥{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="110" align="right">
          <template #default="{ row }">￥{{ formatAmount(row.subtotal) }}</template>
        </el-table-column>
        <el-table-column label="实收数量" width="100" align="right">
          <template #default="{ row }">{{ row.receivedQuantity ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="收货时间" width="150">
          <template #default="{ row }">{{ formatTime(row.receiveTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMaterialPage, applyPurchase, auditPurchase, acceptPurchase, getPurchasePage, getPurchaseDetail } from '@/api/st'

/** 状态下拉（与后端状态枚举一致） */
const statusOptions = [
  { value: 'DRAFT', label: '草稿' },
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'EXPIRED', label: '已过期' }
]
const statusTagTypeMap = {
  DRAFT: 'info',
  PENDING: 'warning',
  APPROVED: 'primary',
  COMPLETED: 'success',
  REJECTED: 'danger',
  RECEIVED: 'success',
  EXPIRED: 'danger'
}
const statusTagType = (s) => statusTagTypeMap[String(s || '').toUpperCase()] || 'info'
const isDraft = (row) => String(row.orderStatus || '').toUpperCase() === 'DRAFT'
const isApproved = (row) => String(row.orderStatus || '').toUpperCase() === 'APPROVED'
const acceptStatusLabel = (s) => {
  if (!s) return '-'
  return String(s).toUpperCase() === 'PASS' ? '通过' : String(s).toUpperCase() === 'FAIL' ? '不通过' : s
}

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))

/** 查询 */
const query = reactive({
  orderNo: '',
  orderStatus: '',
  supplierName: '',
  page: 1,
  size: 10
})
const dateRange = ref([])
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getPurchasePage({
      orderNo: query.orderNo || undefined,
      orderStatus: query.orderStatus || undefined,
      supplierName: query.supplierName || undefined,
      startDate: dateRange.value && dateRange.value.length === 2 ? dateRange.value[0] : undefined,
      endDate: dateRange.value && dateRange.value.length === 2 ? dateRange.value[1] : undefined,
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

const handleReset = () => {
  query.orderNo = ''
  query.orderStatus = ''
  query.supplierName = ''
  dateRange.value = []
  query.page = 1
  loadList()
}

/** 物资下拉数据 */
const materials = ref([])
const loadMaterials = async () => {
  try {
    const res = await getMaterialPage({ page: 1, size: 200 })
    materials.value = res.data || []
  } catch (e) {
    materials.value = []
  }
}

/** 新增采购 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({
  purchaseReason: '',
  supplierName: '',
  items: []
})

const applyRules = {
  purchaseReason: [{ required: true, message: '请填写采购事由', trigger: 'blur' }]
}

const subtotal = (row) => (Number(row.quantity || 0) * Number(row.unitPrice || 0))
const applyTotal = computed(() => applyForm.items.reduce((sum, row) => sum + subtotal(row), 0))

const addItem = () => {
  applyForm.items.push({ materialId: null, quantity: 1, unitPrice: undefined })
}

const removeItem = (index) => {
  applyForm.items.splice(index, 1)
}

const openApply = () => {
  applyForm.purchaseReason = ''
  applyForm.supplierName = ''
  applyForm.items = []
  addItem()
  applyFormRef.value && applyFormRef.value.clearValidate()
  applyVisible.value = true
}

const submitApply = () => {
  applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!applyForm.items.length) {
      ElMessage.warning('请至少添加一项物资')
      return
    }
    const invalid = applyForm.items.some((i) => !i.materialId || i.quantity == null || i.unitPrice == null)
    if (invalid) {
      ElMessage.warning('请完整填写物资明细（物资、数量、单价）')
      return
    }
    applying.value = true
    try {
      await applyPurchase({
        purchaseReason: applyForm.purchaseReason,
        items: applyForm.items.map((i) => ({
          materialId: i.materialId,
          quantity: i.quantity,
          unitPrice: i.unitPrice
        })),
        supplierName: applyForm.supplierName || undefined
      })
      ElMessage.success('采购申请提交成功')
      applyVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      applying.value = false
    }
  })
}

/** 审核 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({
  orderId: null,
  orderNo: '',
  supplierName: '',
  totalAmount: null,
  auditResult: 'PASS',
  auditRemark: ''
})

const openAudit = (row) => {
  Object.assign(auditForm, {
    orderId: row.id,
    orderNo: row.orderNo,
    supplierName: row.supplierName,
    totalAmount: row.totalAmount,
    auditResult: 'PASS',
    auditRemark: ''
  })
  auditVisible.value = true
}

const submitAudit = () => {
  auditing.value = true
  try {
    auditPurchase({
      orderId: auditForm.orderId,
      auditResult: auditForm.auditResult,
      auditRemark: auditForm.auditRemark || undefined
    }).then(() => {
      ElMessage.success('审核完成')
      auditVisible.value = false
      loadList()
    }).finally(() => {
      auditing.value = false
    })
  } catch (e) {
    auditing.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({
  orderId: null,
  orderNo: '',
  supplierName: '',
  acceptResult: 'PASS',
  receivedItems: [],
  acceptRemark: ''
})

const openAccept = async (row) => {
  Object.assign(acceptForm, {
    orderId: row.id,
    orderNo: row.orderNo,
    supplierName: row.supplierName,
    acceptResult: 'PASS',
    receivedItems: [],
    acceptRemark: ''
  })
  try {
    const detail = await getPurchaseDetail(row.id)
    acceptForm.receivedItems = (detail.items || []).map((it) => ({
      detailId: it.id,
      materialName: it.materialName,
      materialCode: it.materialCode,
      quantity: it.quantity,
      receivedQuantity: it.quantity
    }))
  } catch (e) {
    // 错误已由拦截器统一提示
  }
  acceptVisible.value = true
}

const submitAccept = () => {
  accepting.value = true
  try {
    acceptPurchase({
      orderId: acceptForm.orderId,
      acceptResult: acceptForm.acceptResult,
      acceptRemark: acceptForm.acceptRemark || undefined,
      receivedItems: acceptForm.acceptResult === 'PASS'
        ? acceptForm.receivedItems.map((it) => ({
            detailId: it.detailId,
            receivedQuantity: it.receivedQuantity
          }))
        : undefined
    }).then(() => {
      ElMessage.success('验收完成')
      acceptVisible.value = false
      loadList()
    }).finally(() => {
      accepting.value = false
    })
  } catch (e) {
    accepting.value = false
  }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getPurchaseDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadList()
  loadMaterials()
})
</script>

<style scoped>
.query-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.item-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.total-amount {
  font-size: 13px;
  color: #606266;
  font-weight: 600;
}

.audit-info {
  margin-bottom: 16px;
}

.audit-form {
  margin-top: 8px;
}

.sub-title {
  margin: 16px 0 8px;
  font-size: 14px;
  color: #606266;
}
</style>
