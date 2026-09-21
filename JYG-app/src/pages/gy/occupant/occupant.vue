<template>
  <view class="occ-page">
    <!-- 公寓申请表单 -->
    <view class="card">
      <view class="card-title">🏢 公寓申请</view>
      <u-form :model="form" ref="formRef" :rules="rules">
        <u-form-item label="入住人" prop="occupantName" required label-width="170rpx">
          <u-input v-model="form.occupantName" placeholder="入住人姓名" :border="false" />
        </u-form-item>
        <u-form-item label="身份证号" prop="idCard" label-width="170rpx">
          <u-input v-model="form.idCard" placeholder="选填" :border="false" />
        </u-form-item>
        <u-form-item label="联系电话" prop="phone" required label-width="170rpx">
          <u-input v-model="form.phone" type="number" placeholder="联系电话" :border="false" />
        </u-form-item>
        <u-form-item label="所属单位" prop="unitId" required label-width="170rpx">
          <u-input v-model="form.unitName" placeholder="请选择单位" :border="false" disabled @click="showUnit = true" />
        </u-form-item>
        <u-form-item label="职务" prop="position" label-width="170rpx">
          <u-input v-model="form.position" placeholder="选填" :border="false" />
        </u-form-item>
        <u-form-item label="申请原因" prop="applyReason" required label-width="170rpx">
          <u-input v-model="form.applyReason" type="textarea" placeholder="请填写申请原因" :border="false" />
        </u-form-item>
        <u-form-item label="备注" label-width="170rpx">
          <u-input v-model="form.remark" type="textarea" placeholder="选填" :border="false" />
        </u-form-item>
      </u-form>
      <u-button type="primary" :loading="submitting" @click="onSubmit">提交申请</u-button>
    </view>

    <!-- 入住申请列表 -->
    <view class="card">
      <view class="card-title">
        <text>📋 申请记录</text>
        <text class="total">共 {{ total }} 条</text>
      </view>
      <view class="list-item" v-for="row in list" :key="row.id" @click="showDetail(row)">
        <view class="item-head">
          <text class="item-no">{{ row.occupantName }}</text>
          <u-tag :text="statusText(row.occupantStatus)" :type="statusType(row.occupantStatus)" size="mini" />
        </view>
        <view class="item-line"><text class="label">单位</text><text class="value">{{ row.unitName || '-' }}</text></view>
        <view class="item-line"><text class="label">申请原因</text><text class="value">{{ row.applyReason }}</text></view>
        <view class="item-line"><text class="label">申请时间</text><text class="value">{{ formatTime(row.createTime) }}</text></view>
        <view class="item-actions" v-if="isPending(row) && canAudit">
          <u-button size="mini" type="warning" plain @click.stop="openAudit(row)">审批</u-button>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty">暂无申请记录</view>
      <view v-if="loading" class="empty">加载中...</view>
    </view>

    <!-- 单位选择 -->
    <u-picker v-model:show="showUnit" :columns="unitColumns" @confirm="onUnitConfirm"></u-picker>

    <!-- 审批弹窗 -->
    <u-popup :show="auditVisible" mode="bottom" round :closeOnClickOverlay="true" @close="closeAudit" height="70%">
      <view class="popup-panel">
        <view class="popup-title">入住申请审批</view>
        <view class="popup-no">{{ auditForm.occupantName }} · {{ auditForm.unitName || '-' }}</view>
        <u-radio-group v-model="auditForm.auditResult">
          <u-radio label="PASS" name="PASS">通过</u-radio>
          <u-radio label="REJECT" name="REJECT">驳回</u-radio>
        </u-radio-group>
        <!-- 通过时必选分配房间 -->
        <u-form-item label="分配房间" v-if="auditForm.auditResult === 'PASS'">
          <u-input v-model="auditForm.roomNo" placeholder="请选择空闲房间" :border="false" disabled @click="showRoom = true" />
        </u-form-item>
        <u-input v-model="auditForm.auditRemark" type="textarea" placeholder="请输入审批意见" :border="true" />
        <view class="popup-actions">
          <u-button @click="auditVisible = false">取消</u-button>
          <u-button type="primary" :loading="auditing" @click="submitAudit">确定</u-button>
        </view>
      </view>
    </u-popup>

    <!-- 房间选择（空闲房间） -->
    <u-picker v-model:show="showRoom" :columns="roomColumns" @confirm="onRoomConfirm"></u-picker>

    <!-- 详情弹窗 -->
    <u-popup :show="detailVisible" mode="bottom" round :closeOnClickOverlay="true" @close="detailVisible = false" height="80%">
      <view class="popup-panel" v-if="detail">
        <view class="popup-title">入住申请详情</view>
        <view class="detail-row"><text class="d-label">入住人</text><text class="d-value">{{ detail.occupantName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">身份证号</text><text class="d-value">{{ detail.idCard || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">联系电话</text><text class="d-value">{{ detail.phone || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">所属单位</text><text class="d-value">{{ detail.unitName || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">职务</text><text class="d-value">{{ detail.position || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">申请原因</text><text class="d-value">{{ detail.applyReason || '-' }}</text></view>
        <view class="detail-row"><text class="d-label">状态</text><u-tag :text="statusText(detail.occupantStatus)" :type="statusType(detail.occupantStatus)" size="mini" /></view>
        <view class="detail-row" v-if="detail.roomNo"><text class="d-label">分配房间</text><text class="d-value">{{ detail.roomNo }}</text></view>
        <view class="detail-row"><text class="d-label">申请时间</text><text class="d-value">{{ formatTime(detail.createTime) }}</text></view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { request } from '@/utils/request'
import { hasRole, getUserInfo } from '@/utils/auth'

/** 状态映射（对齐后端 occupantStatus 字段，不区分大小写） */
const statusMap = {
  PENDING: { text: '待审批', type: 'warning' },
  ACTIVE: { text: '在住', type: 'primary' },
  RESIGNED: { text: '已退住', type: 'info' },
  REJECTED: { text: '已驳回', type: 'error' },
  ACCEPTED: { text: '已验收', type: 'success' },
  APPROVED: { text: '已通过', type: 'success' }
}
const statusText = (s) => (statusMap[String(s || '').toUpperCase()] || { text: s || '-' }).text
const statusType = (s) => (statusMap[String(s || '').toUpperCase()] || { type: 'info' }).type
const isPending = (row) => String(row.occupantStatus || '').toUpperCase() === 'PENDING'
const formatTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 19) : '-')

/** 审批权限 */
const canAudit = hasRole(['BIZ_ADMIN', 'WAREHOUSE', 'DIRECTOR', 'DEPT_MANAGER'])

/** 当前用户单位（预填默认值） */
const userInfo = getUserInfo()

/** 申请表单 */
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  occupantName: '',
  idCard: '',
  phone: '',
  unitId: userInfo.unitId || null,
  unitName: userInfo.unitName || '',
  position: '',
  applyReason: '',
  remark: ''
})

const rules = {
  occupantName: { required: true, message: '请输入入住人姓名', trigger: ['blur'] },
  phone: { required: true, message: '请输入联系电话', trigger: ['blur'] },
  // 自定义校验：兼容数字/字符串 ID（async-validator 默认 string 类型会误拦截数字）
  unitId: {
    validator: (rule, value, callback) => {
      if (value === null || value === undefined || value === '') callback(new Error('请选择所属单位'))
      else callback()
    },
    trigger: ['change']
  },
  applyReason: { required: true, message: '请填写申请原因', trigger: ['blur'] }
}

/** 单位下拉 */
const showUnit = ref(false)
const units = ref([])
const unitColumns = computed(() => [units.value.map((u) => ({ value: u.id, text: u.unitName }))])

const loadUnits = async () => {
  try {
    units.value = (await request({ url: '/api/sys/unit/list' })) || []
    // 预填：userInfo.unitName 可能为空，从列表找匹配
    if (!form.unitName && form.unitId) {
      const hit = units.value.find((u) => u.id === form.unitId)
      if (hit) form.unitName = hit.unitName
    }
    // 兜底：unitId 仍为空时默认选第一个单位，保证必填校验通过
    if (!form.unitId && units.value.length) {
      form.unitId = units.value[0].id
      form.unitName = units.value[0].unitName
    }
  } catch (e) {
    units.value = []
  }
}

const onUnitConfirm = (e) => {
  const item = e.value[0]
  form.unitId = item.value
  form.unitName = item.text
}

/** 提交申请 */
const onSubmit = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  submitting.value = true
  try {
    await request({
      url: '/api/gy/occupant/apply',
      method: 'POST',
      data: {
        occupantName: form.occupantName,
        idCard: form.idCard || undefined,
        phone: form.phone,
        unitId: form.unitId,
        position: form.position || undefined,
        applyReason: form.applyReason,
        remark: form.remark || undefined
      }
    })
    uni.showToast({ title: '申请提交成功', icon: 'success' })
    resetForm()
    loadList(true)
  } catch (e) {
    // 错误已统一提示
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.occupantName = ''
  form.idCard = ''
  form.phone = ''
  form.unitId = userInfo.unitId || null
  form.unitName = userInfo.unitName || ''
  form.position = ''
  form.applyReason = ''
  form.remark = ''
  formRef.value && formRef.value.resetFields()
}

/** 列表 */
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const finished = ref(false)

const loadList = async (reset = false) => {
  if (reset) { page.value = 1; finished.value = false }
  if (finished.value) return
  loading.value = true
  try {
    const res = await request({ url: '/api/gy/occupant/page', data: { page: page.value, size } })
    const rows = res.data || []
    list.value = reset ? rows : [...list.value, ...rows]
    total.value = res.total || 0
    if (rows.length < size) finished.value = true
    else page.value += 1
  } catch (e) {
    // 错误已统一提示
  } finally {
    loading.value = false
  }
}

onReachBottom(() => {
  loadList()
})

/** 审批 */
const auditVisible = ref(false)
const auditing = ref(false)
const auditForm = reactive({ occupantId: null, occupantName: '', unitName: '', auditResult: 'PASS', auditRemark: '', roomId: null, roomNo: '' })

const openAudit = (row) => {
  auditForm.occupantId = row.id
  auditForm.occupantName = row.occupantName
  auditForm.unitName = row.unitName
  auditForm.auditResult = 'PASS'
  auditForm.auditRemark = ''
  auditForm.roomId = null
  auditForm.roomNo = ''
  auditVisible.value = true
  loadRooms()
}

const closeAudit = () => {
  auditVisible.value = false
}

/** 房间选择（空闲人才公寓房间） */
const showRoom = ref(false)
const rooms = ref([])
const roomColumns = computed(() => [rooms.value.map((r) => ({ value: r.id, text: r.roomNo || r.roomName }))])

const loadRooms = async () => {
  try {
    const res = await request({ url: '/api/gy/room/available' })
    rooms.value = res || []
  } catch (e) {
    rooms.value = []
  }
}

const onRoomConfirm = (e) => {
  const item = e.value[0]
  auditForm.roomId = item.value
  auditForm.roomNo = item.text
}

const submitAudit = async () => {
  if (auditForm.auditResult === 'PASS' && !auditForm.roomId) {
    uni.showToast({ title: '请选择分配房间', icon: 'none' })
    return
  }
  auditing.value = true
  try {
    await request({
      url: '/api/gy/occupant/audit',
      method: 'PUT',
      data: {
        occupantId: auditForm.occupantId,
        auditResult: auditForm.auditResult,
        auditRemark: auditForm.auditRemark,
        roomId: auditForm.auditResult === 'PASS' ? auditForm.roomId : undefined
      }
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

/** 详情 */
const detailVisible = ref(false)
const detail = ref(null)

const showDetail = async (row) => {
  detailVisible.value = true
  try {
    detail.value = await request({ url: `/api/gy/occupant/${row.id}` })
  } catch (e) {
    detailVisible.value = false
  }
}

onMounted(() => {
  loadUnits()
  loadList()
})
</script>

<style>
.occ-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; display: flex; justify-content: space-between; align-items: center; }
.total { font-size: 24rpx; color: #999; font-weight: 400; }
.list-item { padding: 20rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.item-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12rpx; }
.item-no { font-size: 28rpx; font-weight: 600; color: #333; }
.item-line { display: flex; margin: 6rpx 0; font-size: 26rpx; }
.label { color: #999; width: 130rpx; flex-shrink: 0; }
.value { color: #333; flex: 1; }
.item-actions { display: flex; gap: 16rpx; margin-top: 16rpx; justify-content: flex-end; }
.empty { text-align: center; color: #999; padding: 60rpx 0; font-size: 26rpx; }
.popup-panel { padding: 40rpx 32rpx 60rpx; }
.popup-title { font-size: 34rpx; font-weight: 700; text-align: center; margin-bottom: 12rpx; }
.popup-no { text-align: center; color: #999; font-size: 26rpx; margin-bottom: 24rpx; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.detail-row { display: flex; padding: 14rpx 0; font-size: 28rpx; border-bottom: 1rpx solid #f7f7f7; }
.d-label { color: #999; width: 160rpx; flex-shrink: 0; }
.d-value { color: #333; flex: 1; }
</style>