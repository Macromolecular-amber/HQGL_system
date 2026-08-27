<template>
  <div class="repair-manage-page">
    <!-- 查询条件区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="房间">
          <el-select v-model="query.roomId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="r in rooms" :key="r.id" :label="`${r.roomNo}（${r.building}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="维修类型">
          <el-select v-model="query.repairType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in repairTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="费用类型">
          <el-select v-model="query.costType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="c in costTypes" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.orderStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain @click="openApply">新增维修</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 维修单列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="repairNo" label="维修单号" width="150" show-overflow-tooltip />
        <el-table-column prop="roomNo" label="房间号" width="90" show-overflow-tooltip />
        <el-table-column label="维修类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.repairType === 'MAINTENANCE' ? 'info' : 'primary'">
              {{ row.repairTypeLabel || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="费用类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.costType === 'PERSONAL' ? 'warning' : 'success'">
              {{ row.costTypeLabel || row.costType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预估/实际费用" width="130" align="right">
          <template #default="{ row }">
            {{ row.estimatedCost ?? '-' }} / {{ row.actualCost ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.orderStatus)">
              {{ row.statusLabel || row.orderStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 'PENDING'" link type="warning" @click="openAudit(row)">
              审核
            </el-button>
            <el-button v-if="row.orderStatus === 'APPROVED'" link type="primary" @click="openStart(row)">
              开始维修
            </el-button>
            <el-button v-if="row.orderStatus === 'REPAIRING'" link type="success" @click="openAccept(row)">
              验收
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

    <!-- 新增维修对话框 -->
    <el-dialog v-model="applyVisible" title="新增维修" width="600px" :close-on-click-modal="false">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间" prop="roomId">
              <el-select v-model="applyForm.roomId" placeholder="请选择房间" filterable style="width: 100%">
                <el-option v-for="r in rooms" :key="r.id" :label="`${r.roomNo}（${r.building}）`" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="维修类型" prop="repairType">
              <el-select v-model="applyForm.repairType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in repairTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="费用类型" prop="costType">
              <el-select v-model="applyForm.costType" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in costTypes" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="紧急程度">
              <el-select v-model="applyForm.urgencyLevel" placeholder="选填" clearable style="width: 100%">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障描述" prop="faultDesc">
              <el-input v-model="applyForm.faultDesc" type="textarea" :rows="3" maxlength="500" placeholder="请描述故障情况" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="故障照片">
              <el-upload v-model:file-list="faultPhotoList" :auto-upload="false" :limit="5" multiple>
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
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" title="维修审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="维修单号">
          <span>{{ auditForm.repairNo }}</span>
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

    <!-- 开始维修对话框 -->
    <el-dialog v-model="startVisible" title="开始维修" width="480px" :close-on-click-modal="false">
      <el-form ref="startFormRef" label-width="90px">
        <el-form-item label="维修单号">
          <span>{{ startForm.repairNo }}</span>
        </el-form-item>
        <el-form-item label="预估费用">
          <el-input-number v-model="startForm.estimatedCost" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startVisible = false">取消</el-button>
        <el-button type="primary" :loading="starting" @click="submitStart">确定</el-button>
      </template>
    </el-dialog>

    <!-- 验收对话框 -->
    <el-dialog v-model="acceptVisible" title="维修验收" width="640px" :close-on-click-modal="false">
      <el-form ref="acceptFormRef" :model="acceptForm" :rules="acceptRules" label-width="90px">
        <el-form-item label="维修单号">
          <span>{{ acceptForm.repairNo }}</span>
        </el-form-item>
        <el-form-item label="验收结果" prop="acceptResult">
          <el-radio-group v-model="acceptForm.acceptResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="acceptForm.acceptResult === 'PASS'" label="实际费用" prop="actualCost">
          <el-input-number v-model="acceptForm.actualCost" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="acceptForm.acceptResult === 'PASS'" label="工时费">
          <el-input-number v-model="acceptForm.laborCost" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="acceptForm.acceptResult === 'PASS'" label="配件明细">
          <div class="parts-wrap">
            <div v-for="(p, i) in acceptForm.partsDetail" :key="i" class="parts-row">
              <el-input v-model="p.name" placeholder="配件名称" style="width: 170px" />
              <el-input-number v-model="p.quantity" :min="1" :controls="false" placeholder="数量" style="width: 90px" />
              <el-input-number v-model="p.price" :min="0" :precision="2" :controls="false" placeholder="单价" style="width: 110px" />
              <el-button link type="danger" :disabled="acceptForm.partsDetail.length <= 1" @click="removePart(i)">删除</el-button>
            </div>
            <el-button link type="primary" @click="addPart">+ 添加配件</el-button>
          </div>
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptForm.acceptRemark" type="textarea" :rows="2" maxlength="500" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="accepting" @click="submitAccept">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="维修单详情" width="720px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="维修单号">{{ detail.repairNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(detail.orderStatus)">
              {{ detail.statusLabel || detail.orderStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="房间号">{{ detail.roomNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="楼栋">{{ detail.building || '-' }}</el-descriptions-item>
          <el-descriptions-item label="维修类型">
            {{ detail.repairTypeLabel || detail.repairType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="费用类型">
            {{ detail.costTypeLabel || detail.costType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急程度">{{ urgencyLabel(detail.urgencyLevel) }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预估费用">{{ detail.estimatedCost ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际费用">{{ detail.actualCost ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="工时费">{{ detail.laborCost ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始维修时间">{{ formatTime(detail.repairStart) }}</el-descriptions-item>
          <el-descriptions-item label="维修结束时间">{{ formatTime(detail.repairEnd) }}</el-descriptions-item>
          <el-descriptions-item label="故障描述" :span="2">{{ detail.faultDesc || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="验收人">{{ detail.acceptUserId ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="验收时间">{{ formatTime(detail.acceptTime) }}</el-descriptions-item>
          <el-descriptions-item label="验收意见" :span="2">{{ detail.acceptRemark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sub-title">配件明细</h4>
        <el-table :data="detail.partsDetail || []" size="small" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="name" label="配件名称" min-width="140" />
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="price" label="单价" width="100" align="right" />
          <el-table-column label="小计" width="110" align="right">
            <template #default="{ row }">{{ ((row.quantity || 0) * (row.price || 0)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!(detail.partsDetail || []).length" description="暂无配件明细" :image-size="60" />
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoomPage } from '@/api/gy'
import { applyRepair, auditRepair, startRepair, acceptRepair, getRepairPage, getRepairDetail } from '@/api/gy'

/** 维修类型 */
const repairTypes = [
  { value: 'MAINTENANCE', label: '保养' },
  { value: 'REPAIR', label: '维修' }
]

/** 费用类型 */
const costTypes = [
  { value: 'UNIT', label: '单位承担' },
  { value: 'PERSONAL', label: '个人自费' }
]

/** 状态映射 */
const statusOptions = [
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已批准' },
  { value: 'REPAIRING', label: '维修中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' }
]
const statusTypeMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  REPAIRING: 'primary',
  COMPLETED: 'success',
  REJECTED: 'danger',
  DONE: 'success'
}
const statusType = (s) => statusTypeMap[s] || 'info'
const urgencyMap = { HIGH: '高', MEDIUM: '中', LOW: '低' }
const urgencyLabel = (u) => (u ? urgencyMap[u] || u : '-')
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 房间下拉 */
const rooms = ref([])

const loadRooms = async () => {
  try {
    const res = await getRoomPage({ page: 1, size: 100 })
    rooms.value = res.data || []
  } catch (e) {
    rooms.value = []
  }
}

/** 查询 */
const query = reactive({ roomId: undefined, repairType: '', costType: '', orderStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getRepairPage({
      roomId: query.roomId ?? undefined,
      repairType: query.repairType || undefined,
      costType: query.costType || undefined,
      orderStatus: query.orderStatus || undefined,
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
  query.roomId = undefined
  query.repairType = ''
  query.costType = ''
  query.orderStatus = ''
  query.page = 1
  loadList()
}

/** 新增维修 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const faultPhotoList = ref([])
const applyForm = reactive({
  roomId: null,
  repairType: '',
  costType: '',
  urgencyLevel: '',
  faultDesc: ''
})

const applyRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  repairType: [{ required: true, message: '请选择维修类型', trigger: 'change' }],
  costType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
  faultDesc: [{ required: true, message: '请填写故障描述', trigger: 'blur' }]
}

const openApply = () => {
  Object.assign(applyForm, { roomId: null, repairType: '', costType: '', urgencyLevel: '', faultDesc: '' })
  faultPhotoList.value = []
  applyFormRef.value && applyFormRef.value.clearValidate()
  applyVisible.value = true
}

const submitApply = () => {
  applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    applying.value = true
    try {
      await applyRepair({
        roomId: applyForm.roomId,
        repairType: applyForm.repairType,
        faultDesc: applyForm.faultDesc,
        faultPhotos: faultPhotoList.value.map((f) => f.name),
        urgencyLevel: applyForm.urgencyLevel || undefined,
        costType: applyForm.costType
      })
      ElMessage.success('维修申请提交成功')
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
const auditForm = reactive({ repairId: null, repairNo: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  Object.assign(auditForm, { repairId: row.id, repairNo: row.repairNo, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await auditRepair({
      repairId: auditForm.repairId,
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

/** 开始维修 */
const startVisible = ref(false)
const starting = ref(false)
const startFormRef = ref()
const startForm = reactive({ repairId: null, repairNo: '', estimatedCost: undefined })

const openStart = (row) => {
  Object.assign(startForm, { repairId: row.id, repairNo: row.repairNo, estimatedCost: undefined })
  startVisible.value = true
}

const submitStart = async () => {
  starting.value = true
  try {
    await startRepair({
      repairId: startForm.repairId,
      estimatedCost: startForm.estimatedCost ?? undefined
    })
    ElMessage.success('已开始维修')
    startVisible.value = false
    loadList()
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    starting.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptFormRef = ref()
const acceptForm = reactive({
  repairId: null,
  repairNo: '',
  acceptResult: 'PASS',
  actualCost: undefined,
  laborCost: undefined,
  partsDetail: [{ name: '', quantity: 1, price: undefined }],
  acceptRemark: ''
})

const acceptRules = {
  acceptResult: [{ required: true, message: '请选择验收结果', trigger: 'change' }],
  actualCost: [{ required: true, message: '请输入实际费用', trigger: 'blur' }]
}

const addPart = () => {
  acceptForm.partsDetail.push({ name: '', quantity: 1, price: undefined })
}

const removePart = (i) => {
  acceptForm.partsDetail.splice(i, 1)
}

const openAccept = (row) => {
  Object.assign(acceptForm, {
    repairId: row.id,
    repairNo: row.repairNo,
    acceptResult: 'PASS',
    actualCost: undefined,
    laborCost: undefined,
    partsDetail: [{ name: '', quantity: 1, price: undefined }],
    acceptRemark: ''
  })
  acceptFormRef.value && acceptFormRef.value.clearValidate()
  acceptVisible.value = true
}

const submitAccept = () => {
  acceptFormRef.value.validate(async (valid) => {
    if (!valid) return
    accepting.value = true
    try {
      const isPass = acceptForm.acceptResult === 'PASS'
      await acceptRepair({
        repairId: acceptForm.repairId,
        acceptResult: acceptForm.acceptResult,
        actualCost: isPass ? acceptForm.actualCost : 0,
        partsDetail: isPass ? acceptForm.partsDetail.filter((p) => p.name) : undefined,
        laborCost: isPass ? (acceptForm.laborCost ?? undefined) : undefined,
        acceptRemark: acceptForm.acceptRemark
      })
      ElMessage.success('验收完成')
      acceptVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      accepting.value = false
    }
  })
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getRepairDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadRooms()
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

.sub-title {
  margin: 16px 0 8px;
  font-size: 14px;
  color: #606266;
}

.parts-wrap {
  width: 100%;
}

.parts-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
</style>
