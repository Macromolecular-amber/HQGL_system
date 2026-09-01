<template>
  <div class="asset-apply-page">
    <!-- 入仓申请表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span class="card-title">入仓申请</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="资产名称" prop="assetName">
              <el-input v-model="form.assetName" placeholder="请输入资产名称" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="分类" prop="categoryCode">
              <el-select v-model="form.categoryCode" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.code" :label="c.name" :value="c.code" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="规格型号" prop="specModel">
              <el-input v-model="form.specModel" placeholder="请输入规格型号" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="请输入品牌" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="原价值(元)" prop="originalValue">
              <el-input-number
                v-model="form.originalValue"
                :min="0"
                :precision="2"
                :controls="false"
                placeholder="请输入原价值"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="购置日期" prop="purchaseDate">
              <el-date-picker
                v-model="form.purchaseDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="请选择购置日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="使用年限(年)" prop="usefulLife">
              <el-input-number
                v-model="form.usefulLife"
                :min="1"
                :controls="false"
                placeholder="请输入使用年限"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="存放地点" prop="location">
              <el-input v-model="form.location" placeholder="请输入存放地点" maxlength="200" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="权属单位" prop="ownerUnitId">
              <el-select v-model="form.ownerUnitId" placeholder="请选择单位" style="width: 100%">
                <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" maxlength="1000" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="照片">
              <el-upload
                v-model:file-list="photoList"
                list-type="picture-card"
                :auto-upload="false"
                :limit="5"
                accept="image/*"
              >
                <div class="upload-trigger">+</div>
              </el-upload>
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
    <el-card shadow="never" class="list-card">
      <template #header>
        <span class="card-title">入仓申请列表</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="assetCode" label="编号" width="190" show-overflow-tooltip />
        <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="分类" width="100">
          <template #default="{ row }">{{ categoryName(row.categoryCode) }}</template>
        </el-table-column>
        <el-table-column prop="originalValue" label="原价值(元)" width="110" align="right" />
        <el-table-column label="申请单位" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.unitName || row.ownerUnitName || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.assetStatus)">{{ statusText(row.assetStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.assetStatus === 'PENDING'" link type="warning" @click="openAudit(row)">
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
    <el-dialog v-model="auditVisible" title="资产审核" width="480px">
      <el-form label-width="90px">
        <el-form-item label="资产">
          <span>{{ auditForm.assetCode }} - {{ auditForm.assetName }}</span>
        </el-form-item>
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="auditForm.auditResult">
            <el-radio value="PASS">通过</el-radio>
            <el-radio value="REJECT">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditForm.auditRemark" type="textarea" :rows="3" placeholder="请输入审核意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditing" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="资产详情" width="680px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="资产编号">{{ detail.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detail.assetName }}</el-descriptions-item>
        <el-descriptions-item label="分类">
          {{ categoryName(detail.categoryCode) }}
        </el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detail.specModel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="原价值(元)">{{ detail.originalValue }}</el-descriptions-item>
        <el-descriptions-item label="当前净值(元)">{{ detail.currentValue }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ detail.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="使用年限(年)">{{ detail.usefulLife || '-' }}</el-descriptions-item>
        <el-descriptions-item label="存放地点">{{ detail.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请单位">
          {{ detail.unitName || detail.ownerUnitName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.assetStatus)">{{ statusText(detail.assetStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.auditUserName" label="审核人">
          {{ detail.auditUserName }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.auditTime" label="审核时间">
          {{ formatTime(detail.auditTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.auditRemark" label="审核意见" :span="2">
          {{ detail.auditRemark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { apply, audit, getPage, getAssetById } from '@/api/gc'
import { getUnitList } from '@/api/sys'

/** 资产分类（固定列表） */
const categories = [
  { code: 'JJ_01', name: '办公家具' },
  { code: 'IT_01', name: '办公设备' },
  { code: 'DQ_01', name: '电器设备' },
  { code: 'CL_01', name: '车辆' }
]

/** 状态映射 */
const statusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  IN_STOCK: { text: '已通过', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' }
}

const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const categoryName = (code) => (categories.find((c) => c.code === code) || {}).name || code
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  assetName: '',
  categoryCode: '',
  specModel: '',
  brand: '',
  originalValue: undefined,
  purchaseDate: '',
  usefulLife: undefined,
  location: '',
  ownerUnitId: undefined,
  description: '',
  photoUrls: []
})
const photoList = ref([])

const rules = {
  assetName: [{ required: true, message: '请输入资产名称', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '请选择分类', trigger: 'change' }],
  originalValue: [{ required: true, message: '请输入原价值', trigger: 'blur' }],
  purchaseDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
  usefulLife: [{ required: true, message: '请输入使用年限', trigger: 'blur' }],
  ownerUnitId: [{ required: true, message: '请选择权属单位', trigger: 'change' }]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 照片暂以文件名占位，后续接入文件上传接口后替换为真实 URL
      form.photoUrls = photoList.value.map((f) => f.name)
      await apply({ ...form })
      ElMessage.success('入仓申请提交成功')
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
  form.photoUrls = []
  photoList.value = []
}

/** 列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10 })

const loadList = async () => {
  loading.value = true
  try {
    const res = await getPage({ page: query.page, size: query.size })
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
  id: null,
  assetCode: '',
  assetName: '',
  auditResult: 'PASS',
  auditRemark: ''
})

const openAudit = (row) => {
  Object.assign(auditForm, {
    id: row.id,
    assetCode: row.assetCode,
    assetName: row.assetName,
    auditResult: 'PASS',
    auditRemark: ''
  })
  auditVisible.value = true
}

const submitAudit = async () => {
  if (!auditForm.auditResult) {
    ElMessage.warning('请选择审核结果')
    return
  }
  auditing.value = true
  try {
    await audit({
      id: auditForm.id,
      auditResult: auditForm.auditResult,
      auditRemark: auditForm.auditRemark
    })
    ElMessage.success('审核完成')
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
    detail.value = await getAssetById(row.id)
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

.upload-trigger {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #8c939d;
}
</style>
