<template>
  <div class="apply-manage-page">
    <!-- 用车申请表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">用车申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用车事由" prop="purpose">
              <el-input v-model="form.purpose" type="textarea" :rows="2" maxlength="200" placeholder="请填写用车事由" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目的地" prop="destination">
              <el-input v-model="form.destination" maxlength="200" placeholder="请填写目的地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                placeholder="请选择结束时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乘车人数" prop="passengerCount">
              <el-input-number v-model="form.passengerCount" :min="1" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所需车型" prop="requiredVehicleType">
              <el-select v-model="form.requiredVehicleType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in vehicleTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="onSubmit">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card shadow="never">
      <template #header>
        <span class="card-title">用车申请列表</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="applyNo" label="申请编号" width="150" show-overflow-tooltip />
        <el-table-column prop="purpose" label="用车事由" min-width="150" show-overflow-tooltip />
        <el-table-column prop="destination" label="目的地" min-width="110" show-overflow-tooltip />
        <el-table-column label="用车时间" min-width="230">
          <template #default="{ row }">{{ formatPeriod(row.startTime, row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="passengerCount" label="人数" width="60" align="center" />
        <el-table-column label="所需车型" width="90">
          <template #default="{ row }">{{ row.vehicleTypeLabel || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.applyStatus)">{{ row.statusLabel || row.applyStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.applyStatus === 'PENDING'" link type="danger" @click="handleCancel(row)">
              取消
            </el-button>
            <el-button v-if="row.applyStatus === 'PENDING'" link type="warning" v-hasRole="['BIZ_ADMIN','DIRECTOR']" @click="openAudit(row)">
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

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="用车申请审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="申请编号">
          <span>{{ auditForm.applyNo }}</span>
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
    <el-dialog v-model="detailVisible" title="用车申请详情" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请编号">{{ detail.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.applyStatus)">{{ detail.statusLabel || detail.applyStatus }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请单位">{{ detail.applicantUnitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用车事由" :span="2">{{ detail.purpose || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目的地">{{ detail.destination || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所需车型">
            {{ detail.vehicleTypeLabel || detail.requiredVehicleType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="乘车人数">{{ detail.passengerCount }}</el-descriptions-item>
          <el-descriptions-item label="用车时间" :span="2">
            {{ formatPeriod(detail.startTime, detail.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="自动审批">{{ detail.autoApprove ? '是' : '否' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sub-title">可调度车辆</h4>
        <el-table :data="detail.availableVehicles || []" size="small" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="plateNumber" label="车牌号" width="120" />
          <el-table-column prop="brandModel" label="品牌型号" min-width="140" />
          <el-table-column prop="vehicleTypeLabel" label="车型" width="90" />
        </el-table>
        <el-empty v-if="!(detail.availableVehicles || []).length" description="暂无可用车辆" :image-size="60" />

        <h4 class="sub-title">审批记录</h4>
        <el-descriptions v-if="detail.auditUserName || detail.auditTime" :column="2" border>
          <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批结果">
            {{ detail.applyStatus === 'APPROVED' ? '通过'
              : detail.applyStatus === 'REJECTED' ? '驳回' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无审批记录" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyCar, auditApply, getApplyPage, getApplyDetail, cancelApply } from '@/api/cl'

/** 车辆类型 */
const vehicleTypes = [
  { value: 'SEDAN', label: '轿车' },
  { value: 'SUV', label: 'SUV' },
  { value: 'MPV', label: 'MPV' },
  { value: 'BUS', label: '客车' }
]

/** 状态映射 */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' },
  CANCELLED: { text: '已取消', type: 'info' },
  DONE: { text: '已完成', type: 'info' }
}

const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatPeriod = (start, end) => `${formatTime(start)} ~ ${formatTime(end)}`

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  purpose: '',
  destination: '',
  startTime: '',
  endTime: '',
  passengerCount: undefined,
  requiredVehicleType: '',
  remark: ''
})

const rules = {
  purpose: [{ required: true, message: '请填写用车事由', trigger: 'blur' }],
  destination: [{ required: true, message: '请填写目的地', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  passengerCount: [{ required: true, message: '请输入乘车人数', trigger: 'blur' }],
  requiredVehicleType: [{ required: true, message: '请选择所需车型', trigger: 'change' }]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.startTime >= form.endTime) {
      ElMessage.warning('开始时间必须早于结束时间')
      return
    }
    submitting.value = true
    try {
      await applyCar({
        purpose: form.purpose,
        destination: form.destination,
        startTime: form.startTime,
        endTime: form.endTime,
        passengerCount: form.passengerCount,
        requiredVehicleType: form.requiredVehicleType,
        remark: form.remark || undefined
      })
      ElMessage.success('用车申请提交成功')
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
}

/** 申请列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getApplyPage({ page: query.page, size: query.size })
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
  applyId: null,
  applyNo: '',
  auditResult: 'PASS',
  auditRemark: ''
})

const openAudit = (row) => {
  Object.assign(auditForm, {
    applyId: row.id,
    applyNo: row.applyNo,
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
    await auditApply({
      applyId: auditForm.applyId,
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

/** 取消 */
const handleCancel = (row) => {
  ElMessageBox.confirm(`确认取消申请「${row.applyNo}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async () => {
      await cancelApply(row.id)
      ElMessage.success('已取消')
      loadList()
    })
    .catch(() => {})
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getApplyDetail(row.id)
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
