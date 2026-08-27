<template>
  <div class="meal-statistics-page">
    <!-- 统计查询条件 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true">
        <el-form-item label="日期">
          <el-date-picker v-model="queryDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 160px" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="queryMealType" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="t in mealTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button type="info" @click="openDetail">查看预约明细</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-num stat-total">{{ detailStat?.totalCount ?? '-' }}</div>
          <div class="stat-label">总预约人数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-num stat-breakfast">{{ breakfastCount ?? '-' }}</div>
          <div class="stat-label">早餐</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-num stat-lunch">{{ lunchCount ?? '-' }}</div>
          <div class="stat-label">午餐</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-num stat-dinner">{{ dinnerCount ?? '-' }}</div>
          <div class="stat-label">晚餐</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 按单位统计 -->
    <el-card shadow="never">
      <template #header>
        <span>按单位统计（{{ queryDate }}）</span>
      </template>
      <el-table v-loading="loading" :data="unitStats" border stripe>
        <el-table-column prop="unitName" label="单位名称" min-width="200">
          <template #default="{ row }">{{ row.unitName || '未知单位' }}</template>
        </el-table-column>
        <el-table-column prop="count" label="预约人数" width="120" align="center" />
        <el-table-column label="占比" min-width="240">
          <template #default="{ row }">
            <el-progress :percentage="percent(row.count)" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!unitStats.length" description="暂无统计数据" :image-size="60" />
    </el-card>

    <!-- 预约明细对话框 -->
    <el-dialog v-model="detailVisible" :title="`${queryDate} 预约明细`" width="720px">
      <el-table :data="detailList" size="small" border max-height="420">
        <el-table-column prop="userName" label="预约人" width="110">
          <template #default="{ row }">{{ row.userName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unitName" label="单位" min-width="160">
          <template #default="{ row }">{{ row.unitName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="mealDate" label="日期" width="110" />
        <el-table-column label="餐次" width="90" align="center">
          <template #default="{ row }">{{ row.mealTypeLabel || row.mealType }}</template>
        </el-table-column>
        <el-table-column prop="mealCount" label="人数" width="70" align="center" />
        <el-table-column label="预约时间" width="160">
          <template #default="{ row }">{{ formatTime(row.reservationTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMealStatistics, getMealByDate } from '@/api/st'

const mealTypeOptions = [
  { value: 'BREAKFAST', label: '早餐' },
  { value: 'LUNCH', label: '午餐' },
  { value: 'DINNER', label: '晚餐' }
]

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')
const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const queryDate = ref(today())
const queryMealType = ref('')
const loading = ref(false)

/** 统计结果 */
const allStat = ref(null)        // 全部餐次
const breakfastCount = ref(null)
const lunchCount = ref(null)
const dinnerCount = ref(null)

/** 当前选中查询条件下的统计（餐次筛选时用对应餐次数据） */
const detailStat = computed(() => {
  if (!queryMealType.value) return allStat.value
  return {
    totalCount: queryMealType.value === 'BREAKFAST' ? breakfastCount.value
      : queryMealType.value === 'LUNCH' ? lunchCount.value
      : dinnerCount.value
  }
})

const unitStats = computed(() => {
  if (queryMealType.value) return []
  return allStat.value?.unitStatistics || []
})

const percent = (count) => {
  const total = allStat.value?.totalCount || 0
  if (!total) return 0
  return Number(((count || 0) / total) * 100).toFixed(1) * 1
}

const handleQuery = async () => {
  if (!queryDate.value) {
    return
  }
  loading.value = true
  try {
    // 全部餐次
    allStat.value = await getMealStatistics({ mealDate: queryDate.value })
    // 各餐次人数
    const [bf, lh, dn] = await Promise.all([
      getMealStatistics({ mealDate: queryDate.value, mealType: 'BREAKFAST' }),
      getMealStatistics({ mealDate: queryDate.value, mealType: 'LUNCH' }),
      getMealStatistics({ mealDate: queryDate.value, mealType: 'DINNER' })
    ])
    breakfastCount.value = bf.totalCount
    lunchCount.value = lh.totalCount
    dinnerCount.value = dn.totalCount
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 预约明细 */
const detailVisible = ref(false)
const detailList = ref([])

const openDetail = async () => {
  if (!queryDate.value) {
    return
  }
  detailVisible.value = true
  try {
    detailList.value = (await getMealByDate(queryDate.value)) || []
  } catch (e) {
    detailList.value = []
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.query-card {
  margin-bottom: 16px;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
}

.stat-num {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-total {
  color: #409eff;
}

.stat-breakfast {
  color: #e6a23c;
}

.stat-lunch {
  color: #409eff;
}

.stat-dinner {
  color: #67c23a;
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}
</style>
