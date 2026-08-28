<template>
  <div class="material-manage-page">
    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="物资编码">
          <el-input v-model="query.materialCode" placeholder="编码模糊查询" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="物资名称">
          <el-input v-model="query.materialName" placeholder="名称模糊查询" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="openSave()">新增物资</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 物资列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="materialCode" label="物资编码" width="140" show-overflow-tooltip />
        <el-table-column prop="materialName" label="物资名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="分类" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="categoryTagType(row.category)">{{ row.categoryLabel || row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="spec" label="规格" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.spec || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" align="center">
          <template #default="{ row }">{{ row.unit || '-' }}</template>
        </el-table-column>
        <el-table-column label="保质期(天)" width="100" align="right">
          <template #default="{ row }">{{ row.shelfLife ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="安全库存" width="100" align="right">
          <template #default="{ row }">{{ row.safetyStock ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="当前库存" width="100" align="right">
          <template #default="{ row }">{{ row.currentStock ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="当前价格" width="110" align="right">
          <template #default="{ row }">{{ row.currentPrice == null ? '-' : `￥${row.currentPrice}` }}</template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="openSave(row)">编辑</el-button>
            <el-button link type="danger" v-hasRole="['BIZ_ADMIN','WAREHOUSE','DEPT_MANAGER']" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="saveVisible" :title="saveForm.id ? '编辑物资' : '新增物资'" width="620px" :close-on-click-modal="false">
      <el-form ref="saveFormRef" :model="saveForm" :rules="saveRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="物资编码" prop="materialCode">
              <el-input v-model="saveForm.materialCode" maxlength="50" placeholder="如 MAT_2026_0006" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物资名称" prop="materialName">
              <el-input v-model="saveForm.materialName" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="saveForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格">
              <el-input v-model="saveForm.spec" maxlength="50" placeholder="如 25kg/袋" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位" prop="unit">
              <el-input v-model="saveForm.unit" maxlength="10" placeholder="如 袋、桶、斤" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="保质期(天)">
              <el-input-number v-model="saveForm.shelfLife" :min="0" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存">
              <el-input-number v-model="saveForm.safetyStock" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存上限">
              <el-input-number v-model="saveForm.maxStock" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前价格">
              <el-input-number v-model="saveForm.currentPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="saveForm.remark" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="saveVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="物资详情" width="640px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="物资编码">{{ detail.materialCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="物资名称">{{ detail.materialName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">
          <el-tag :type="categoryTagType(detail.category)">{{ detail.categoryLabel || detail.category || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="规格">{{ detail.spec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="计量单位">{{ detail.unit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="保质期(天)">{{ detail.shelfLife ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="安全库存">{{ detail.safetyStock ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="库存上限">{{ detail.maxStock ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前库存">{{ detail.currentStock ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="已占用库存">{{ detail.occupiedStock ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前价格">{{ detail.currentPrice == null ? '-' : `￥${detail.currentPrice}` }}</el-descriptions-item>
        <el-descriptions-item label="上次价格">{{ detail.lastPrice == null ? '-' : `￥${detail.lastPrice}` }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialPage, saveMaterial, deleteMaterial, getMaterialDetail } from '@/api/st'

/** 分类选项 */
const categoryOptions = [
  { value: 'FRESH_INGREDIENTS', label: '生鲜食材' },
  { value: 'CONDIMENT', label: '调味品' },
  { value: 'DAILY_GOODS', label: '日用品' }
]
const categoryTagTypeMap = {
  FRESH_INGREDIENTS: 'success',
  CONDIMENT: 'warning',
  DAILY_GOODS: 'info'
}
const categoryTagType = (c) => categoryTagTypeMap[String(c || '').toUpperCase()] || 'primary'

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 查询 */
const query = reactive({
  materialCode: '',
  materialName: '',
  category: '',
  page: 1,
  size: 10
})
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMaterialPage({
      materialCode: query.materialCode || undefined,
      materialName: query.materialName || undefined,
      category: query.category || undefined,
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
  query.materialCode = ''
  query.materialName = ''
  query.category = ''
  query.page = 1
  loadList()
}

/** 新增/编辑 */
const saveVisible = ref(false)
const saving = ref(false)
const saveFormRef = ref()
const saveForm = reactive({
  id: null,
  materialCode: '',
  materialName: '',
  category: '',
  spec: '',
  unit: '',
  shelfLife: undefined,
  safetyStock: 0,
  maxStock: 1000,
  currentPrice: undefined,
  remark: ''
})

const saveRules = {
  materialCode: [{ required: true, message: '请输入物资编码', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入物资名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }]
}

const openSave = (row) => {
  Object.assign(saveForm, {
    id: row ? row.id : null,
    materialCode: row ? row.materialCode : '',
    materialName: row ? row.materialName : '',
    category: row ? row.category : '',
    spec: row ? row.spec : '',
    unit: row ? row.unit : '',
    shelfLife: row ? row.shelfLife : undefined,
    safetyStock: row ? row.safetyStock : 0,
    maxStock: row ? row.maxStock : 1000,
    currentPrice: row ? row.currentPrice : undefined,
    remark: row ? row.remark : ''
  })
  saveFormRef.value && saveFormRef.value.clearValidate()
  saveVisible.value = true
}

const submitSave = () => {
  saveFormRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      await saveMaterial({
        id: saveForm.id ?? undefined,
        materialCode: saveForm.materialCode.trim(),
        materialName: saveForm.materialName.trim(),
        category: saveForm.category,
        spec: saveForm.spec || undefined,
        unit: saveForm.unit.trim(),
        shelfLife: saveForm.shelfLife ?? undefined,
        safetyStock: saveForm.safetyStock ?? 0,
        maxStock: saveForm.maxStock ?? 1000,
        currentPrice: saveForm.currentPrice ?? undefined,
        remark: saveForm.remark || undefined
      })
      ElMessage.success(saveForm.id ? '编辑成功' : '新增成功')
      saveVisible.value = false
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
  ElMessageBox.confirm(`确定删除物资「${row.materialName}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteMaterial(row.id)
      ElMessage.success('删除成功')
      if (list.value.length === 1 && query.page > 1) {
        query.page -= 1
      }
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    }
  }).catch(() => {})
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getMaterialDetail(row.id)
  } catch (e) {
    detailVisible.value = false
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
</style>
