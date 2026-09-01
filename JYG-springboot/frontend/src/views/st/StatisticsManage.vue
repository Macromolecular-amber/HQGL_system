<template>
  <div class="statistics-manage-page">
    <el-card shadow="never" class="tab-card">
      <el-radio-group v-model="tabType">
        <el-radio-button value="waste">餐余管理</el-radio-button>
        <el-radio-button value="consume">消费统计</el-radio-button>
        <el-radio-button value="purchase">采购统计</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- Tab1: 餐余管理 -->
    <template v-if="tabType === 'waste'">
      <el-card shadow="never" class="block-card">
        <template #header><span>录入餐余</span></template>
        <el-form :inline="true" :model="wasteForm">
          <el-form-item label="日期">
            <el-date-picker v-model="wasteForm.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 150px" />
          </el-form-item>
          <el-form-item label="餐次">
            <el-select v-model="wasteForm.mealType" placeholder="请选择" clearable style="width: 110px">
              <el-option v-for="t in mealTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="重量(kg)">
            <el-input-number v-model="wasteForm.wasteWeight" :min="0.01" :precision="2" :controls="false" style="width: 110px" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="wasteForm.wasteType" placeholder="请选择" clearable style="width: 110px">
              <el-option v-for="t in wasteTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理方式">
            <el-select v-model="wasteForm.disposalMethod" placeholder="请选择" clearable style="width: 110px">
              <el-option v-for="t in disposalOptions" :key="t.value" :label="t.label" :value="t.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理人">
            <el-input v-model="wasteForm.disposalPerson" maxlength="50" style="width: 110px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="recording" @click="submitWaste">录入</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-row :gutter="16" class="block-row">
        <el-col :span="4">
          <el-card shadow="never" class="stat-card waste-stat">
            <div class="stat-num">{{ wasteStat?.totalWeight ?? '-' }}</div>
            <div class="stat-label">总餐余量（kg）</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="never" class="stat-card waste-stat">
            <div class="stat-num">{{ wasteStat?.avgWeightPerDay ?? '-' }}</div>
            <div class="stat-label">日均餐余量（kg）</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header><span>按餐次分布</span></template>
            <div ref="wastePieRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header><span>按处理方式分布</span></template>
            <div ref="wasteDisposalRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never">
        <template #header><span>餐余记录</span></template>
        <el-table v-loading="wasteLoading" :data="wasteList" border stripe>
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column label="餐次" width="90" align="center">
            <template #default="{ row }">{{ row.mealTypeLabel || '-' }}</template>
          </el-table-column>
          <el-table-column prop="wasteWeight" label="重量(kg)" width="100" align="right" />
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">{{ row.wasteTypeLabel || '-' }}</template>
          </el-table-column>
          <el-table-column label="处理方式" width="100" align="center">
            <template #default="{ row }">{{ row.disposalMethodLabel || '-' }}</template>
          </el-table-column>
          <el-table-column prop="disposalPerson" label="处理人" width="110" align="center">
            <template #default="{ row }">{{ row.disposalPerson || '-' }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '-' }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="wasteQuery.page"
            v-model:page-size="wasteQuery.size"
            :total="wasteTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadWasteList"
            @current-change="loadWasteList"
          />
        </div>
      </el-card>
    </template>

    <!-- Tab2: 消费统计 -->
    <template v-else-if="tabType === 'consume'">
      <el-card shadow="never" class="query-card">
        <el-form :inline="true">
          <el-form-item label="统计周期">
            <el-date-picker v-model="consumeRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadConsumeStat">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-row :gutter="16" class="block-row">
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num">￥{{ formatAmount(consumeStat?.totalAmount) }}</div>
            <div class="stat-label">总消费金额</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num">{{ consumeStat?.totalCount ?? '-' }}</div>
            <div class="stat-label">消费笔数</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="stat-card">
            <div class="stat-num">￥{{ formatAmount(consumeStat?.avgAmountPerPerson) }}</div>
            <div class="stat-label">人均消费</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="16" class="block-row">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span>每日消费趋势</span></template>
            <div ref="consumeLineRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span>消费金额（按日）</span></template>
            <div ref="consumeBarRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Tab3: 采购统计 -->
    <template v-else>
      <el-card shadow="never" class="query-card">
        <el-form :inline="true">
          <el-form-item label="统计周期">
            <el-date-picker v-model="purchaseRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 260px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadPurchaseStat">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-card shadow="never" class="stat-card stat-full">
        <div class="stat-num">￥{{ formatAmount(purchaseStat?.totalPurchaseAmount) }}</div>
        <div class="stat-label">采购总金额</div>
      </el-card>
      <el-row :gutter="16" class="block-row">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span>采购金额月度趋势</span></template>
            <div ref="purchaseMonthlyRef" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header><span>采购金额前5物资</span></template>
            <div ref="purchaseTopRef" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { recordWaste, getWastePage, getWasteStatistics, getConsumeStatistics, getPurchaseStatistics } from '@/api/st'

const tabType = ref('waste')

const mealTypeOptions = [
  { value: 'BREAKFAST', label: '早餐' },
  { value: 'LUNCH', label: '午餐' },
  { value: 'DINNER', label: '晚餐' }
]
const wasteTypeOptions = [
  { value: 'FOOD', label: '食物' },
  { value: 'PACKAGING', label: '包装' },
  { value: 'OTHER', label: '其他' }
]
const disposalOptions = [
  { value: 'COMPOST', label: '堆肥' },
  { value: 'FEED', label: '饲料' },
  { value: 'WASTE', label: '废弃物' }
]

const formatAmount = (v) => (v == null ? '0.00' : Number(v).toFixed(2))
const pad = (n) => String(n).padStart(2, '0')
const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const daysAgo = (n) => {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 图表实例管理 */
const charts = {}
const chartRefs = {}

const initChart = (key, refName, option) => {
  chartRefs[key] = refName
  nextTick(() => {
    const el = refName.value
    if (!el) return
    if (charts[key]) {
      charts[key].dispose()
    }
    const chart = echarts.init(el)
    chart.setOption(option)
    charts[key] = chart
  })
}

onBeforeUnmount(() => {
  Object.values(charts).forEach((c) => c && c.dispose())
})

/** ============ Tab1 餐余管理 ============ */
const wasteForm = reactive({
  recordDate: todayStr(),
  mealType: 'LUNCH',
  wasteWeight: 10,
  wasteType: 'FOOD',
  disposalMethod: 'COMPOST',
  disposalPerson: ''
})
const recording = ref(false)

const submitWaste = async () => {
  if (!wasteForm.recordDate) {
    ElMessage.warning('请选择日期')
    return
  }
  if (wasteForm.wasteWeight == null || wasteForm.wasteWeight <= 0) {
    ElMessage.warning('请输入餐余重量')
    return
  }
  recording.value = true
  try {
    await recordWaste({
      recordDate: wasteForm.recordDate,
      mealType: wasteForm.mealType || undefined,
      wasteWeight: wasteForm.wasteWeight,
      wasteType: wasteForm.wasteType || undefined,
      disposalMethod: wasteForm.disposalMethod || undefined,
      disposalPerson: wasteForm.disposalPerson || undefined
    })
    ElMessage.success('录入成功')
    loadWasteList()
    loadWasteStat()
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    recording.value = false
  }
}

const wasteQuery = reactive({ page: 1, size: 10 })
const wasteLoading = ref(false)
const wasteList = ref([])
const wasteTotal = ref(0)

const loadWasteList = async () => {
  wasteLoading.value = true
  try {
    const res = await getWastePage({ page: wasteQuery.page, size: wasteQuery.size })
    wasteList.value = res.data || []
    wasteTotal.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    wasteLoading.value = false
  }
}

const wasteStat = ref(null)
const loadWasteStat = async () => {
  try {
    const params = { periodType: 'MONTH', startDate: daysAgo(30), endDate: todayStr() }
    wasteStat.value = await getWasteStatistics(params)
    renderWastePie(wasteStat.value)
    renderWasteDisposal(wasteStat.value)
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

const wastePieRef = ref()
const renderWastePie = (stat) => {
  const byMeal = stat?.byMealType || {}
  const mealLabels = { BREAKFAST: '早餐', LUNCH: '午餐', DINNER: '晚餐' }
  const data = Object.entries(byMeal).map(([k, v]) => ({ name: mealLabels[k] || k, value: Number(v) }))
  initChart('wastePie', wastePieRef, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} kg ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 8,
      top: 'middle',
      icon: 'circle',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 16 },
      formatter: (name) => {
        const item = data.find((x) => x.name === name)
        return `${name}  ${item ? item.value : 0}kg`
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '62%'],
      center: ['36%', '50%'],
      startAngle: 90,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      labelLine: { show: false },
      data
    }]
  })
}

const wasteDisposalRef = ref()
const renderWasteDisposal = (stat) => {
  const byDisposal = stat?.byDisposalMethod || {}
  const disposalLabels = { COMPOST: '堆肥', FEED: '饲料', WASTE: '废弃物' }
  const data = Object.entries(byDisposal).map(([k, v]) => ({ name: disposalLabels[k] || k, value: Number(v) }))
  initChart('wasteDisposal', wasteDisposalRef, {
    color: ['#67c23a', '#e6a23c', '#909399'],
    tooltip: { trigger: 'item', formatter: '{b}: {c} kg ({d}%)' },
    legend: {
      orient: 'vertical',
      right: 8,
      top: 'middle',
      icon: 'circle',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 16 },
      formatter: (name) => {
        const item = data.find((x) => x.name === name)
        return `${name}  ${item ? item.value : 0}kg`
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '62%'],
      center: ['36%', '50%'],
      startAngle: 90,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      labelLine: { show: false },
      data
    }]
  })
}

/** ============ Tab2 消费统计 ============ */
const consumeRange = ref([daysAgo(30), todayStr()])
const consumeStat = ref(null)
const consumeLineRef = ref()
const consumeBarRef = ref()

const loadConsumeStat = async () => {
  if (!consumeRange.value || consumeRange.value.length !== 2) return
  try {
    consumeStat.value = await getConsumeStatistics({
      periodType: 'MONTH',
      startDate: consumeRange.value[0],
      endDate: consumeRange.value[1]
    })
    renderConsumeCharts(consumeStat.value)
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

const renderConsumeCharts = (stat) => {
  const trend = stat?.dailyTrend || []
  const dates = trend.map((d) => d.date)
  const amounts = trend.map((d) => Number(d.totalAmount))
  // 折线图：每日消费趋势
  initChart('consumeLine', consumeLineRef, {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '元' },
    series: [{ type: 'line', smooth: true, areaStyle: { opacity: 0.15 }, data: amounts }]
  })
  // 柱状图：按日消费金额排序
  const sorted = [...trend].sort((a, b) => Number(b.totalAmount) - Number(a.totalAmount))
  initChart('consumeBar', consumeBarRef, {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: sorted.map((d) => d.date) },
    yAxis: { type: 'value', name: '元' },
    series: [{ type: 'bar', data: sorted.map((d) => Number(d.totalAmount)), itemStyle: { color: '#409eff' } }]
  })
}

/** ============ Tab3 采购统计 ============ */
const purchaseRange = ref([daysAgo(90), todayStr()])
const purchaseStat = ref(null)
const purchaseMonthlyRef = ref()
const purchaseTopRef = ref()

const loadPurchaseStat = async () => {
  if (!purchaseRange.value || purchaseRange.value.length !== 2) return
  try {
    purchaseStat.value = await getPurchaseStatistics({
      periodType: 'MONTH',
      startDate: purchaseRange.value[0],
      endDate: purchaseRange.value[1]
    })
    renderPurchaseCharts(purchaseStat.value)
  } catch (e) {
    // 错误已由拦截器统一提示
  }
}

const renderPurchaseCharts = (stat) => {
  const monthly = stat?.monthlyTrend || []
  const top = stat?.topMaterials || []
  // 月度趋势柱状图
  initChart('purchaseMonthly', purchaseMonthlyRef, {
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: monthly.map((m) => m.month) },
    yAxis: { type: 'value', name: '元' },
    series: [{ type: 'bar', data: monthly.map((m) => Number(m.totalAmount)), itemStyle: { color: '#67c23a' } }]
  })
  // Top5 物资条形图
  initChart('purchaseTop', purchaseTopRef, {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 100, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value', name: '元' },
    yAxis: { type: 'category', data: top.map((t) => t.materialName) },
    series: [{ type: 'bar', data: top.map((t) => Number(t.totalAmount)), itemStyle: { color: '#e6a23c' } }]
  })
}

onMounted(() => {
  loadWasteList()
  loadWasteStat()
  loadConsumeStat()
  loadPurchaseStat()
})

// Tab 切换后重新渲染对应图表（v-if 场景下挂载延迟）
watch(tabType, () => {
  nextTick(() => {
    if (tabType.value === 'waste') {
      renderWastePie(wasteStat.value)
      renderWasteDisposal(wasteStat.value)
    } else if (tabType.value === 'consume') {
      renderConsumeCharts(consumeStat.value)
    } else {
      renderPurchaseCharts(purchaseStat.value)
    }
  })
})
</script>

<style scoped>
.tab-card {
  margin-bottom: 16px;
}

.block-card {
  margin-bottom: 16px;
}

.block-row {
  margin-bottom: 16px;
}

.query-card {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  height: 100%;
}

.stat-full {
  margin-bottom: 16px;
  text-align: center;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  line-height: 1.2;
}

.stat-label {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

/* 餐余统计卡片：内容在显示框内垂直水平居中，放大字体 */
.waste-stat {
  display: flex;
  flex-direction: column;
}
.waste-stat :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.waste-stat .stat-num {
  font-size: 46px;
  line-height: 1.1;
}
.waste-stat .stat-label {
  margin-top: 12px;
  font-size: 16px;
}

.chart-card {
  height: 100%;
}

.chart {
  height: 300px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
