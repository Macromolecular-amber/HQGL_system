<template>
  <div class="vehicle-manage-page">
    <!-- 查询条件区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="车牌号">
          <el-input
            v-model="query.plateNumber"
            placeholder="请输入车牌号"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="车辆类型">
          <el-select v-model="query.vehicleType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in vehicleTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="车辆状态">
          <el-select v-model="query.vehicleStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属单位">
          <el-select v-model="query.unitId" placeholder="全部" clearable filterable style="width: 200px">
            <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain @click="openAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 车辆列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="plateNumber" label="车牌号" width="120" show-overflow-tooltip />
        <el-table-column prop="brandModel" label="品牌型号" min-width="160" show-overflow-tooltip />
        <el-table-column label="车辆类型" width="100">
          <template #default="{ row }">{{ row.vehicleTypeLabel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="seatCount" label="座位数" width="80" align="center" />
        <el-table-column prop="unitName" label="所属单位" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.unitName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.vehicleStatus)">
              {{ row.vehicleStatusLabel || row.vehicleStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="购置日期" width="110">
          <template #default="{ row }">{{ row.purchaseDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button link type="info" @click="showDetail(row)">详情</el-button>
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
    <el-dialog v-model="formVisible" :title="form.id ? '编辑车辆' : '新增车辆'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车牌号" prop="plateNumber">
              <el-input v-model="form.plateNumber" placeholder="如 甘B·00001" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌型号" prop="brandModel">
              <el-input v-model="form.brandModel" placeholder="如 丰田考斯特" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车辆类型" prop="vehicleType">
              <el-select v-model="form.vehicleType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in vehicleTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位数" prop="seatCount">
              <el-input-number v-model="form.seatCount" :min="1" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发动机号" prop="engineNo">
              <el-input v-model="form.engineNo" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车架号" prop="frameNo">
              <el-input v-model="form.frameNo" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排量" prop="displacement">
              <el-input-number
                v-model="form.displacement"
                :min="0"
                :precision="1"
                :controls="false"
                placeholder="L"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="form.color" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购置日期" prop="purchaseDate">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="购置价格" prop="purchasePrice">
              <el-input-number
                v-model="form.purchasePrice"
                :min="0"
                :precision="2"
                :controls="false"
                placeholder="元"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="所属单位" prop="unitId">
              <el-select v-model="form.unitId" placeholder="请选择" filterable style="width: 100%">
                <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="车辆详情" width="700px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="车牌号">{{ detail.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="品牌型号">{{ detail.brandModel }}</el-descriptions-item>
        <el-descriptions-item label="车辆类型">
          {{ detail.vehicleTypeLabel || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.vehicleStatus)">
            {{ detail.vehicleStatusLabel || detail.vehicleStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发动机号">{{ detail.engineNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="车架号">{{ detail.frameNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="座位数">{{ detail.seatCount }}</el-descriptions-item>
        <el-descriptions-item label="排量(L)">{{ detail.displacement ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="颜色">{{ detail.color || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ detail.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="购置价格(元)">{{ detail.purchasePrice ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属单位">{{ detail.unitName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前里程(km)">{{ detail.currentMileage ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="下次保养里程(km)">{{ detail.nextMaintenanceMileage ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="保险公司">{{ detail.insuranceCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保险止期">{{ detail.insuranceEnd || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveVehicle, deleteVehicle, getVehiclePage, getVehicleDetail } from '@/api/cl'
import { getUnitList } from '@/api/sys'

/** 车辆类型 */
const vehicleTypes = [
  { value: 'SEDAN', label: '轿车' },
  { value: 'SUV', label: 'SUV' },
  { value: 'MPV', label: 'MPV' },
  { value: 'BUS', label: '客车' }
]

/** 状态筛选选项与标签配色 */
const statusOptions = [
  { value: 'AVAILABLE', label: '可用' },
  { value: 'ON_DUTY', label: '出车中' },
  { value: 'REPAIRING', label: '维修中' },
  { value: 'MAINTAINING', label: '保养中' },
  { value: 'WAIT_SCRAP', label: '待报废' },
  { value: 'SCRAPPED', label: '已报废' }
]

const statusTypeMap = {
  AVAILABLE: 'success',
  ON_DUTY: 'primary',
  REPAIRING: 'danger',
  MAINTAINING: 'warning',
  WAIT_SCRAP: 'info',
  SCRAPPED: 'info'
}

const statusType = (s) => statusTypeMap[s] || 'info'

/** 查询条件 */
const query = reactive({
  plateNumber: '',
  vehicleType: '',
  vehicleStatus: '',
  unitId: undefined,
  page: 1,
  size: 10
})

const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getVehiclePage({
      plateNumber: query.plateNumber || undefined,
      vehicleType: query.vehicleType || undefined,
      vehicleStatus: query.vehicleStatus || undefined,
      unitId: query.unitId ?? undefined,
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
  query.plateNumber = ''
  query.vehicleType = ''
  query.vehicleStatus = ''
  query.unitId = undefined
  query.page = 1
  loadList()
}

/** 新增/编辑表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  plateNumber: '',
  brandModel: '',
  vehicleType: '',
  engineNo: '',
  frameNo: '',
  seatCount: undefined,
  displacement: undefined,
  color: '',
  purchaseDate: '',
  purchasePrice: undefined,
  unitId: undefined,
  remark: ''
})

const rules = {
  plateNumber: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  brandModel: [{ required: true, message: '请输入品牌型号', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请选择车辆类型', trigger: 'change' }],
  seatCount: [{ required: true, message: '请输入座位数', trigger: 'blur' }],
  unitId: [{ required: true, message: '请选择所属单位', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    plateNumber: '',
    brandModel: '',
    vehicleType: '',
    engineNo: '',
    frameNo: '',
    seatCount: undefined,
    displacement: undefined,
    color: '',
    purchaseDate: '',
    purchasePrice: undefined,
    unitId: undefined,
    remark: ''
  })
}

const openAdd = () => {
  resetForm()
  formVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    plateNumber: row.plateNumber,
    brandModel: row.brandModel,
    vehicleType: row.vehicleType,
    engineNo: row.engineNo,
    frameNo: row.frameNo,
    seatCount: row.seatCount,
    displacement: row.displacement,
    color: row.color,
    purchaseDate: row.purchaseDate,
    purchasePrice: row.purchasePrice,
    unitId: row.unitId,
    remark: row.remark
  })
  formVisible.value = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await saveVehicle({
        id: form.id || undefined,
        plateNumber: form.plateNumber,
        brandModel: form.brandModel,
        vehicleType: form.vehicleType,
        engineNo: form.engineNo || undefined,
        frameNo: form.frameNo || undefined,
        seatCount: form.seatCount,
        displacement: form.displacement ?? undefined,
        color: form.color || undefined,
        purchaseDate: form.purchaseDate || undefined,
        purchasePrice: form.purchasePrice ?? undefined,
        unitId: form.unitId,
        remark: form.remark || undefined
      })
      ElMessage.success(form.id ? '编辑成功' : '新增成功')
      formVisible.value = false
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      saving.value = false
    }
  })
}

/** 删除 */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除车辆「${row.plateNumber}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async () => {
      await deleteVehicle(row.id)
      ElMessage.success('删除成功')
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
    detail.value = await getVehicleDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

/** 单位下拉 */
const units = ref([])

onMounted(async () => {
  try {
    units.value = (await getUnitList()) || []
  } catch (e) {
    units.value = []
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
