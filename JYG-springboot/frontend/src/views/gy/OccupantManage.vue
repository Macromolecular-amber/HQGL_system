<template>
  <div class="occupant-manage-page">
    <!-- Tab 切换 -->
    <el-card shadow="never" class="tab-card">
      <div class="tab-bar">
        <el-radio-group v-model="tabType" @change="handleTabChange">
          <el-radio-button value="expert">专家公寓</el-radio-button>
          <el-radio-button value="talent">人才公寓</el-radio-button>
        </el-radio-group>
        <div class="tab-actions">
          <template v-if="tabType === 'expert'">
            <el-button type="primary" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="openAssign">直接分配</el-button>
          </template>
          <template v-else>
            <el-button type="warning" plain @click="filterPending">待审批列表</el-button>
          </template>
        </div>
      </div>
    </el-card>

    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="入住人">
          <el-input v-model="query.occupantName" placeholder="姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="所属单位">
          <el-select v-model="query.unitId" placeholder="全部" clearable filterable style="width: 200px">
            <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.occupantStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 入住记录列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="occupantName" label="入住人" width="100" show-overflow-tooltip />
        <el-table-column label="公寓类型" width="90" align="center">
          <template #default="{ row }">{{ row.roomTypeLabel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="90" show-overflow-tooltip>
          <template #default="{ row }">{{ row.roomNo || '待分配' }}</template>
        </el-table-column>
        <el-table-column prop="unitName" label="所属单位" min-width="160" show-overflow-tooltip />
        <el-table-column label="入住时间" width="155">
          <template #default="{ row }">{{ formatTime(row.checkinTime) }}</template>
        </el-table-column>
        <el-table-column label="预计离开" width="155">
          <template #default="{ row }">{{ formatTime(row.expectedLeaveTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.occupantStatus)">
              {{ row.statusLabel || row.occupantStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="isActive(row)" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER']" link type="danger" @click="openCheckout(row)">退住</el-button>
            <el-button v-if="isPending(row)" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER']" link type="warning" @click="openAudit(row)">审批</el-button>
            <el-button v-if="canAccept(row)" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DIRECTOR','DEPT_MANAGER']" link type="success" @click="openAccept(row)">验收</el-button>
            <el-tag v-if="isAccepted(row)" type="success" size="small">已验收</el-tag>
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

    <!-- 直接分配对话框（专家公寓） -->
    <el-dialog v-model="assignVisible" title="专家公寓直接分配" width="620px" :close-on-click-modal="false">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间" prop="roomId">
              <el-select v-model="assignForm.roomId" placeholder="请选择空闲房间" style="width: 100%">
                <el-option v-for="r in expertRooms" :key="r.id" :label="`${r.roomNo}（${r.building}）`" :value="r.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住人" prop="occupantName">
              <el-input v-model="assignForm.occupantName" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="assignForm.idCard" maxlength="18" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="assignForm.phone" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属单位" prop="unitId">
              <el-select v-model="assignForm.unitId" placeholder="请选择单位" filterable style="width: 100%">
                <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职务">
              <el-input v-model="assignForm.position" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入住时间" prop="checkinTime">
              <el-date-picker v-model="assignForm.checkinTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计离开" prop="expectedLeaveTime">
              <el-date-picker v-model="assignForm.expectedLeaveTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="assignForm.remark" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框（人才公寓） -->
    <el-dialog v-model="auditVisible" title="人才公寓入住审批" width="560px" :close-on-click-modal="false">
      <el-descriptions :column="2" border size="small" class="audit-info">
        <el-descriptions-item label="入住人">{{ auditForm.occupantName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ auditForm.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属单位" :span="2">{{ auditForm.unitName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职务">{{ auditForm.position || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ auditForm.applyReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="auditFormRef" :model="auditForm" :rules="auditRules" label-width="100px" class="audit-form">
        <el-form-item label="审批结果" prop="auditResult">
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="auditForm.auditResult === 'PASS'" label="分配房间" prop="roomId">
          <el-select v-model="auditForm.roomId" placeholder="请选择空闲人才公寓房间" clearable style="width: 100%">
            <el-option v-for="r in talentRooms" :key="r.id" :label="`${r.roomNo}（${r.building}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" maxlength="500" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 退住对话框 -->
    <el-dialog v-model="checkoutVisible" title="退住登记" width="480px" :close-on-click-modal="false">
      <el-form ref="checkoutFormRef" label-width="100px">
        <el-form-item label="入住人">
          <span>{{ checkoutForm.occupantName }}</span>
        </el-form-item>
        <el-form-item label="房间号">
          <span>{{ checkoutForm.roomNo || '待分配' }}</span>
        </el-form-item>
        <el-form-item label="实际退住时间">
          <el-date-picker v-model="checkoutForm.actualLeaveTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="checkoutForm.remark" type="textarea" :rows="3" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="checkingOut" @click="submitCheckout">确认退住</el-button>
      </template>
    </el-dialog>

    <!-- 验收登记对话框 -->
    <el-dialog v-model="acceptVisible" title="退住验收" width="620px" :close-on-click-modal="false">
      <el-form ref="acceptFormRef" :model="acceptForm" label-width="100px">
        <el-form-item label="入住人">
          <span>{{ acceptForm.occupantName }}（{{ acceptForm.roomNo || '待分配' }}）</span>
        </el-form-item>
        <el-form-item label="房屋状况">
          <el-input v-model="acceptForm.roomCondition" type="textarea" :rows="2" maxlength="500" placeholder="描述房间整体状况" />
        </el-form-item>
        <el-form-item label="资产核对">
          <div class="facility-row" v-for="f in acceptForm.facilityItems" :key="f.name">
            <span class="facility-name">{{ f.name }}</span>
            <el-radio-group v-model="f.status">
              <el-radio value="完好">完好</el-radio>
              <el-radio value="损坏">损坏</el-radio>
              <el-radio value="缺失">缺失</el-radio>
            </el-radio-group>
          </div>
          <el-empty v-if="!acceptForm.facilityItems.length" description="该房间暂无设施清单" :image-size="50" />
        </el-form-item>
        <el-form-item label="结算金额">
          <el-input-number v-model="acceptForm.settlementAmount" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结算明细">
          <el-input v-model="acceptForm.settlementDetail" type="textarea" :rows="2" maxlength="500" placeholder="如：水费 xx 元，电费 xx 元" />
        </el-form-item>
        <el-form-item label="退房照片">
          <el-upload v-model:file-list="checkoutPhotoList" :auto-upload="false" :limit="5" multiple>
            <el-button>选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">仅暂存文件名，不实际上传</div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="acceptForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="accepting" @click="submitAccept">确认验收</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="入住记录详情" width="680px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="入住人">{{ detail.occupantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公寓类型">{{ detail.roomTypeLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detail.roomNo || '待分配' }}</el-descriptions-item>
        <el-descriptions-item label="楼栋">{{ detail.building || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detail.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属单位" :span="2">{{ detail.unitName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职务">{{ detail.position || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分配方式">{{ detail.assignMethodLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住时间">{{ formatTime(detail.checkinTime) }}</el-descriptions-item>
        <el-descriptions-item label="预计离开">{{ formatTime(detail.expectedLeaveTime) }}</el-descriptions-item>
        <el-descriptions-item label="实际退住">{{ formatTime(detail.actualLeaveTime) }}</el-descriptions-item>
        <el-descriptions-item label="租金">{{ detail.rentAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.occupantStatus)">
            {{ detail.statusLabel || detail.occupantStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请原因" :span="2">{{ detail.applyReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.auditUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="验收时间">{{ formatTime(detail.acceptTime) }}</el-descriptions-item>
        <el-descriptions-item label="验收人">{{ detail.acceptUserName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房屋状况" :span="2">{{ detail.roomCondition || '-' }}</el-descriptions-item>
        <el-descriptions-item label="结算金额">{{ detail.settlementAmount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="结算明细" :span="1">{{ detail.settlementDetail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退房照片" :span="2">{{ detail.checkoutPhotos || '-' }}</el-descriptions-item>
      </el-descriptions>

      <template v-if="detail && detail.facilityCheckResult">
        <h4 class="sub-title">资产核对结果</h4>
        <el-table :data="facilityCheckList" size="small" border>
          <el-table-column prop="name" label="设施名称" min-width="140" />
          <el-table-column prop="status" label="状态" width="120" align="center" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUnitList } from '@/api/sys'
import { getAvailableRooms, assignDirect, auditOccupant, getOccupantPage, getOccupantDetail, checkoutOccupant, acceptCheckout, getRoomDetail } from '@/api/gy'

/** Tab：专家公寓 / 人才公寓 */
const tabType = ref('expert')

/** 状态映射（忽略大小写） */
const statusOptions = [
  { value: 'active', label: '在住' },
  { value: 'pending', label: '待审批' },
  { value: 'resigned', label: '已退住' }
]
const statusTypeMap = {
  active: 'success',
  pending: 'warning',
  resigned: 'info',
  rejected: 'danger'
}
const statusType = (s) => statusTypeMap[String(s || '').toLowerCase()] || 'info'
const isActive = (row) => String(row.occupantStatus || '').toLowerCase() === 'active'
const isPending = (row) => String(row.occupantStatus || '').toLowerCase() === 'pending'
const isResigned = (row) => String(row.occupantStatus || '').toLowerCase() === 'resigned'
/** 已退住且未验收 -> 显示验收按钮 */
const canAccept = (row) => isResigned(row) && !row.acceptTime
/** 已退住且已完成验收 -> 显示已验收标签 */
const isAccepted = (row) => isResigned(row) && !!row.acceptTime
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')
const nowDateTime = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

/** 单位下拉 */
const units = ref([])

const loadUnits = async () => {
  try {
    units.value = (await getUnitList()) || []
  } catch (e) {
    units.value = []
  }
}

/** 空闲房间（按 Tab 类型加载） */
const expertRooms = ref([])
const talentRooms = ref([])

const loadRooms = async () => {
  try {
    expertRooms.value = (await getAvailableRooms('expert_apartment')) || []
  } catch (e) {
    expertRooms.value = []
  }
  try {
    talentRooms.value = (await getAvailableRooms('talent_apartment')) || []
  } catch (e) {
    talentRooms.value = []
  }
}

/** 查询 */
const query = reactive({
  roomType: 'expert_apartment',
  occupantName: '',
  unitId: undefined,
  occupantStatus: '',
  page: 1,
  size: 10
})
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getOccupantPage({
      roomType: query.roomType,
      occupantName: query.occupantName || undefined,
      unitId: query.unitId ?? undefined,
      occupantStatus: query.occupantStatus || undefined,
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

const handleTabChange = (type) => {
  query.roomType = type === 'talent' ? 'talent_apartment' : 'expert_apartment'
  query.page = 1
  loadList()
}

/** 人才公寓：查看待审批列表 */
const filterPending = () => {
  query.occupantStatus = 'pending'
  query.page = 1
  loadList()
}

const handleQuery = () => {
  query.page = 1
  loadList()
}

const handleReset = () => {
  query.occupantName = ''
  query.unitId = undefined
  query.occupantStatus = ''
  query.page = 1
  loadList()
}

/** 直接分配 */
const assignVisible = ref(false)
const assigning = ref(false)
const assignFormRef = ref()
const assignForm = reactive({
  roomId: null,
  occupantName: '',
  idCard: '',
  phone: '',
  unitId: null,
  position: '',
  checkinTime: '',
  expectedLeaveTime: '',
  remark: ''
})

const assignRules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  occupantName: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  unitId: [{ required: true, message: '请选择所属单位', trigger: 'change' }],
  checkinTime: [{ required: true, message: '请选择入住时间', trigger: 'change' }],
  expectedLeaveTime: [{ required: true, message: '请选择预计离开时间', trigger: 'change' }]
}

const openAssign = async () => {
  await loadRooms()
  Object.assign(assignForm, {
    roomId: null,
    occupantName: '',
    idCard: '',
    phone: '',
    unitId: null,
    position: '',
    checkinTime: nowDateTime(),
    expectedLeaveTime: '',
    remark: ''
  })
  assignFormRef.value && assignFormRef.value.clearValidate()
  assignVisible.value = true
}

const submitAssign = () => {
  assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    assigning.value = true
    try {
      await assignDirect({
        roomId: assignForm.roomId,
        occupantName: assignForm.occupantName,
        idCard: assignForm.idCard || undefined,
        phone: assignForm.phone,
        unitId: assignForm.unitId,
        position: assignForm.position || undefined,
        checkinTime: assignForm.checkinTime,
        expectedLeaveTime: assignForm.expectedLeaveTime,
        remark: assignForm.remark || undefined
      })
      ElMessage.success('分配成功')
      assignVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      assigning.value = false
    }
  })
}

/** 审批 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditFormRef = ref()
const auditForm = reactive({
  occupantId: null,
  occupantName: '',
  phone: '',
  unitName: '',
  position: '',
  applyReason: '',
  auditResult: 'PASS',
  auditRemark: '',
  roomId: null
})

const auditRules = {
  auditResult: [{ required: true, message: '请选择审批结果', trigger: 'change' }]
}

const openAudit = async (row) => {
  await loadRooms()
  Object.assign(auditForm, {
    occupantId: row.id,
    occupantName: row.occupantName,
    phone: row.phone,
    unitName: row.unitName,
    position: row.position,
    applyReason: row.applyReason,
    auditResult: 'PASS',
    auditRemark: '',
    roomId: null
  })
  auditFormRef.value && auditFormRef.value.clearValidate()
  auditVisible.value = true
}

const submitAudit = () => {
  auditFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (auditForm.auditResult === 'PASS' && !auditForm.roomId) {
      ElMessage.warning('审批通过请选择分配房间')
      return
    }
    auditing.value = true
    try {
      await auditOccupant({
        occupantId: auditForm.occupantId,
        auditResult: auditForm.auditResult,
        auditRemark: auditForm.auditRemark,
        roomId: auditForm.auditResult === 'PASS' ? auditForm.roomId : undefined
      })
      ElMessage.success('审批完成')
      auditVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      auditing.value = false
    }
  })
}

/** 退住 */
const checkoutVisible = ref(false)
const checkingOut = ref(false)
const checkoutFormRef = ref()
const checkoutForm = reactive({
  id: null,
  occupantName: '',
  roomNo: '',
  actualLeaveTime: '',
  remark: ''
})

const openCheckout = (row) => {
  Object.assign(checkoutForm, {
    id: row.id,
    occupantName: row.occupantName,
    roomNo: row.roomNo,
    actualLeaveTime: nowDateTime(),
    remark: ''
  })
  checkoutVisible.value = true
}

const submitCheckout = () => {
  checkingOut.value = true
  try {
    checkoutOccupant(checkoutForm.id, {
      actualLeaveTime: checkoutForm.actualLeaveTime || undefined,
      remark: checkoutForm.remark || undefined
    }).then(() => {
      ElMessage.success('退住成功')
      checkoutVisible.value = false
      loadList()
    }).finally(() => {
      checkingOut.value = false
    })
  } catch (e) {
    checkingOut.value = false
  }
}

/** 退住验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptFormRef = ref()
const checkoutPhotoList = ref([])
const acceptForm = reactive({
  id: null,
  occupantName: '',
  roomNo: '',
  roomCondition: '',
  facilityItems: [],
  settlementAmount: undefined,
  settlementDetail: '',
  remark: ''
})

/** 解析房间配套设施 JSON（{"家具":["床","衣柜"],...}）为设施清单 */
const parseRoomFacilities = (facilitiesJson) => {
  if (!facilitiesJson) return []
  try {
    const obj = JSON.parse(facilitiesJson)
    const names = []
    Object.values(obj).forEach((v) => {
      if (Array.isArray(v)) {
        names.push(...v.map((n) => String(n)))
      } else if (v !== null && v !== undefined) {
        // 兼容 {"冰箱": 1, "电视": 1} 键值形式
        names.push(...Object.keys(obj))
      }
    })
    if (!names.length) names.push(...Object.keys(obj))
    const unique = [...new Set(names.filter((n) => n && n.trim()))]
    return unique.map((name) => ({ name, status: '完好' }))
  } catch (e) {
    return []
  }
}

const openAccept = async (row) => {
  Object.assign(acceptForm, {
    id: row.id,
    occupantName: row.occupantName,
    roomNo: row.roomNo,
    roomCondition: '',
    facilityItems: [],
    settlementAmount: undefined,
    settlementDetail: '',
    remark: ''
  })
  checkoutPhotoList.value = []
  // 从房间档案读取设施清单，逐项核对
  if (row.roomId) {
    try {
      const room = await getRoomDetail(row.roomId)
      acceptForm.facilityItems = parseRoomFacilities(room.facilities)
    } catch (e) {
      acceptForm.facilityItems = []
    }
  }
  acceptVisible.value = true
}

const submitAccept = () => {
  accepting.value = true
  try {
    const facilityMap = {}
    acceptForm.facilityItems.forEach((f) => {
      facilityMap[f.name] = f.status
    })
    acceptCheckout({
      occupantId: acceptForm.id,
      checkoutTime: nowDateTime(),
      roomCondition: acceptForm.roomCondition || undefined,
      facilityCheckResult: Object.keys(facilityMap).length ? facilityMap : undefined,
      settlementAmount: acceptForm.settlementAmount ?? undefined,
      settlementDetail: acceptForm.settlementDetail || undefined,
      checkoutPhotos: checkoutPhotoList.value.map((f) => f.name),
      remark: acceptForm.remark || undefined
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

/** 资产核对结果（详情表格） */
const facilityCheckList = computed(() => {
  if (!detail.value || !detail.value.facilityCheckResult) return []
  try {
    const obj = JSON.parse(detail.value.facilityCheckResult)
    return Object.entries(obj).map(([name, status]) => ({ name, status }))
  } catch (e) {
    return []
  }
})

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getOccupantDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadUnits()
  loadRooms()
  loadList()
})
</script>

<style scoped>
.tab-card {
  margin-bottom: 16px;
}

.tab-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.query-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.audit-info {
  margin-bottom: 16px;
}

.audit-form {
  margin-top: 8px;
}

.facility-row {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 6px 0;
  border-bottom: 1px dashed #ebeef5;
}

.facility-name {
  width: 140px;
  font-size: 13px;
  color: #606266;
}

.sub-title {
  margin: 16px 0 8px;
  font-size: 14px;
  color: #606266;
}
</style>
