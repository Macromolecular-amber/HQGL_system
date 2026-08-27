<template>
  <div class="dispatch-manage-page">
    <!-- 待派车申请列表 -->
    <el-card shadow="never" class="pending-card">
      <template #header>
        <span class="card-title">待派车申请</span>
      </template>
      <el-table v-loading="pendingLoading" :data="pendingList" border stripe>
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
        <el-table-column prop="applicantName" label="申请人" width="100" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDispatch(row)">派车</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!pendingLoading && !pendingList.length" description="暂无待派车申请" :image-size="60" />
    </el-card>

    <!-- 派单记录 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">派单记录</span>
          <el-button link type="primary" @click="loadDispatchList">刷新</el-button>
        </div>
      </template>
      <el-table v-loading="dispatchLoading" :data="dispatchList" border stripe>
        <el-table-column prop="dispatchNo" label="派单编号" width="150" show-overflow-tooltip />
        <el-table-column prop="applyNo" label="关联申请" width="150" show-overflow-tooltip />
        <el-table-column prop="plateNumber" label="车牌号" width="110" show-overflow-tooltip />
        <el-table-column prop="driverName" label="驾驶员" width="90" show-overflow-tooltip />
        <el-table-column label="计划时间" min-width="230">
          <template #default="{ row }">{{ formatPeriod(row.scheduledStart, row.scheduledEnd) }}</template>
        </el-table-column>
        <el-table-column label="实际时间" min-width="160">
          <template #default="{ row }">{{ formatTime(row.actualEnd) }}</template>
        </el-table-column>
        <el-table-column label="里程(km)" width="90" align="right">
          <template #default="{ row }">{{ row.actualMileage ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.dispatchStatus)">
              {{ row.statusLabel || row.dispatchStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button
              v-if="row.dispatchStatus === 'WAITING' || row.dispatchStatus === 'ONGOING'"
              link
              type="warning"
              @click="openReturn(row)"
            >
              归还
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
          @size-change="loadDispatchList"
          @current-change="loadDispatchList"
        />
      </div>
    </el-card>

    <!-- 派车对话框 -->
    <el-dialog v-model="dispatchVisible" title="车辆派车" width="640px" :close-on-click-modal="false">
      <template v-if="currentApply">
        <el-descriptions :column="2" border size="small" class="apply-info">
          <el-descriptions-item label="申请编号">{{ currentApply.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentApply.applicantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用车事由" :span="2">{{ currentApply.purpose || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目的地">{{ currentApply.destination || '-' }}</el-descriptions-item>
          <el-descriptions-item label="所需车型">
            {{ currentApply.vehicleTypeLabel || currentApply.requiredVehicleType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用车时间" :span="2">
            {{ formatPeriod(currentApply.startTime, currentApply.endTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-form ref="dispatchFormRef" :model="dispatchForm" :rules="dispatchRules" label-width="100px" class="dispatch-form">
          <el-form-item label="调度车辆" prop="vehicleId">
            <el-select v-model="dispatchForm.vehicleId" placeholder="请选择车辆" filterable style="width: 100%">
              <el-option
                v-for="v in vehicles"
                :key="v.id"
                :label="`${v.plateNumber}（${v.brandModel || ''} ${v.vehicleTypeLabel || ''}）`"
                :value="v.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="驾驶员" prop="driverId">
            <el-select v-model="dispatchForm.driverId" placeholder="请选择驾驶员" filterable style="width: 100%">
              <el-option
                v-for="d in drivers"
                :key="d.id"
                :label="`${d.realName}（${d.phone || ''}）`"
                :value="d.id"
              />
            </el-select>
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="计划出车时间" prop="scheduledStart">
                <el-date-picker
                  v-model="dispatchForm.scheduledStart"
                  type="datetime"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  placeholder="请选择"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划返回时间" prop="scheduledEnd">
                <el-date-picker
                  v-model="dispatchForm.scheduledEnd"
                  type="datetime"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  placeholder="请选择"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注">
            <el-input v-model="dispatchForm.remark" type="textarea" :rows="2" maxlength="500" placeholder="选填" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="submitDispatch">确认派车</el-button>
      </template>
    </el-dialog>

    <!-- 归还对话框 -->
    <el-dialog v-model="returnVisible" title="车辆归还" width="480px" :close-on-click-modal="false">
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="110px">
        <el-form-item label="派单编号">
          <span>{{ returnForm.dispatchNo }}</span>
        </el-form-item>
        <el-form-item label="车牌号">
          <span>{{ returnForm.plateNumber }}</span>
        </el-form-item>
        <el-form-item label="实际结束时间" prop="actualEnd">
          <el-date-picker
            v-model="returnForm.actualEnd"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="请选择实际结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际里程(km)" prop="actualMileage">
          <el-input-number
            v-model="returnForm.actualMileage"
            :min="0"
            :precision="1"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="returnForm.remark" type="textarea" :rows="2" maxlength="500" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" :loading="returning" @click="submitReturn">确认归还</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="派单详情" width="700px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="派单编号">{{ detail.dispatchNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.dispatchStatus)">
            {{ detail.statusLabel || detail.dispatchStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联申请">{{ detail.applyNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ detail.applicantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请单位">{{ detail.applicantUnitName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用车事由">{{ detail.purpose || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ detail.destination || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ detail.plateNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ detail.vehicleTypeLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驾驶员">{{ detail.driverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="驾驶员电话">{{ detail.driverPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计划时间" :span="2">
          {{ formatPeriod(detail.scheduledStart, detail.scheduledEnd) }}
        </el-descriptions-item>
        <el-descriptions-item label="实际结束时间">{{ formatTime(detail.actualEnd) }}</el-descriptions-item>
        <el-descriptions-item label="实际里程(km)">{{ detail.actualMileage ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getApplyPage, getAvailableVehicles, dispatchCar, returnCar, getDispatchPage, getDispatchDetail } from '@/api/cl'
import { getDrivers } from '@/api/sys'

/** 调度状态映射 */
const statusMap = {
  WAITING: { text: '待出车', type: 'warning' },
  ONGOING: { text: '出车中', type: 'primary' },
  RETURNED: { text: '已归还', type: 'success' },
  CANCELLED: { text: '已取消', type: 'info' }
}

const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const formatPeriod = (start, end) => `${formatTime(start)} ~ ${formatTime(end)}`

/** 待派车申请 */
const pendingLoading = ref(false)
const pendingList = ref([])

const loadPendingList = async () => {
  pendingLoading.value = true
  try {
    const res = await getApplyPage({ applyStatus: 'APPROVED', page: 1, size: 50 })
    pendingList.value = res.data || []
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    pendingLoading.value = false
  }
}

/** 派单记录 */
const dispatchLoading = ref(false)
const dispatchList = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadDispatchList = async () => {
  dispatchLoading.value = true
  try {
    const res = await getDispatchPage({ page: query.page, size: query.size })
    dispatchList.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    dispatchLoading.value = false
  }
}

/** 派车 */
const dispatchVisible = ref(false)
const dispatching = ref(false)
const dispatchFormRef = ref()
const currentApply = ref(null)
const vehicles = ref([])
const drivers = ref([])
const dispatchForm = reactive({
  applyId: null,
  vehicleId: null,
  driverId: null,
  scheduledStart: '',
  scheduledEnd: '',
  remark: ''
})

const dispatchRules = {
  vehicleId: [{ required: true, message: '请选择调度车辆', trigger: 'change' }],
  driverId: [{ required: true, message: '请选择驾驶员', trigger: 'change' }],
  scheduledStart: [{ required: true, message: '请选择计划出车时间', trigger: 'change' }],
  scheduledEnd: [{ required: true, message: '请选择计划返回时间', trigger: 'change' }]
}

const openDispatch = (row) => {
  currentApply.value = row
  Object.assign(dispatchForm, {
    applyId: row.id,
    vehicleId: null,
    driverId: null,
    scheduledStart: '',
    scheduledEnd: '',
    remark: ''
  })
  dispatchFormRef.value && dispatchFormRef.value.clearValidate()
  dispatchVisible.value = true
  loadOptions()
}

const loadOptions = async () => {
  try {
    vehicles.value = (await getAvailableVehicles()) || []
  } catch (e) {
    vehicles.value = []
  }
  try {
    drivers.value = (await getDrivers()) || []
  } catch (e) {
    drivers.value = []
  }
}

const submitDispatch = () => {
  dispatchFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (dispatchForm.scheduledStart >= dispatchForm.scheduledEnd) {
      ElMessage.warning('计划出车时间必须早于计划返回时间')
      return
    }
    dispatching.value = true
    try {
      await dispatchCar({
        applyId: dispatchForm.applyId,
        vehicleId: dispatchForm.vehicleId,
        driverId: dispatchForm.driverId,
        scheduledStart: dispatchForm.scheduledStart,
        scheduledEnd: dispatchForm.scheduledEnd,
        remark: dispatchForm.remark || undefined
      })
      ElMessage.success('派车成功')
      dispatchVisible.value = false
      loadPendingList()
      loadDispatchList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      dispatching.value = false
    }
  })
}

/** 归还 */
const returnVisible = ref(false)
const returning = ref(false)
const returnFormRef = ref()
const returnForm = reactive({
  dispatchId: null,
  dispatchNo: '',
  plateNumber: '',
  actualEnd: '',
  actualMileage: undefined,
  remark: ''
})

const returnRules = {
  actualEnd: [{ required: true, message: '请选择实际结束时间', trigger: 'change' }],
  actualMileage: [{ required: true, message: '请输入实际行驶里程', trigger: 'blur' }]
}

const openReturn = (row) => {
  Object.assign(returnForm, {
    dispatchId: row.id,
    dispatchNo: row.dispatchNo,
    plateNumber: row.plateNumber,
    actualEnd: '',
    actualMileage: undefined,
    remark: ''
  })
  returnFormRef.value && returnFormRef.value.clearValidate()
  returnVisible.value = true
}

const submitReturn = () => {
  returnFormRef.value.validate(async (valid) => {
    if (!valid) return
    returning.value = true
    try {
      await returnCar({
        dispatchId: returnForm.dispatchId,
        actualEnd: returnForm.actualEnd,
        actualMileage: returnForm.actualMileage,
        remark: returnForm.remark || undefined
      })
      ElMessage.success('归还成功')
      returnVisible.value = false
      loadDispatchList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      returning.value = false
    }
  })
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getDispatchDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadPendingList()
  loadDispatchList()
})
</script>

<style scoped>
.card-title {
  font-weight: 600;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pending-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.apply-info {
  margin-bottom: 16px;
}

.dispatch-form {
  margin-top: 8px;
}
</style>
