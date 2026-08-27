<template>
  <div class="meal-reserve-page">
    <!-- 预约表单 -->
    <el-card shadow="never" class="form-card">
      <template #header>
        <span>预约订餐</span>
      </template>
      <el-form ref="reserveFormRef" :model="reserveForm" :rules="reserveRules" inline>
        <el-form-item label="就餐日期" prop="mealDate">
          <el-date-picker v-model="reserveForm.mealDate" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledDate" placeholder="选择日期" style="width: 160px" />
        </el-form-item>
        <el-form-item label="餐次" prop="mealType">
          <el-radio-group v-model="reserveForm.mealType">
            <el-radio-button value="BREAKFAST">早餐</el-radio-button>
            <el-radio-button value="LUNCH">午餐</el-radio-button>
            <el-radio-button value="DINNER">晚餐</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="就餐人数" prop="mealCount">
          <el-input-number v-model="reserveForm.mealCount" :min="1" :max="50" :controls="false" style="width: 100px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reserveForm.remark" maxlength="500" placeholder="选填" style="width: 240px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="reserving" @click="submitReserve">提交预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 我的预约记录 -->
    <el-card shadow="never">
      <template #header>
        <span>我的预约记录</span>
      </template>
      <el-table v-loading="loading" :data="list" border stripe>
        <el-table-column label="日期" width="120">
          <template #default="{ row }">{{ row.mealDate }}</template>
        </el-table-column>
        <el-table-column label="餐次" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="mealTagType(row.mealType)">{{ row.mealTypeLabel || row.mealType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mealCount" label="人数" width="90" align="center" />
        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">{{ formatTime(row.reservationTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="!row.isCancelled" type="success">有效</el-tag>
            <el-tag v-else type="info">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button v-if="canCancel(row)" link type="danger" @click="handleCancel(row)">取消</el-button>
            <span v-else>-</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reserveMeal, cancelMeal, getMealPage } from '@/api/st'

/** 餐次开餐时间（用于取消时限判断） */
const mealTimeMap = { BREAKFAST: 7, LUNCH: 12, DINNER: 18 }
const mealTagTypeMap = { BREAKFAST: 'warning', LUNCH: 'primary', DINNER: 'success' }
const mealTagType = (t) => mealTagTypeMap[String(t || '').toUpperCase()] || 'info'

const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const pad = (n) => String(n).padStart(2, '0')
const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}
const disabledDate = (date) => date.getTime() < new Date(new Date().toDateString()).getTime()

/** 预约表单 */
const reserving = ref(false)
const reserveFormRef = ref()
const reserveForm = reactive({
  mealDate: today(),
  mealType: 'LUNCH',
  mealCount: 1,
  remark: ''
})

const reserveRules = {
  mealDate: [{ required: true, message: '请选择就餐日期', trigger: 'change' }],
  mealType: [{ required: true, message: '请选择餐次', trigger: 'change' }],
  mealCount: [{ required: true, message: '请输入就餐人数', trigger: 'blur' }]
}

const submitReserve = () => {
  reserveFormRef.value.validate(async (valid) => {
    if (!valid) return
    reserving.value = true
    try {
      await reserveMeal({
        mealDate: reserveForm.mealDate,
        mealType: reserveForm.mealType,
        mealCount: reserveForm.mealCount || 1,
        remark: reserveForm.remark || undefined
      })
      ElMessage.success('预约成功')
      reserveForm.remark = ''
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    } finally {
      reserving.value = false
    }
  })
}

/** 我的预约记录 */
const query = reactive({ page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)

const loadList = async () => {
  loading.value = true
  try {
    const res = await getMealPage({ page: query.page, size: query.size })
    list.value = res.data || []
    total.value = res.total || 0
  } catch (e) {
    // 错误已由拦截器统一提示
  } finally {
    loading.value = false
  }
}

/** 是否可取消：未取消且当前时间在餐次开始前1小时之前 */
const canCancel = (row) => {
  if (row.isCancelled) return false
  const mealHour = mealTimeMap[String(row.mealType || '').toUpperCase()]
  if (mealHour == null) return false
  const [y, m, d] = String(row.mealDate).split('-').map(Number)
  const cutoff = new Date(y, m - 1, d, mealHour - 1, 0, 0)
  return new Date().getTime() < cutoff.getTime()
}

const handleCancel = (row) => {
  ElMessageBox.confirm(`确定取消 ${row.mealDate} 的${row.mealTypeLabel}预约吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await cancelMeal({ reservationId: row.id })
      ElMessage.success('预约已取消')
      loadList()
    } catch (e) {
      // 错误已由拦截器统一提示
    }
  }).catch(() => {})
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.form-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
