<template>
  <div class="cost-summary-page">
    <!-- 年月选择 -->
    <el-card shadow="never" class="query-card">
      <el-form :inline="true">
        <el-form-item label="统计年月">
          <el-date-picker
            v-model="yearMonth"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择年月"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
        <el-form-item>
          <span class="tip">点击行可查看该车辆的费用明细</span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 台账表格 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="list"
        border
        stripe
        highlight-current-row
        @row-click="goDetail"
      >
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column label="燃油费" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalFuelCost) }}</template>
        </el-table-column>
        <el-table-column label="维修费" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalRepairCost) }}</template>
        </el-table-column>
        <el-table-column label="保险费" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalInsuranceCost) }}</template>
        </el-table-column>
        <el-table-column label="过路费" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalTollCost) }}</template>
        </el-table-column>
        <el-table-column label="ETC" width="100" align="right">
          <template #default="{ row }">{{ money(row.totalEtcCost) }}</template>
        </el-table-column>
        <el-table-column label="停车费" width="110" align="right">
          <template #default="{ row }">{{ money(row.totalParkingCost) }}</template>
        </el-table-column>
        <el-table-column label="其他" width="100" align="right">
          <template #default="{ row }">{{ money(row.totalOtherCost) }}</template>
        </el-table-column>
        <el-table-column label="总费用" width="120" align="right">
          <template #default="{ row }">
            <span class="total">{{ money(row.totalCost) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="百公里油耗(L)" width="130" align="right">
          <template #default="{ row }">{{ row.avgFuelConsumption ?? '-' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="该月暂无已审批费用" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAllSummary } from '@/api/cl'

const router = useRouter()

const currentMonth = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const yearMonth = ref(currentMonth())
const loading = ref(false)
const list = ref([])

const money = (v) => (v == null ? '0.00' : Number(v).toFixed(2))

const loadList = async () => {
  loading.value = true
  try {
    list.value = (await getAllSummary(yearMonth.value)) || []
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  loadList()
}

/** 点击行跳转到该车辆的费用明细 */
const goDetail = (row) => {
  router.push({ path: '/cl/cost', query: { vehicleId: row.vehicleId } })
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.query-card {
  margin-bottom: 16px;
}

.tip {
  color: #909399;
  font-size: 12px;
}

.total {
  font-weight: 600;
  color: #f56c6c;
}
</style>
