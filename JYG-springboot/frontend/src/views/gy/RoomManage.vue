<template>
  <div class="room-manage-page">
    <!-- 查询条件区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="楼栋">
          <el-input v-model="query.building" placeholder="楼栋名称" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="query.floor" :min="0" :controls="false" placeholder="全部" style="width: 120px" />
        </el-form-item>
        <el-form-item label="房间类型">
          <el-select v-model="query.roomType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in roomTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间状态">
          <el-select v-model="query.roomStatus" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" plain v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="openAdd">新增房间</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="building" label="楼栋" min-width="150" show-overflow-tooltip />
        <el-table-column prop="floor" label="楼层" width="70" align="center" />
        <el-table-column prop="roomNo" label="房间号" width="100" show-overflow-tooltip />
        <el-table-column label="房间类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.roomTypeLabel || row.roomType }}
          </template>
        </el-table-column>
        <el-table-column prop="layout" label="户型" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.layout || '-' }}</template>
        </el-table-column>
        <el-table-column prop="area" label="面积(㎡)" width="100" align="right">
          <template #default="{ row }">{{ row.area ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.roomStatus)">
              {{ row.roomStatusLabel || row.roomStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentOccupantName" label="当前入住人" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.currentOccupantName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button link type="primary" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="formVisible" :title="form.id ? '编辑房间' : '新增房间'" width="620px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="楼栋" prop="building">
              <el-input v-model="form.building" maxlength="20" placeholder="如：专家公寓A栋" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input-number v-model="form.floor" :min="0" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomNo">
              <el-input v-model="form.roomNo" maxlength="20" placeholder="如：A-0501" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间类型" prop="roomType">
              <el-select v-model="form.roomType" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in roomTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="户型">
              <el-input v-model="form.layout" maxlength="20" placeholder="如：两室一厅" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(㎡)">
              <el-input-number v-model="form.area" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="配套设施">
              <el-input
                v-model="form.facilitiesText"
                type="textarea"
                :rows="4"
                placeholder='JSON 格式，如 {"家具":["床","衣柜"],"家电":["空调","电视"]}'
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" placeholder="选填" />
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
    <el-dialog v-model="detailVisible" title="房间详情" width="620px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="楼栋">{{ detail.building || '-' }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ detail.floor ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detail.roomNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="房间类型">
          {{ detail.roomTypeLabel || detail.roomType }}
        </el-descriptions-item>
        <el-descriptions-item label="户型">{{ detail.layout || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面积(㎡)">{{ detail.area ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.roomStatus)">
            {{ detail.roomStatusLabel || detail.roomStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前入住人">{{ detail.currentOccupantName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入住人数">{{ detail.occupantCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="sub-title">配套设施</h4>
      <pre v-if="detail && detail.facilities" class="facilities-pre">{{ formatFacilities(detail.facilities) }}</pre>
      <el-empty v-else description="暂无配套设施" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { saveRoom, deleteRoom, getRoomPage, getRoomDetail } from '@/api/gy'

/** 房间类型 */
const roomTypes = [
  { value: 'expert_apartment', label: '专家公寓' },
  { value: 'talent_apartment', label: '人才公寓' }
]

/** 状态映射（忽略大小写） */
const statusOptions = [
  { value: 'idle', label: '空闲' },
  { value: 'occupied', label: '已入住' },
  { value: 'repairing', label: '维修中' },
  { value: 'reserved', label: '已预留' }
]
const statusTypeMap = {
  idle: 'success',
  occupied: 'primary',
  repairing: 'warning',
  reserved: 'info'
}
const statusType = (s) => statusTypeMap[String(s || '').toLowerCase()] || 'info'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 查询 */
const query = reactive({ building: '', floor: undefined, roomType: '', roomStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getRoomPage({
      building: query.building || undefined,
      floor: query.floor ?? undefined,
      roomType: query.roomType || undefined,
      roomStatus: query.roomStatus || undefined,
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
  query.building = ''
  query.floor = undefined
  query.roomType = ''
  query.roomStatus = ''
  query.page = 1
  loadList()
}

/** 新增/编辑表单 */
const formVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  building: '',
  floor: undefined,
  roomNo: '',
  roomType: '',
  layout: '',
  area: undefined,
  facilitiesText: '',
  remark: ''
})

const rules = {
  building: [{ required: true, message: '请输入楼栋', trigger: 'blur' }],
  floor: [{ required: true, message: '请输入楼层', trigger: 'blur' }],
  roomNo: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房间类型', trigger: 'change' }]
}

const openAdd = () => {
  Object.assign(form, {
    id: null,
    building: '',
    floor: undefined,
    roomNo: '',
    roomType: '',
    layout: '',
    area: undefined,
    facilitiesText: '',
    remark: ''
  })
  formRef.value && formRef.value.clearValidate()
  formVisible.value = true
}

const openEdit = async (row) => {
  try {
    const d = await getRoomDetail(row.id)
    Object.assign(form, {
      id: d.id,
      building: d.building,
      floor: d.floor,
      roomNo: d.roomNo,
      roomType: String(d.roomType || '').toLowerCase(),
      layout: d.layout,
      area: d.area,
      facilitiesText: d.facilities ? formatFacilities(d.facilities) : '',
      remark: d.remark
    })
    formRef.value && formRef.value.clearValidate()
    formVisible.value = true
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

/** 配套设施：解析 JSON 文本为对象，供后端存储 */
const parseFacilities = () => {
  const text = (form.facilitiesText || '').trim()
  if (!text) return undefined
  try {
    const obj = JSON.parse(text)
    if (obj && typeof obj === 'object' && !Array.isArray(obj)) {
      return obj
    }
    throw new Error('invalid')
  } catch (e) {
    ElMessage.warning('配套设施需为 JSON 对象格式，如 {"家具":["床","衣柜"]}')
    return null
  }
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const facilities = parseFacilities()
    if (facilities === null) return
    saving.value = true
    try {
      const payload = {
        id: form.id || undefined,
        building: form.building,
        floor: form.floor,
        roomNo: form.roomNo,
        roomType: form.roomType,
        layout: form.layout || undefined,
        area: form.area ?? undefined,
        facilities,
        remark: form.remark || undefined
      }
      await saveRoom(payload)
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
  ElMessageBox.confirm(`确定删除房间「${row.roomNo}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
    .then(async () => {
      await deleteRoom(row.id)
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
    detail.value = await getRoomDetail(row.id)
  } catch (e) {
    detailVisible.value = false
  }
}

/** 配套设施 JSON 格式化展示 */
const formatFacilities = (facilities) => {
  if (!facilities) return ''
  try {
    return JSON.stringify(JSON.parse(facilities), null, 2)
  } catch (e) {
    return facilities
  }
}

onMounted(() => {
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

.facilities-pre {
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
