<template>
  <view class="gy-page">
    <!-- 搜索区 -->
    <view class="card">
      <view class="filter-row">
        <u-tag v-for="s in filterStatus" :key="s.value" :text="s.label" :type="query.orderStatus === s.value ? 'primary' : 'info'" size="mini" plain @click="toggleStatus(s.value)" />
      </view>
      <u-button size="mini" type="primary" plain @click="openApply">新增保洁</u-button>
    </view>

    <!-- 保洁单列表 -->
    <view class="card">
      <view class="card-title">
        <text>🧹 保洁管理</text>
        <text class="total">共 {{ total }} 单</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.cleaningNo }}</text>
          <u-tag :text="row.statusLabel || row.orderStatus" :type="statusType(row.orderStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">房间</text><text class="value">{{ row.roomNo }} · {{ row.cleaningTypeLabel || row.cleaningType }}</text></view>
        <view class="item-line"><text class="label">保洁日期</text><text class="value">{{ cleaningDate(row.cleaningTime) }}</text></view>
        <view class="item-line"><text class="label">范围</text><text class="value">{{ row.cleaningScope || '-' }}</text></view>
        <view class="item-line"><text class="label">保洁员</text><text class="value">{{ row.assigneeName || '-' }}</text></view>
        <view class="item-actions">
          <u-button v-if="row.orderStatus === 'PENDING' && canAudit" size="mini" type="warning" plain @click.stop="openAudit(row)">审批</u-button>
          <u-button v-if="row.orderStatus === 'APPROVED' && canAssign" size="mini" type="primary" plain @click.stop="openAssign(row)">派单</u-button>
          <u-button v-if="row.orderStatus === 'ONGOING' && canAccept" size="mini" type="success" plain @click.stop="openAccept(row)">验收</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无保洁单</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 新增保洁弹窗 -->
    <u-popup :show="applyVisible" mode="bottom" round :closeOnClickOverlay="true" @close="applyVisible = false" height="75%">
      <view class="popup-panel">
        <view class="popup-title">新增保洁</view>
        <u-form :model="applyForm" ref="applyFormRef" :rules="applyRules">
          <u-form-item label="房间" prop="roomId" required label-width="180rpx">
            <u-input v-model="applyForm.roomText" placeholder="请选择房间" :border="false" disabled @click="showRoom = true" />
          </u-form-item>
          <u-form-item label="保洁类型" prop="cleaningType" required label-width="180rpx">
            <u-input v-model="applyForm.cleaningTypeText" placeholder="请选择" :border="false" disabled @click="showCleaningType = true" />
          </u-form-item>
          <u-form-item label="保洁日期" prop="cleaningDate" required label-width="180rpx">
            <u-input v-model="applyForm.cleaningDate" placeholder="请选择日期" :border="false" disabled @click="showDate = true" />
          </u-form-item>
          <u-form-item label="时间段" label-width="180rpx">
            <u-input v-model="applyForm.timeSlotText" placeholder="选填" :border="false" disabled @click="showSlot = true" />
          </u-form-item>
          <u-form-item label="保洁范围" label-width="180rpx">
            <u-input v-model="applyForm.cleaningScope" type="textarea" placeholder="如：卧室、客厅" :border="false" />
          </u-form-item>
          <u-form-item label="特殊要求" label-width="180rpx">
            <u-input v-model="applyForm.cleaningRequirement" type="textarea" placeholder="如：深度清洁" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="applyVisible = false">取消</u-button>
          <u-button type="primary" :loading="applying" @click="submitApply">提交申请</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 房间选择 -->
    <u-picker v-model:show="showRoom" :columns="roomColumns" @confirm="onRoomConfirm"></u-picker>
    <!-- 保洁类型 -->
    <u-picker v-model:show="showCleaningType" :columns="cleaningTypeColumns" @confirm="onCleaningTypeConfirm"></u-picker>
    <!-- 时间段 -->
    <u-picker v-model:show="showSlot" :columns="slotColumns" @confirm="onSlotConfirm"></u-picker>
    <!-- 保洁日期 -->
    <u-datetime-picker :show="showDate" v-model="dateTs" mode="date" @confirm="onDateConfirm"></u-datetime-picker>

    <!-- 审批弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="auditVisible = false">
      <view class="popup-panel">
        <view class="popup-title">保洁审批</view>
        <view class="popup-no">{{ auditForm.cleaningNo }}</view>
        <u-radio-group v-model="auditForm.auditResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="REJECT" name="REJECT">驳回</u-radio>
        </u-radio-group>
        <u-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审批意见" :border="true" />
        <view class="popup-actions">
          <u-button @click="auditVisible = false">取消</u-button>
          <u-button type="primary" :loading="auditing" @click="submitAudit">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 派单弹窗 -->
    <u-popup :show="assignVisible" mode="bottom" round :closeOnClickOverlay="true" @close="assignVisible = false" height="55%">
      <view class="popup-panel">
        <view class="popup-title">保洁派单</view>
        <view class="popup-no">{{ assignForm.cleaningNo }}</view>
        <u-form :model="assignForm" ref="assignFormRef" :rules="assignRules">
          <u-form-item label="保洁员" prop="assigneeId" required label-width="160rpx">
            <u-input v-model="assignForm.assigneeText" placeholder="请选择保洁员" :border="false" disabled @click="showCleaner = true" />
          </u-form-item>
          <u-form-item label="服务公司" label-width="160rpx">
            <u-input v-model="assignForm.assigneeCompany" placeholder="保洁公司名称，选填" :border="false" />
          </u-form-item>
        </u-form>
        <view class="popup-actions">
          <u-button @click="assignVisible = false">取消</u-button>
          <u-button type="primary" :loading="assigning" @click="submitAssign">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 保洁员选择 -->
    <u-picker v-model:show="showCleaner" :columns="cleanerColumns" @confirm="onCleanerConfirm"></u-picker>

    <!-- 验收弹窗 -->
    <u-popup :show="acceptVisible" mode="bottom" round :closeOnClickOverlay="true" @close="acceptVisible = false" height="60%">
      <view class="popup-panel">
        <view class="popup-title">保洁验收</view>
        <view class="popup-no">{{ acceptForm.cleaningNo }}</view>
        <u-radio-group v-model="acceptForm.acceptResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="FAIL" name="FAIL">不通过</u-radio>
        </u-radio-group>
        <u-form-item label="评分" v-if="acceptForm.acceptResult === 'PASS'">
          <u-input v-model="acceptForm.acceptScore" type="number" placeholder="1-5 分" :border="false" />
        </u-form-item>
        <u-input v-model="acceptForm.acceptRemark" type="textarea" placeholder="验收意见，选填" :border="true" />
        <view class="popup-actions">
          <u-button @click="acceptVisible = false">取消</u-button>
          <u-button type="primary" :loading="accepting" @click="submitAccept">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="85%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">保洁单详情</view>
        <view class="detail-row"><text class="d-label">保洁单号</text><text class="d-value">{{ detail.cleaningNo }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="detail.statusLabel || detail.orderStatus" :type="statusType(detail.orderStatus)" size="mini" /></view>
        <view class="detail-row"><text class="d-label">房间</text><text class="d-value">{{ detail.roomNo || '-' }} · {{ detail.building || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">保洁类型</text><text class="d-value">{{ detail.cleaningTypeLabel || detail.cleaningType || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">保洁时间</text><text class="d-value">{{ formatTime(detail.cleaningTime) }}</text></view>
        <view class="detail-row"><text class="d-label">申请人</text><text class="d-value">{{ detail.applicantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">保洁员</text><text class="d-value">{{ detail.assigneeName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">范围</text><text class="d-value">{{ detail.cleaningScope || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">特殊要求</text><text class="d-value">{{ detail.cleaningRequirement || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">服务公司</text><text class="d-value">{{ detail.assigneeCompany || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批人</text><text class="d-value">{{ detail.auditUserName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">审批时间</text><text class="d-value">{{ formatTime(detail.auditTime) }}</text></view>
        <view class="detail-row"><text class="d-label">验收评分</text><text class="d-value">{{ detail.acceptScore ?? '-' }}</text></view>
        <view class="detail-row"><text class="d-label">验收意见</text><text class="d-value">{{ detail.acceptRemark || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">执行照片</text><text class="d-value">{{ detail.executePhotos || '-' }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole } from '@/utils/auth'

/** 保洁类型与状态 */
const cleaningTypes = [
  { value: 'REGULAR', text: '定期' },
  { value: 'ON_DEMAND', text: '按需' }
]
const statusOptions = [
  { value: 'PENDING', label: '待审批' },
  { value: 'APPROVED', label: '已批准' },
  { value: 'ONGOING', label: '执行中' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'REJECTED', label: '已驳回' }
]
const filterStatus = [{ value: '', label: '全部' }, ...statusOptions]
const statusTypeMap = { PENDING: 'warning', APPROVED: 'success', ONGOING: 'primary', COMPLETED: 'success', REJECTED: 'error', DONE: 'success' }
const statusType = (s) => statusTypeMap[s] || 'info'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')
const cleaningDate = (t) => (t ? String(t).slice(0, 10) : '-')

/** 权限 */
const canAudit = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER'])
const canAssign = canAudit
const canAccept = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER', 'CLEANER'])

/** 查询 */
const query = reactive({ orderStatus: '', page: 1, size: 10 })
const loading = ref(false)
const list = ref([])
const total = ref(0)
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { query.page = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({
      url: '/api/gy/cleaning/page',
      data: { orderStatus: query.orderStatus || undefined, page: query.page, size: query.size }
    })
    const rows = res.data || []
    list.value = reset ? rows : [...list.value, ...rows]
    total.value = res.total || 0
    if (rows.length < query.size) finished.value = true
    else query.page += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

const toggleStatus = (v) => {
  query.orderStatus = query.orderStatus === v ? '' : v
  query.page = 1; finished.value = false
  loadList(true)
}

onReachBottom(() => { loadList() })

/** 下拉数据：房间/保洁员 */
const rooms = ref([])
const cleaners = ref([])

const loadOptions = async () => {
  try {
    const res = await request({ url: '/api/gy/room/page', data: { page: 1, size: 100 } })
    rooms.value = res.data || []
  } catch (e) {
    rooms.value = []
  }
  try {
    cleaners.value = (await request({ url: '/api/sys/user/cleaners' })) || []
  } catch (e) {
    cleaners.value = []
  }
}

/** 新增保洁 */
const applyVisible = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({
  roomId: null, roomText: '', cleaningType: '', cleaningTypeText: '',
  cleaningDate: '', cleaningTimeSlot: '', timeSlotText: '',
  cleaningScope: '', cleaningRequirement: ''
})

const applyRules = {
  roomId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择房间'))
      else callback()
    },
    trigger: ['change']
  },
  cleaningType: { required: true, message: '请选择保洁类型', trigger: ['change'] },
  cleaningDate: { required: true, message: '请选择保洁日期', trigger: ['change'] }
}

const openApply = () => {
  Object.assign(applyForm, {
    roomId: null, roomText: '', cleaningType: '', cleaningTypeText: '',
    cleaningDate: '', cleaningTimeSlot: '', timeSlotText: '',
    cleaningScope: '', cleaningRequirement: ''
  })
  applyVisible.value = true
}

/** 房间选择 */
const showRoom = ref(false)
const roomColumns = computed(() => [rooms.value.map((r) => ({ value: r.id, text: `${r.roomNo}（${r.building}）` }))])
const onRoomConfirm = (e) => {
  applyForm.roomId = e.value[0].value
  applyForm.roomText = e.value[0].text
}

/** 保洁类型 */
const showCleaningType = ref(false)
const cleaningTypeColumns = computed(() => [cleaningTypes.map((t) => ({ value: t.value, text: t.text }))])
const onCleaningTypeConfirm = (e) => {
  applyForm.cleaningType = e.value[0].value
  applyForm.cleaningTypeText = e.value[0].text
}

/** 时间段 */
const showSlot = ref(false)
const slots = [
  { value: 'MORNING', text: '上午' },
  { value: 'AFTERNOON', text: '下午' }
]
const slotColumns = computed(() => [slots.map((s) => ({ value: s.value, text: s.text }))])
const onSlotConfirm = (e) => {
  applyForm.cleaningTimeSlot = e.value[0].value
  applyForm.timeSlotText = e.value[0].text
}

/** 日期 */
const showDate = ref(false)
const dateTs = ref(Date.now())
const pad = (n) => String(n).padStart(2, '0')
const onDateConfirm = (e) => {
  dateTs.value = e.value
  const d = new Date(e.value)
  applyForm.cleaningDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
  showDate.value = false
}

const submitApply = async () => {
  const valid = await applyFormRef.value.validate()
  if (!valid) return
  applying.value = true
  try {
    await request({
      url: '/api/gy/cleaning/apply',
      method: 'POST',
      data: {
        roomId: applyForm.roomId,
        cleaningType: applyForm.cleaningType,
        cleaningDate: applyForm.cleaningDate,
        cleaningTimeSlot: applyForm.cleaningTimeSlot || undefined,
        cleaningScope: applyForm.cleaningScope || undefined,
        cleaningRequirement: applyForm.cleaningRequirement || undefined
      }
    })
    uni.showToast({ title: '保洁申请提交成功', icon: 'success' })
    applyVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    applying.value = false
  }
}

/** 审批 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ cleaningId: null, cleaningNo: '', auditResult: 'PASS', auditRemark: '' })

const openAudit = (row) => {
  Object.assign(auditForm, { cleaningId: row.id, cleaningNo: row.cleaningNo, auditResult: 'PASS', auditRemark: '' })
  auditVisible.value = true
}

const submitAudit = async () => {
  auditing.value = true
  try {
    await request({
      url: '/api/gy/cleaning/audit',
      method: 'PUT',
      data: { cleaningId: auditForm.cleaningId, auditResult: auditForm.auditResult, auditRemark: auditForm.auditRemark }
    })
    uni.showToast({ title: '审批完成', icon: 'success' })
    auditVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    auditing.value = false
  }
}

/** 派单 */
const assignVisible = ref(false)
const assigning = ref(false)
const assignFormRef = ref()
const assignForm = reactive({ cleaningId: null, cleaningNo: '', assigneeId: null, assigneeText: '', assigneeCompany: '' })

const assignRules = {
  assigneeId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择保洁员'))
      else callback()
    },
    trigger: ['change']
  }
}

const openAssign = (row) => {
  Object.assign(assignForm, { cleaningId: row.id, cleaningNo: row.cleaningNo, assigneeId: null, assigneeText: '', assigneeCompany: '' })
  assignVisible.value = true
}

const showCleaner = ref(false)
const cleanerColumns = computed(() => [cleaners.value.map((c) => ({ value: c.id, text: `${c.realName}（${c.phone || '-'}）` }))])
const onCleanerConfirm = (e) => {
  assignForm.assigneeId = e.value[0].value
  assignForm.assigneeText = e.value[0].text
}

const submitAssign = async () => {
  const valid = await assignFormRef.value.validate()
  if (!valid) return
  assigning.value = true
  try {
    await request({
      url: '/api/gy/cleaning/assign',
      method: 'PUT',
      data: { cleaningId: assignForm.cleaningId, assigneeId: assignForm.assigneeId, assigneeCompany: assignForm.assigneeCompany || undefined }
    })
    uni.showToast({ title: '派单成功', icon: 'success' })
    assignVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    assigning.value = false
  }
}

/** 验收 */
const acceptVisible = ref(false)
const accepting = ref(false)
const acceptForm = reactive({ cleaningId: null, cleaningNo: '', acceptResult: 'PASS', acceptScore: '5', acceptRemark: '' })

const openAccept = (row) => {
  Object.assign(acceptForm, { cleaningId: row.id, cleaningNo: row.cleaningNo, acceptResult: 'PASS', acceptScore: '5', acceptRemark: '' })
  acceptVisible.value = true
}

const submitAccept = async () => {
  accepting.value = true
  try {
    await request({
      url: '/api/gy/cleaning/accept',
      method: 'PUT',
      data: {
        cleaningId: acceptForm.cleaningId,
        acceptResult: acceptForm.acceptResult,
        acceptScore: acceptForm.acceptResult === 'PASS' ? Number(acceptForm.acceptScore) : undefined,
        acceptRemark: acceptForm.acceptRemark || undefined
      }
    })
    uni.showToast({ title: '验收完成', icon: 'success' })
    acceptVisible.value = false
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    accepting.value = false
  }
}

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)
const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gy/cleaning/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadOptions()
  loadList(true)
})
</script>

<style>
.gy-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.filter-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-bottom: 20rpx; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 150rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 180rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>