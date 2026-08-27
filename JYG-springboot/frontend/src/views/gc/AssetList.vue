<template>
  <div class="asset-list-page">
    <!-- 查询条件 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true" :model="query">
        <el-form-item label="资产名称">
          <el-input
            v-model="query.assetName"
            placeholder="请输入资产名称"
            clearable
            style="width: 170px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.categoryCode" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="c in categories" :key="c.code" :label="c.name" :value="c.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.assetStatus" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="原值区间">
          <el-input-number
            v-model="query.startValue"
            :min="0"
            :controls="false"
            placeholder="下限"
            style="width: 110px"
          />
          <span class="range-sep">-</span>
          <el-input-number
            v-model="query.endValue"
            :min="0"
            :controls="false"
            placeholder="上限"
            style="width: 110px"
          />
        </el-form-item>
        <el-form-item label="权属单位">
          <el-select v-model="query.ownerUnitId" placeholder="全部" clearable filterable style="width: 190px">
            <el-option v-for="u in units" :key="u.id" :label="u.unitName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 资产列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column prop="assetCode" label="资产编号" width="180" show-overflow-tooltip />
        <el-table-column prop="assetName" label="资产名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="specModel" label="规格型号" min-width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.specModel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="originalValue" label="原值(元)" width="110" align="right" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.assetStatus)">{{ statusText(row.assetStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="存放地点" min-width="110">
          <template #default="{ row }">{{ row.location || '-' }}</template>
        </el-table-column>
        <el-table-column prop="ownerUnitName" label="权属单位" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.ownerUnitName || '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">查看详情</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="资产详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="资产编号">{{ detail.assetCode }}</el-descriptions-item>
        <el-descriptions-item label="资产名称">{{ detail.assetName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detail.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ detail.specModel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ detail.brand || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数量">{{ detail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="原值(元)">{{ detail.originalValue }}</el-descriptions-item>
        <el-descriptions-item label="残值率(%)">{{ detail.residualRate }}</el-descriptions-item>
        <el-descriptions-item label="当前净值(元)">{{ detail.currentValue }}</el-descriptions-item>
        <el-descriptions-item label="累计折旧(元)">{{ detail.accumulatedDepreciation }}</el-descriptions-item>
        <el-descriptions-item label="购置日期">{{ detail.purchaseDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="使用年限(年)">{{ detail.usefulLife || '-' }}</el-descriptions-item>
        <el-descriptions-item label="折旧方法">{{ detail.depreciationMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.assetStatus)">{{ statusText(detail.assetStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="存放地点">{{ detail.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="权属单位">
          {{ detail.unitName || detail.ownerUnitName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前使用单位">{{ detail.currentUseUnitId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="仓库ID">{{ detail.warehouseId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="二维码">{{ detail.qrCodeUrl || '-' }}</el-descriptions-item>
        <el-descriptions-item label="RFID标签">{{ detail.rfidTag || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detail.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核人">{{ detail.auditUser || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ formatTime(detail.auditTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ detail.auditRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入仓时间" :span="2">{{ formatTime(detail.inStockTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getList, getDetail } from '@/api/gc'
import { getUnitList } from '@/api/sys'

/** 资产分类（固定列表） */
const categories = [
  { code: 'JJ_01', name: '办公家具' },
  { code: 'IT_01', name: '办公设备' },
  { code: 'DQ_01', name: '电器设备' },
  { code: 'CL_01', name: '车辆' }
]

/** 状态筛选选项 */
const statusOptions = [
  { value: 'IN_STOCK', label: '在仓' },
  { value: 'BORROWED', label: '已借用' },
  { value: 'TRANSFERRED', label: '已调剂' },
  { value: 'DISPOSED', label: '已处置' },
  { value: 'REPAIRING', label: '维修中' }
]

/** 状态展示映射（含入仓流程状态） */
const statusMap = {
  PENDING: { text: '待审核', type: 'warning' },
  IN_STOCK: { text: '在仓', type: 'success' },
  REJECTED: { text: '已驳回', type: 'danger' },
  BORROWED: { text: '已借用', type: 'primary' },
  TRANSFERRED: { text: '已调剂', type: 'info' },
  DISPOSED: { text: '已处置', type: 'info' },
  REPAIRING: { text: '维修中', type: 'danger' }
}

const statusText = (s) => (statusMap[s] || { text: s }).text
const statusType = (s) => (statusMap[s] || { type: 'info' }).type
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 查询条件 */
const query = reactive({
  assetName: '',
  categoryCode: '',
  assetStatus: '',
  startValue: undefined,
  endValue: undefined,
  ownerUnitId: undefined,
  page: 1,
  size: 10
})

const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getList({
      assetName: query.assetName || undefined,
      categoryCode: query.categoryCode || undefined,
      assetStatus: query.assetStatus || undefined,
      startValue: query.startValue ?? undefined,
      endValue: query.endValue ?? undefined,
      ownerUnitId: query.ownerUnitId ?? undefined,
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
  query.assetName = ''
  query.categoryCode = ''
  query.assetStatus = ''
  query.startValue = undefined
  query.endValue = undefined
  query.ownerUnitId = undefined
  query.page = 1
  loadList()
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await getDetail(row.id)
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

.range-sep {
  margin: 0 6px;
  color: #909399;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
