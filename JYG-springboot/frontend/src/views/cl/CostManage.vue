<template>
  <div class="cost-manage-page">
    <!-- 查询条件区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="车辆">
          <el-select v-model="query.vehicleId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="v in vehicles" :key="v.id" :label="v.plateNumber" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型">
          <el-select v-model="query.costType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in costTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="query.approvalStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="发生时间">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain v-hasRole="['BIZ_ADMIN','WAREHOUSE','DRIVER']" @click="openAdd">新增费用</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 费用列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="plateNumber" label="车辆" width="110" show-overflow-tooltip />
        <el-table-column label="费用类型" width="90">
          <template #default="{ row }">{{ row.costTypeLabel || row.costType }}</template>
        </el-table-column>
        <el-table-column prop="costAmount" label="金额(元)" width="110" align="right" />
        <el-table-column label="发生时间" width="160">
          <template #default="{ row }">{{ formatTime(row.costTime) }}</template>
        </el-table-column>
        <el-table-column prop="costDesc" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="bizOrderNo" label="单据号" width="130" show-overflow-tooltip />
        <el-table-column label="审批状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.approvalStatus)">
              {{ row.approvalStatusLabel || row.approvalStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.approvalStatus === 'PENDING'" link type="warning" v-hasRole="['BIZ_ADMIN','DIRECTOR']" @click="openAudit(row)">
              审核
            </el-button>
            <el-button v-if="row.approvalStatus === 'PENDING'" link type="primary" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DRIVER']" @click="openEdit(row)">
              编辑
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑费用' : '新增费用'" width="640px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车辆" prop="vehicleId">
              <el-select v-model="form.vehicleId" placeholder="请选择车辆" filterable style="width: 100%">
                <el-option v-for="v in vehicles" :key="v.id" :label="`${v.plateNumber}（${v.vehicleTypeLabel || ''}）`" :value="v.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用类型" prop="costType">
              <el-select v-model="form.costType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in costTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额(元)" prop="costAmount">
              <el-input-number
                v-model="form.costAmount"
                :min="0"
                :precision="2"
                :controls="false"
                placeholder="请输入金额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发生时间" prop="costTime">
              <el-date-picker
                v-model="form.costTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="请选择"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="form.costType === 'FUEL'" :span="12">
            <el-form-item label="加油里程(km)" prop="currentMileage">
              <el-input-number v-model="form.currentMileage" :min="0" :precision="1" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col v-if="form.costType === 'FUEL'" :span="12">
            <el-form-item label="加油量(L)" prop="fuelQuantity">
              <el-input-number v-model="form.fuelQuantity" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input v-model="form.costDesc" type="textarea" :rows="2" maxlength="500" placeholder="费用说明" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="单据号">
              <el-input v-model="form.bizOrderNo" maxlength="50" placeholder="关联单据号，选填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="附件">
              <el-upload
                v-model:file-list="attachmentList"
                :auto-upload="false"
                :limit="5"
                multiple
              >
                <el-button>选择文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">仅暂存文件名，不实际上传</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="费用审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="车辆">
          <span>{{ auditForm.plateNumber }}</span>
        </el-form-item>
        <el-form-item label="费用金额">
          <span>{{ auditForm.costAmount }} 元</span>
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
    <el-dialog v-model="detailVisible" title="费用详情" width="680px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="车辆">{{ detail.plateNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ detail.vehicleTypeLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="费用类型">{{ detail.costTypeLabel || detail.costType }}</el-descriptions-item>
        <el-descriptions-item label="金额(元)">{{ detail.costAmount }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ formatTime(detail.costTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="statusType(detail.approvalStatus)">
            {{ detail.approvalStatusLabel || detail.approvalStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="加油里程(km)">{{ detail.currentMileage ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="加油量(L)">{{ detail.fuelQuantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="单据号">{{ detail.bizOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.bizType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.costDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="附件" :span="2">{{ detail.attachmentUrls || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.approvalUserId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(detail.approvalTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ detail.approvalRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { saveCost, updateCost, auditCost, getCostPage, getCostDetail, getVehiclePage } from '@/api/cl'

/** 费用类型 */
const costTypes = [
  { value: 'FUEL', label: '加油' },
  { value: 'REPAIR', label: '维修' },
  { value: 'INSURANCE', label: '保险' },
  { value: 'TOLL', label: '过路费' },
  { value: 'ETC', label: 'ETC' },
  { value: 'PARKING', label: '停车' },
  { value: 'OTHER', label: '其他' }
]

/** 审批状态映射 */
const statusOptions = [
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' }
]
const statusTypeMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger'
}
const statusType = (s) => statusTypeMap[s] || 'info'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

const route = useRoute()

/** 车辆下拉 */
const vehicles = ref([])

const loadVehicles = async () => {
  try {
    const res = await getVehiclePage({ page: 1, size: 100 })
    vehicles.value = res.data || []
  } catch (e) {
    vehicles.value = []
  }
}

/** 查询条件 */
const query = reactive({ vehicleId: undefined, costType: '', approvalStatus: '', page: 1, size: 10 })
const timeRange = ref([])

const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getCostPage({
      vehicleId: query.vehicleId ?? undefined,
      costType: query.costType || undefined,
      approvalStatus: query.approvalStatus || undefined,
      startTime: timeRange.value && timeRange.value.length ? timeRange.value[0] : undefined,
      endTime: timeRange.value && timeRange.value.length ? timeRange.value[1] : undefined,
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
  query.vehicleId = undefined
  query.costType = ''
  query.approvalStatus = ''
  timeRange.value = []
  query.page = 1
  loadList()
}

/** 新增/编辑表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const attachmentList = ref([])
const form = reactive({
  id: null,
  vehicleId: null,
  costType: '',
  costAmount: undefined,
  costTime: '',
  costDesc: '',
  bizOrderNo: '',
  currentMileage: undefined,
  fuelQuantity: undefined
})

const rules = {
  vehicleId: [{ required: true, message: '请选择车辆', trigger: 'change' }],
  costType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  costAmount: [{ required: true, message: '请输入费用金额', trigger: 'blur' }],
  costTime: [{ required: true, message: '请选择发生时间', trigger: 'change' }],
  currentMileage: [{ required: true, message: '请输入加油里程', trigger: 'blur' }],
  fuelQuantity: [{ required: true, message: '请输入加油量', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    vehicleId: null,
    costType: '',
    costAmount: undefined,
    costTime: '',
    costDesc: '',
    bizOrderNo: '',
    currentMileage: undefined,
    fuelQuantity: undefined
  })
  attachmentList.value = []
}

const openAdd = () => {
  resetForm()
  formRef.value && formRef.value.clearValidate()
  formVisible.value = true
}

const openEdit = async (row) => {
  resetForm()
  try {
    const d = await getCostDetail(row.id)
    Object.assign(form, {
      id: d.id,
      vehicleId: d.vehicleId,
      costType: d.costType,
      costAmount: d.costAmount,
      costTime: d.costTime ? String(d.costTime).slice(0, 19) : '',
      costDesc: d.costDesc,
      bizOrderNo: d.bizOrderNo,
      currentMileage: d.currentMileage,
      fuelQuantity: d.fuelQuantity
    })
    attachmentList.value = d.attachmentUrls
      ? String(d.attachmentUrls).split(',').map((n, i) => ({ name: n, url: '' , uid: i }))
      : []
  } catch (e) {
    return
  }
  formVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        vehicleId: form.vehicleId,
        costType: form.costType,
        costAmount: form.costAmount,
        costTime: form.costTime,
        costDesc: form.costDesc || undefined,
        bizOrderNo: form.bizOrderNo || undefined,
        attachmentUrls: attachmentList.value.map((f) => f.name),
        currentMileage: form.costType === 'FUEL' ? form.currentMileage : undefined,
        fuelQuantity: form.costType === 'FUEL' ? form.fuelQuantity : undefined
      }
      if (form.id) {
        await updateCost(form.id, payload)
        ElMessage.success('编辑成功')
      } else {
        await saveCost(payload)
        ElMessage.success('费用登记成功')
      }
      formVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      saving.value = false
    }
  })
}

/** 审核 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({
  costId: null,
  plateNumber: '',
  costAmount: null,
  auditResult: 'PASS',
  auditRemark: ''
})

const openAudit = (row) => {
  Object.assign(auditForm, {
    costId: row.id,
    plateNumber: row.plateNumber,
    costAmount: row.costAmount,
    auditResult: 'PASS',
    auditRemark: ''
  })
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await auditCost({
      costId: auditForm.costId,
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
    detail.value = await getCostDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(async () => {
  await loadVehicles()
  // 从台账页跳转时携带车辆过滤条件
  if (route.query.vehicleId) {
    query.vehicleId = Number(route.query.vehicleId)
  }
  loadList()
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
</style>
