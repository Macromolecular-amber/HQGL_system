<template>
  <view class="sys-page">
    <view class="card">
      <view class="card-title">📢 发布全体消息</view>
      <u-form :model="form" ref="formRef" :rules="rules" label-width="160rpx">
        <u-form-item label="消息标题" prop="title" required>
          <u-input v-model="form.title" placeholder="例如：中秋放假通知" maxlength="100" :border="false" />
        </u-form-item>
        <u-form-item label="消息类型" prop="messageType" required>
          <u-input v-model="form.messageTypeText" placeholder="请选择" :border="false" disabled @click="showType = true" />
        </u-form-item>
        <u-form-item label="消息内容" prop="content" required>
          <u-input v-model="form.content" type="textarea" placeholder="请输入消息内容，将下发给全体用户" maxlength="2000" :border="false" />
        </u-form-item>
      </u-form>
      <view class="range-tip">发布范围：全体启用账号</view>
      <view class="popup-actions">
        <u-button @click="handleReset">重置</u-button>
        <u-button type="primary" :loading="sending" @click="handleSend">发布消息</u-button>
      </view>
    </view>

    <u-picker v-model:show="showType" :columns="typeColumns" @confirm="onTypeConfirm"></u-picker>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { request } from '@/utils/request'

const messageTypes = [
  { value: 'SYSTEM', text: '系统消息' },
  { value: 'BUSINESS', text: '业务消息' },
  { value: 'WARNING', text: '预警消息' },
  { value: 'APPROVAL', text: '审批通知' }
]

const formRef = ref()
const sending = ref(false)
const showType = ref(false)
const form = reactive({ title: '', messageType: 'SYSTEM', messageTypeText: '系统消息', content: '' })

const rules = {
  title: { required: true, message: '请输入消息标题', trigger: ['blur'] },
  messageType: { required: true, message: '请选择消息类型', trigger: ['change'] },
  content: { required: true, message: '请输入消息内容', trigger: ['blur'] }
}

const typeColumns = computed(() => [messageTypes.map((t) => ({ value: t.value, text: t.text }))])
const onTypeConfirm = (e) => {
  form.messageType = e.value[0].value
  form.messageTypeText = e.value[0].text
}

const handleSend = async () => {
  const valid = await formRef.value.validate()
  if (!valid) return
  uni.showModal({
    title: '发布确认',
    content: '确认将这条消息发送给全体用户吗？',
    confirmText: '确认发布',
    cancelText: '取消',
    success: async (res) => {
      if (!res.confirm) return
      sending.value = true
      try {
        await request({
          url: '/api/message/send', method: 'POST',
          data: { sendAll: true, title: form.title, messageType: form.messageType, content: form.content }
        })
        uni.showToast({ title: '发布成功，已发送给全体用户', icon: 'success' })
        handleReset()
      } catch (e) {
        // 错误已统一提示
      } finally {
        sending.value = false
      }
    }
  })
}

const handleReset = () => {
  form.title = ''
  form.messageType = 'SYSTEM'
  form.messageTypeText = '系统消息'
  form.content = ''
  formRef.value && formRef.value.clearValidate()
}
</script>

<style>
.sys-page { min-height: 100vh; background: #f5f7fa; padding: 20rpx; box-sizing: border-box; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 20rpx; }
.card-title { font-size: 32rpx; font-weight: 700; margin-bottom: 20rpx; }
.range-tip { font-size: 24rpx; color: #999; margin: 8rpx 0 20rpx; }
.popup-actions { display: flex; gap: 20rpx; }
</style>