<template>
  <div class="leadership-dashboard">
    <!-- 顶部工具栏 -->
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-inner">
        <div>
          <h2 class="page-title">📊 领导驾驶舱</h2>
          <span class="page-sub">当前月份：{{ currentMonth }} · 数据实时统计</span>
        </div>
        <el-button type="primary" :loading="loading" @click="loadData">
          <el-icon class="refresh-icon"><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </el-card>

    <div v-loading="loading" element-loading-text="数据加载中...">
      <!-- 顶部：KPI 卡片 -->
      <el-row :gutter="16" class="kpi-row">
        <el-col :span="4" v-for="kpi in kpiList" :key="kpi.key">
          <el-card shadow="hover" class="kpi-card">
            <div class="kpi-value">{{ kpi.displayValue }}</div>
            <div class="kpi-label">{{ kpi.label }}</div>
            <div class="kpi-change" :class="'trend-' + kpi.trend">
              <el-icon v-if="kpi.trend === 'up'"><CaretTop /></el-icon>
              <el-icon v-else-if="kpi.trend === 'down'"><CaretBottom /></el-icon>
              <el-icon v-else><Minus /></el-icon>
              <span>{{ Math.abs(kpi.change) }}%</span>
              <span class="kpi-compare">环比上月</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 中部：图表 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="14">
          <el-card shadow="never" class="chart-card">
            <template #header><span>📈 各模块趋势（近7天）</span></template>
            <div ref="trendRef" class="chart-box"></div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="never" class="chart-card">
            <template #header><span>🍩 本月业务分布</span></template>
            <div class="dist-total">
              本月业务总量：<b>{{ distTotal }}</b>
            </div>
            <div ref="distRef" class="chart-box"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 底部：数据明细表 -->
      <el-card shadow="never" class="detail-card">
        <template #header><span>📋 本月核心数据明细</span></template>
        <el-table :data="detailRows" border stripe>
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column label="本月新增" min-width="150" align="right">
            <template #default="{ row }">{{ formatDetail(row.monthNew, row.unit) }}</template>
          </el-table-column>
          <el-table-column label="本月完成" min-width="150" align="right">
            <template #default="{ row }">{{ formatDetail(row.monthDone, row.unit) }}</template>
          </el-table-column>
          <el-table-column label="环比上月" width="130" align="right">
            <template #default="{ row }">
              <span :class="row.lastMonth >= 0 ? 'up-text' : 'down-text'">
                {{ row.lastMonth >= 0 ? '+' : '' }}{{ row.lastMonth }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="同比增长" width="130" align="right">
            <template #default="{ row }">
              <span :class="row.yoy >= 0 ? 'up-text' : 'down-text'">
                {{ row.yoy >= 0 ? '+' : '' }}{{ row.yoy }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'normal' ? 'success' : 'danger'" size="small">
                {{ row.status === 'normal' ? '正常' : '异常' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getLeadershipData } from '@/api/dashboard'

export default {
  name: 'LeadershipDashboard',
  data() {
    return {
      loading: false,
      currentMonth: '',
      kpis: {},
      trends: null,
      distribution: {},
      details: {},
      trendChart: null,
      distChart: null
    }
  },
  computed: {
    /** KPI 展示配置 */
    kpiList() {
      const defs = [
        { key: 'totalAssets', label: '总资产（件）', suffix: '' },
        { key: 'inStockAssets', label: '在仓资产（件）', suffix: '' },
        { key: 'monthCarUses', label: '本月用车（次）', suffix: '' },
        { key: 'occupancyRate', label: '公寓入住率', suffix: '%' },
        { key: 'monthMealReserves', label: '本月预约（人次）', suffix: '' },
        { key: 'pendingApprovals', label: '待审批（条）', suffix: '' }
      ]
      return defs.map((d) => {
        const raw = this.kpis[d.key] || { value: 0, change: 0, trend: 'flat' }
        const value = d.suffix === '%' ? Number(raw.value).toFixed(1) : Math.round(Number(raw.value)).toLocaleString()
        return {
          key: d.key,
          label: d.label,
          value: raw.value,
          displayValue: value + d.suffix,
          change: Number(raw.change || 0),
          trend: raw.trend || 'flat'
        }
      })
    },
    /** 本月业务总量（环形图标题下方展示） */
    distTotal() {
      const d = this.distribution || {}
      return Object.values(d).reduce((s, v) => s + Number(v || 0), 0)
    },
    /** 明细表格数据 */
    detailRows() {
      const defs = [
        { key: 'gc', module: '公物仓', unit: '件', detail: '入仓数/归还数' },
        { key: 'cl', module: '公务用车', unit: '次', detail: '申请数/批准数' },
        { key: 'gy', module: '公寓', unit: '人', detail: '入住数/在住数' },
        { key: 'st', module: '食堂', unit: '元', detail: '采购金额/预约人次' }
      ]
      return defs.map((d) => {
        const r = this.details[d.key] || {}
        return {
          module: d.module,
          unit: d.unit,
          detail: d.detail,
          monthNew: Number(r.monthNew || 0),
          monthDone: Number(r.monthDone || 0),
          lastMonth: Number(r.lastMonth || 0),
          yoy: Number(r.yoy || 0),
          status: r.status || 'normal'
        }
      })
    }
  },
  mounted() {
    this.currentMonth = this.formatMonth()
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) { this.trendChart.dispose(); this.trendChart = null }
    if (this.distChart) { this.distChart.dispose(); this.distChart = null }
  },
  methods: {
    formatMonth() {
      const d = new Date()
      return `${d.getFullYear()}年${d.getMonth() + 1}月`
    },
    async loadData() {
      this.loading = true
      try {
        const data = await getLeadershipData()
        this.kpis = data.kpis || {}
        this.trends = data.trends || null
        this.distribution = data.distribution || {}
        this.details = data.details || {}
        this.$nextTick(() => {
          this.renderTrend()
          this.renderDistribution()
        })
      } catch (e) {
        // 错误已由拦截器统一提示
      } finally {
        this.loading = false
      }
    },
    renderTrend() {
      if (!this.$refs.trendRef) return
      if (!this.trendChart) {
        this.trendChart = echarts.init(this.$refs.trendRef)
      }
      const t = this.trends || { dates: [], gc: [], cl: [], st: [] }
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['公物仓入仓', '用车次数', '食堂预约'], top: 0 },
        grid: { left: 40, right: 20, top: 36, bottom: 28 },
        xAxis: { type: 'category', data: t.dates, boundaryGap: false },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          { name: '公物仓入仓', type: 'line', smooth: true, data: t.gc, itemStyle: { color: '#667eea' } },
          { name: '用车次数', type: 'line', smooth: true, data: t.cl, itemStyle: { color: '#36cfc9' } },
          { name: '食堂预约', type: 'line', smooth: true, data: t.st, itemStyle: { color: '#ff9c6e' } }
        ]
      })
    },
    renderDistribution() {
      if (!this.$refs.distRef) return
      if (!this.distChart) {
        this.distChart = echarts.init(this.$refs.distRef)
      }
      const d = this.distribution || {}
      const names = { gc: '公物仓借用', cl: '用车申请', gy: '公寓入住', st: '食堂预约' }
      const colors = ['#5470c6', '#91cc75', '#909399', '#fa8c16']
      const keys = ['gc', 'cl', 'gy', 'st']
      const data = keys
        .filter((k) => Number(d[k]) > 0)
        .map((k, i) => ({ name: names[k], value: Number(d[k]), itemStyle: { color: colors[i] } }))
      const values = data.map((x) => x.value)
      const total = values.reduce((s, v) => s + v, 0)
      // 最大余数法分配百分比，保证各项相加恰好为 100
      const percents = this.allocatePercent(values, total)
      data.forEach((x, i) => {
        x.percent = percents[i]
      })

      this.distChart.setOption(
        {
          tooltip: { trigger: 'item', formatter: '{b}<br/>数量：{c}<br/>占比：{d}%' },
          legend: {
            orient: 'vertical',
            right: 8,
            top: 'middle',
            icon: 'circle',
            itemWidth: 10,
            itemHeight: 10,
            formatter: (name) => {
              const item = data.find((x) => x.name === name)
              return `${name}  ${item ? item.percent : 0}%`
            }
          },
          series: [
            {
              name: '业务分布',
              type: 'pie',
              radius: ['42%', '66%'],
              center: ['40%', '50%'],
              startAngle: 90,
              itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
              label: { show: false },
              labelLine: { show: false },
              emphasis: {
                itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.35)' }
              },
              data
            }
          ]
        },
        true
      )
    },
    /**
     * 最大余数法：将总数按比例分配为整数百分比，保证总和恰好等于 100
     */
    allocatePercent(values, total) {
      const n = values.length
      if (!n || total <= 0) return values.map(() => 0)
      const exact = values.map((v) => (v * 100) / total)
      const floors = exact.map((p) => Math.floor(p))
      const remainders = exact.map((p, i) => p - floors[i])
      const order = remainders
        .map((r, i) => ({ r, i }))
        .sort((a, b) => b.r - a.r)
        .map((x) => x.i)
      let diff = 100 - floors.reduce((s, v) => s + v, 0)
      for (let k = 0; k < diff && k < n; k++) {
        floors[order[k]]++
      }
      return floors
    },
    handleResize() {
      if (this.trendChart) this.trendChart.resize()
      if (this.distChart) this.distChart.resize()
    },
    formatDetail(value, unit) {
      if (unit === '元') return `¥${Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
      return Number(value).toLocaleString() + (unit || '')
    }
  }
}
</script>

<style scoped>
.leadership-dashboard {
  padding: 0;
}

.toolbar-card {
  margin-bottom: 16px;
}
.toolbar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  margin: 0 0 4px;
  font-size: 20px;
}
.page-sub {
  color: #909399;
  font-size: 13px;
}
.refresh-icon {
  margin-right: 4px;
}

.kpi-row {
  margin-bottom: 16px;
}
.kpi-card {
  text-align: center;
}
.kpi-value {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.kpi-label {
  margin-top: 4px;
  color: #606266;
  font-size: 13px;
}
.kpi-change {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  padding: 2px 8px;
}
.trend-up {
  color: #16a34a;
  background: rgba(22, 163, 74, 0.1);
}
.trend-down {
  color: #dc2626;
  background: rgba(220, 38, 38, 0.1);
}
.trend-flat {
  color: #909399;
  background: rgba(144, 147, 153, 0.12);
}
.kpi-compare {
  font-weight: 400;
  font-size: 12px;
  opacity: 0.85;
}

.chart-row {
  margin-bottom: 16px;
}
.dist-total {
  padding: 0 0 8px;
  font-size: 13px;
  color: #606266;
}
.dist-total b {
  font-size: 18px;
  color: #303133;
  margin-left: 2px;
}
.chart-card .chart-box {
  height: 300px;
}

.detail-card .up-text {
  color: #16a34a;
}
.detail-card .down-text {
  color: #dc2626;
}
</style>
