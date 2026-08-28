<template>
  <div class="cleaning-manage-page">
    <!-- 查询条件区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="房间">
          <el-select v-model="query.roomId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="r in rooms" :key="r.id" :label="`${r.roomNo}（${r.building}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="保洁类型">
          <el-select v-model="query.cleaningType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in cleaningTypes" :key="t.value" :label="t.label" :value="t.value" />
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
          <el-button type="primary" plain @click="openApply">新增保洁</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 保洁单列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="cleaningNo" label="保洁单号" width="150" show-overflow-tooltip />
        <el-table-column prop="roomNo" label="房间号" width="90" show-overflow-tooltip />
        <el-table-column label="保洁类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.cleaningType === 'ON_DEMAND' ? 'warning' : 'success'">
              {{ row.cleaningTypeLabel || row.cleaningType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="保洁日期" width="110" align="center">
          <template #default="{ row }">{{ cleaningDate(row.cleaningTime) }}</template>
        </el-table-column>
        <el-table-column prop="cleaningScope" label="保洁范围" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.cleaningScope || '-' }}</template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="保洁员" width="90" show-overflow-tooltip>
          <template #default="{ row }">{{ row.assigneeName || '-' }}</template>
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
            <el-button v-if="row.orderStatus === 'PENDING'" link type="warning" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER']" @click="openAudit(row)">
              审批
            </el-button>
            <el-button v-if="row.orderStatus === 'APPROVED'" link type="primary" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER']" @click="openAssign(row)">
              派单
            </el-button>
            <el-button v-if="row.orderStatus === 'ONGOING'" link type="success" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER','CLEANER']" @click="openAccept(row)">
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

    <!-- 新增保洁对话框 -->
    <el-dialog v-model="applyVisible" title="新增保洁" width="600px" :close-on-click-modal="false">
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
            <el-form-item label="保洁类型" prop="cleaningType">
              <el-select v-model="applyForm.cleaningType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in cleaningTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保洁日期" prop="cleaningDate">
              <el-date-picker v-model="applyForm.cleaningDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间段">
              <el-select v-model="applyForm.cleaningTimeSlot" placeholder="选填" clearable style="width: 100%">
                <el-option label="上午" value="MORNING" />
                <el-option label="下午" value="AFTERNOON" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="保洁范围">
              <el-input v-model="applyForm.cleaningScope" type="textarea" :rows="2" maxlength="500" placeholder="如：卧室、客厅" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="特殊要求">
              <el-input v-model="applyForm.cleaningRequirement" type="textarea" :rows="2" maxlength="500" placeholder="如：深度清洁" />
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
    <el-dialog v-model="auditVisible" title="保洁审批" width="480px">
      <el-form label-width="90px">
        <el-form-item label="保洁单号">
          <span>{{ auditForm.cleaningNo }}</span>
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

    <!-- 派单对话框 -->
    <el-dialog v-model="assignVisible" title="保洁派单" width="480px" :close-on-click-modal="false">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="90px">
        <el-form-item label="保洁单号">
          <span>{{ assignForm.cleaningNo }}</span>
        </el-form-item>
        <el-form-item label="保洁员" prop="assigneeId">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择保洁员" filterable style="width: 100%">
            <el-option v-for="c in cleaners" :key="c.id" :label="`${c.realName}（${c.phone || '-'}）`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务公司">
          <el-input v-model="assignForm.assigneeCompany" maxlength="100" placeholder="保洁公司名称，选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 验收对话框 -->
    <el-dialog v-model="acceptVisible" title="保洁验收" width="560px" :close-on-click-modal="false">
      <el-form ref="acceptFormRef" label-width="90px">
        <el-form-item label="保洁单号">
          <span>{{ acceptForm.cleaningNo }}</span>
        </el-form-item>
        <el-form-item label="验收结果" required>
          <el-radio-group v-model="acceptForm.acceptResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="FAIL">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="acceptForm.acceptResult === 'PASS'" label="评分">
          <el-rate v-model="acceptForm.acceptScore" />
        </el-form-item>
        <el-form-item label="验收意见">
          <el-input v-model="acceptForm.acceptRemark" type="textarea" :rows="3" maxlength="500" placeholder="选填" />
        </el-form-item>
        <el-form-item label="执行照片">
          <el-upload v-model:file-list="executePhotoList" :auto-upload="false" :limit="5" multiple>
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅暂存文件名，不实际上传</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="accepting" @click="submitAccept">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="保洁单详情" width="680px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="保洁单号">{{ detail.cleaningNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.orderStatus)">
            {{ detail.statusLabel || detail.orderStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detail.roomNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ detail.building || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保洁类型">
          {{ detail.cleaningTypeLabel || detail.cleaningType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="保洁时间">{{ formatTime(detail.cleaningTime) }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保洁员">{{ detail.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保洁范围" :span="2">{{ detail.cleaningScope || '-' }}</el-descriptions-item>
        <el-descriptions-item label="特殊要求" :span="2">{{ detail.cleaningRequirement || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务公司">{{ detail.assigneeCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="派单时间">{{ formatTime(detail.assignTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收评分">{{ detail.acceptScore ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收时间">{{ formatTime(detail.acceptTime) }}</el-descriptions-item>
        <el-descriptions-item label="验收意见" :span="2">{{ detail.acceptRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行照片" :span="2">{{ detail.executePhotos || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoomPage, applyCleaning, auditCleaning, assignCleaning, acceptCleaning, getCleaningPage, getCleaningDetail } from '@/api/gy'
import { getCleaners } from '@/api/sys'

/** 保洁类型 */
const cleaningTypes = [
  { value: 'REGULAR', label: '定期' },
  { value: 'ON_DEMAND', label: '按需' }
]

/** 状态映射 */
const statusOptions = [
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已批准' },
  { value: 'ONGOING', label: '执行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' }
]
const statusTypeMap = {
  PENDING: 'warning',
  APPROVED: 'success',
  ONGOING: 'primary',
  COMPLETED: 'success',
  REJECTED: 'danger',
  DONE: 'success'
}
const statusType = (s) => statusTypeMap[s] || 'info'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const cleaningDate = (t) => (t ? String(t).slice(0, 10) : '-')

/** 房间与保洁员 */
const rooms = ref([])
const cleaners = ref([])

const loadOptions = async () => {
  try {
    const res = await getRoomPage({ page: 1, size: 100 })
    rooms.value = res.data || []
  } catch (e) {
    rooms.value = []
  }
  try {
    cleaners.value = (await getCleaners()) || []
  } catch (e) {
    cleaners.value = []
  }
}

/** 查询 */
const query = reactive({ roomId: undefined, cleaningType: '', orderStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getCleaningPage({
      roomId: query.roomId ?? undefined,
      cleaningType: query.cleaningType || undefined,
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
  query.cleaningType = ''
  query.orderStatus = ''
  query.page = 1
  loadList()
}

/** 新增保洁 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({
  roomId: null,
  cleaningType: '',
  cleaningDate: '',
  cleaningTimeSlot: '',
  cleaningScope: '',
  cleaningRequirement: ''
})

const applyRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  cleaningType: [{ required: true, message: '请选择保洁类型', trigger: 'change' }],
  cleaningDate: [{ required: true, message: '请选择保洁日期', trigger: 'change' }]
}

const openApply = () => {
  Object.assign(applyForm, {
    roomId: null,
    cleaningType: '',
    cleaningDate: '',
    cleaningTimeSlot: '',
    cleaningScope: '',
    cleaningRequirement: ''
  })
  applyFormRef.value && applyFormRef.value.clearValidate()
  applyVisible.value = true
}

const submitApply = () => {
  applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    applying.value = true
    try {
      await applyCleaning({
        roomId: applyForm.roomId,
        cleaningType: applyForm.cleaningType,
        cleaningDate: applyForm.cleaningDate,
        cleaningTimeSlot: applyForm.cleaningTimeSlot || undefined,
        cleaningScope: applyForm.cleaningScope || undefined,
        cleaningRequirement: applyForm.cleaningRequirement || undefined
      })
      ElMessage.success('保洁申请提交成功')
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
const auditForm = reactive({ cleaningId: null, cleaningNo: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  Object.assign(auditForm, { cleaningId: row.id, cleaningNo: row.cleaningNo, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await auditCleaning({
      cleaningId: auditForm.cleaningId,
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

/** 派单 */
const assignVisible = ref(false)
const assigning = ref(false)
const assignFormRef = ref()
const assignForm = reactive({ cleaningId: null, cleaningNo: '', assigneeId: null, assigneeCompany: '' })

const assignRules = {
  assigneeId: [{ required: true, message: '请选择保洁员', trigger: 'change' }]
}

const openAssign = (row) => {
  Object.assign(assignForm, { cleaningId: row.id, cleaningNo: row.cleaningNo, assigneeId: null, assigneeCompany: '' })
  assignFormRef.value && assignFormRef.value.clearValidate()
  assignVisible.value = true
}

const submitAssign = () => {
  assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    assigning.value = true
    try {
      await assignCleaning({
        cleaningId: assignForm.cleaningId,
        assigneeId: assignForm.assigneeId,
        assigneeCompany: assignForm.assigneeCompany || undefined
      })
      ElMessage.success('派单成功')
      assignVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      assigning.value = false
    }
  })
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptFormRef = ref()
const executePhotoList = ref([])
const acceptForm = reactive({
  cleaningId: null,
  cleaningNo: '',
  acceptResult: 'PASS',
  acceptScore: 5,
  acceptRemark: ''
})

const openAccept = (row) => {
  Object.assign(acceptForm, {
    cleaningId: row.id,
    cleaningNo: row.cleaningNo,
    acceptResult: 'PASS',
    acceptScore: 5,
    acceptRemark: ''
  })
  executePhotoList.value = []
  acceptVisible.value = true
}

const submitAccept = () => {
  accepting.value = true
  try {
    acceptCleaning({
      cleaningId: acceptForm.cleaningId,
      acceptResult: acceptForm.acceptResult,
      acceptScore: acceptForm.acceptResult === 'PASS' ? acceptForm.acceptScore : undefined,
      acceptRemark: acceptForm.acceptRemark || undefined,
      executePhotos: acceptForm.acceptResult === 'PASS' ? executePhotoList.value.map((f) => f.name) : undefined
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
    detail.value = await getCleaningDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadOptions()
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
