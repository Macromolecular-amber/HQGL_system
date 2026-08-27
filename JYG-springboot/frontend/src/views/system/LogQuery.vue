<template>
  <div class="log-query">
    <h2>📋 操作日志</h2>

    <!-- 筛选条件 -->
    <div class="filters">
      <el-input
        v-model="query.username"
        placeholder="用户名"
        clearable
        style="width:150px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="query.module" placeholder="模块" clearable @change="handleSearch" style="width:150px">
        <el-option label="全部模块" value="" />
        <el-option label="公物仓" value="GC" />
        <el-option label="用车" value="CL" />
        <el-option label="公寓" value="GY" />
        <el-option label="食堂" value="ST" />
        <el-option label="平台" value="SYS" />
        <el-option label="支付" value="PAY" />
      </el-select>
      <el-select v-model="query.operationType" placeholder="操作类型" clearable @change="handleSearch" style="width:150px">
        <el-option label="全部" value="" />
        <el-option label="登录" value="LOGIN" />
        <el-option label="查询" value="QUERY" />
        <el-option label="新增" value="ADD" />
        <el-option label="编辑" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="审批" value="APPROVE" />
        <el-option label="导出" value="EXPORT" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD HH:mm:ss"
        @change="handleSearch"
        style="width:360px"
      />
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 日志表格 -->
    <el-table v-loading="loading" :data="logs" border stripe style="width:100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="module" label="模块" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getModuleLabel(row.module) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationType" label="操作类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.operationType)" size="small">{{ getTypeLabel(row.operationType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDesc" label="操作描述" min-width="200" />
      <el-table-column prop="clientIp" label="IP" width="140" />
      <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center">
        <template #default="{ row }">
          <span :style="{ color: row.costTime > 1000 ? 'red' : 'inherit' }">{{ row.costTime || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:page-size="query.size"
        v-model:current-page="query.page"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="loadLogs"
      />
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.realName }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ getModuleLabel(detailData.module) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ getTypeLabel(detailData.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">{{ detailData.operationDesc }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detailData.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2" v-if="detailData.requestParams">{{ detailData.requestParams }}</el-descriptions-item>
        <el-descriptions-item label="IP">{{ detailData.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detailData.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="响应码">{{ detailData.responseCode }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detailData.exceptionMsg">{{ detailData.exceptionMsg }}</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { getLogPage } from '@/api/log'

export default {
  name: 'LogQuery',
  setup() {
    const loading = ref(false)
    const logs = ref([])
    const total = ref(0)
    const detailVisible = ref(false)
    const detailData = ref({})
    const dateRange = ref([])

    const query = reactive({
      username: '',
      module: '',
      operationType: '',
      page: 1,
      size: 20
    })

    // 加载日志（request 拦截器已解包 Result，直接返回 PageResult）
    const loadLogs = async () => {
      loading.value = true
      try {
        const params = {
          page: query.page,
          size: query.size,
          username: query.username || undefined,
          module: query.module || undefined,
          operationType: query.operationType || undefined
        }
        if (dateRange.value && dateRange.value.length === 2) {
          params.startTime = dateRange.value[0]
          params.endTime = dateRange.value[1]
        }
        const res = await getLogPage(params)
        logs.value = res.data || []
        total.value = res.total || 0
      } catch (e) {
        // 错误已由拦截器统一提示
      } finally {
        loading.value = false
      }
    }

    const handleSearch = () => {
      query.page = 1
      loadLogs()
    }

    // 切换每页条数：回到第一页重新加载（当前页码由 v-model 更新）
    const handleSizeChange = () => {
      query.page = 1
      loadLogs()
    }

    const handleReset = () => {
      query.username = ''
      query.module = ''
      query.operationType = ''
      dateRange.value = []
      query.page = 1
      loadLogs()
    }

    const showDetail = (row) => {
      detailData.value = row
      detailVisible.value = true
    }

    const getModuleLabel = (module) => {
      const map = { GC: '公物仓', CL: '用车', GY: '公寓', ST: '食堂', SYS: '平台', PAY: '支付' }
      return map[module] || module
    }

    const getTypeTag = (type) => {
      const map = { LOGIN: 'info', QUERY: 'primary', ADD: 'success', UPDATE: 'warning', DELETE: 'danger', APPROVE: 'success', EXPORT: 'info' }
      return map[type] || 'info'
    }

    const getTypeLabel = (type) => {
      const map = { LOGIN: '登录', QUERY: '查询', ADD: '新增', UPDATE: '编辑', DELETE: '删除', APPROVE: '审批', EXPORT: '导出' }
      return map[type] || type
    }

    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN', { hour12: false })
    }

    onMounted(() => {
      loadLogs()
    })

    return {
      loading,
      logs,
      total,
      query,
      dateRange,
      detailVisible,
      detailData,
      loadLogs,
      handleSearch,
      handleSizeChange,
      handleReset,
      showDetail,
      getModuleLabel,
      getTypeTag,
      getTypeLabel,
      formatTime
    }
  }
}
</script>

<style scoped>
.log-query {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.log-query h2 {
  margin: 0 0 16px 0;
  font-size: 20px;
}

.filters {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
}
</style>
