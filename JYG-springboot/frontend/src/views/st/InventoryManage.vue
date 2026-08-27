<template>
  <div class="inventory-manage-page">
    <!-- 库存预警卡片 -->
    <el-row :gutter="16" class="alert-row">
      <el-col :span="12">
        <el-card shadow="never" class="alert-card alert-short">
          <template #header>
            <div class="alert-header">
              <span>短缺物资预警</span>
              <el-tag type="danger" size="small">{{ shortAlerts.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="!shortAlerts.length" description="暂无短缺物资" :image-size="60" />
          <div v-for="m in shortAlerts" :key="m.id" class="alert-item">
            <span class="alert-name">{{ m.materialName }}</span>
            <span class="alert-desc">当前 {{ m.currentStock }} / 安全 {{ m.safetyStock }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="alert-card alert-over">
          <template #header>
            <div class="alert-header">
              <span>积压物资预警</span>
              <el-tag type="warning" size="small">{{ overAlerts.length }}</el-tag>
            </div>
          </template>
          <el-empty v-if="!overAlerts.length" description="暂无积压物资" :image-size="60" />
          <div v-for="m in overAlerts" :key="m.id" class="alert-item">
            <span class="alert-name">{{ m.materialName }}</span>
            <span class="alert-desc">当前 {{ m.currentStock }} / 上限 {{ m.maxStock }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询区 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="物资">
          <el-select v-model="query.materialId" placeholder="全部" clearable filterable style="width: 180px">
            <el-option v-for="m in materials" :key="m.id" :label="m.materialName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="流水类型">
          <el-select v-model="query.recordType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="timeRange" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 340px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="warning" @click="openStockOut">出库</el-button>
          <el-button type="info" @click="openAdjust">盘点</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 流水列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="materialCode" label="物资编码" width="130" show-overflow-tooltip />
        <el-table-column prop="materialName" label="物资名称" min-width="140" show-overflow-tooltip />
        <el-table-column label="流水类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.recordType)">{{ row.recordTypeLabel || row.recordType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: Number(row.quantity) >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ Number(row.quantity) > 0 ? '+' : '' }}{{ row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="100" align="right">
          <template #default="{ row }">{{ row.unitPrice == null ? '-' : `￥${row.unitPrice}` }}</template>
        </el-table-column>
        <el-table-column label="金额" width="110" align="right">
          <template #default="{ row }">{{ row.totalAmount == null ? '-' : `￥${row.totalAmount}` }}</template>
        </el-table-column>
        <el-table-column prop="businessOrderNo" label="关联单据" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.businessOrderNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="变动前" width="100" align="right">
          <template #default="{ row }">{{ row.stockBefore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="变动后" width="100" align="right">
          <template #default="{ row }">{{ row.stockAfter ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" align="center">
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
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

    <!-- 出库对话框 -->
    <el-dialog v-model="outVisible" title="领用出库" width="680px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="出库明细">
          <el-table :data="outItems" border size="small">
            <el-table-column label="物资" min-width="180">
              <template #default="{ row }">
                <el-select v-model="row.materialId" placeholder="请选择物资" filterable style="width: 100%">
                  <el-option v-for="m in materials" :key="m.id" :label="`${m.materialName}（库存 ${m.currentStock}）`" :value="m.id" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.quantity" :min="0.01" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">
                <el-input-number v-model="row.unitPrice" :min="0" :precision="2" :controls="false" style="width: 100%" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="outItems.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="item-actions">
            <el-button type="primary" plain size="small" @click="outItems.push({ materialId: null, quantity: 1, unitPrice: undefined })">+ 添加物资</el-button>
          </div>
        </el-form-item>
        <el-form-item label="出库原因">
          <el-input v-model="outRemark" type="textarea" :rows="2" maxlength="500" placeholder="如：食堂午餐领用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outVisible = false">取消</el-button>
        <el-button type="primary" :loading="outLoading" @click="submitStockOut">提交出库</el-button>
      </template>
    </el-dialog>

    <!-- 盘点调整对话框 -->
    <el-dialog v-model="adjustVisible" title="库存盘点调整" width="460px" :close-on-click-modal="false">
      <el-form label-width="90px">
        <el-form-item label="物资">
          <el-select v-model="adjustForm.materialId" placeholder="请选择物资" filterable style="width: 100%">
            <el-option v-for="m in materials" :key="m.id" :label="`${m.materialName}（当前库存 ${m.currentStock}）`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="新库存数量">
          <el-input-number v-model="adjustForm.newStock" :min="0" :precision="2" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="2" maxlength="500" placeholder="如：月末盘点调整" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustLoading" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMaterialPage, getInventoryPage, getStockAlerts, stockOut, adjustStock } from '@/api/st'

/** 流水类型下拉 */
const typeOptions = [
  { value: 'IN', label: '入库' },
  { value: 'OUT', label: '出库' },
  { value: 'ADJUST', label: '调整' },
  { value: 'LOSS', label: '报损' }
]
const typeTagTypeMap = {
  IN: 'success',
  OUT: 'warning',
  ADJUST: 'info',
  LOSS: 'danger'
}
const typeTagType = (t) => typeTagTypeMap[String(t || '').toUpperCase()] || 'info'

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 物资下拉数据 */
const materials = ref([])
const loadMaterials = async () => {
  try {
    const res = await getMaterialPage({ page: 1, size: 200 })
    materials.value = res.data || []
  } catch (e) {
    materials.value = []
  }
}

/** 预警卡片 */
const alerts = ref([])
const shortAlerts = computed(() => alerts.value.filter((m) => m.safetyStock != null && Number(m.currentStock) < Number(m.safetyStock)))
const overAlerts = computed(() => alerts.value.filter((m) => m.maxStock != null && Number(m.currentStock) > Number(m.maxStock)))
const loadAlerts = async () => {
  try {
    alerts.value = (await getStockAlerts()) || []
  } catch (e) {
    alerts.value = []
  }
}

/** 查询 */
const query = reactive({
  materialId: undefined,
  recordType: '',
  page: 1,
  size: 10
})
const timeRange = ref([])
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getInventoryPage({
      materialId: query.materialId ?? undefined,
      recordType: query.recordType || undefined,
      startTime: timeRange.value && timeRange.value.length === 2 ? timeRange.value[0] : undefined,
      endTime: timeRange.value && timeRange.value.length === 2 ? timeRange.value[1] : undefined,
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
  query.materialId = undefined
  query.recordType = ''
  timeRange.value = []
  query.page = 1
  loadList()
}

/** 出库 */
const outVisible = ref(false)
const outLoading = ref(false)
const outItems = ref([])
const outRemark = ref('')

const openStockOut = () => {
  outItems.value = [{ materialId: null, quantity: 1, unitPrice: undefined }]
  outRemark.value = ''
  outVisible.value = true
}

const submitStockOut = () => {
  if (!outItems.value.length) {
    ElMessage.warning('请至少添加一项物资')
    return
  }
  const invalid = outItems.value.some((i) => !i.materialId || i.quantity == null)
  if (invalid) {
    ElMessage.warning('请完整填写出库明细（物资、数量）')
    return
  }
  outLoading.value = true
  try {
    stockOut({
      items: outItems.value.map((i) => ({
        materialId: i.materialId,
        quantity: i.quantity,
        unitPrice: i.unitPrice ?? undefined
      })),
      remark: outRemark.value || undefined
    }).then(() => {
      ElMessage.success('出库成功')
      outVisible.value = false
      loadList()
      loadMaterials()
      loadAlerts()
    }).finally(() => {
      outLoading.value = false
    })
  } catch (e) {
    outLoading.value = false
  }
}

/** 盘点调整 */
const adjustVisible = ref(false)
const adjustLoading = ref(false)
const adjustForm = reactive({
  materialId: null,
  newStock: undefined,
  remark: ''
})

const openAdjust = () => {
  Object.assign(adjustForm, { materialId: null, newStock: undefined, remark: '' })
  adjustVisible.value = true
}

const submitAdjust = () => {
  if (!adjustForm.materialId) {
    ElMessage.warning('请选择物资')
    return
  }
  if (adjustForm.newStock == null) {
    ElMessage.warning('请输入新库存数量')
    return
  }
  adjustLoading.value = true
  try {
    adjustStock({
      materialId: adjustForm.materialId,
      newStock: adjustForm.newStock,
      remark: adjustForm.remark || undefined
    }).then(() => {
      ElMessage.success('盘点调整完成')
      adjustVisible.value = false
      loadList()
      loadMaterials()
      loadAlerts()
    }).finally(() => {
      adjustLoading.value = false
    })
  } catch (e) {
    adjustLoading.value = false
  }
}

onMounted(() => {
  loadList()
  loadMaterials()
  loadAlerts()
})
</script>

<style scoped>
.alert-row {
  margin-bottom: 16px;
}

.alert-card {
  height: 100%;
}

.alert-short {
  border-top: 3px solid #f56c6c;
}

.alert-over {
  border-top: 3px solid #e6a23c;
}

.alert-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.alert-item:last-child {
  border-bottom: none;
}

.alert-name {
  font-size: 13px;
  color: #303133;
}

.alert-desc {
  font-size: 12px;
  color: #909399;
}

.query-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.item-actions {
  display: flex;
  align-items: center;
  margin-top: 8px;
}
</style>
