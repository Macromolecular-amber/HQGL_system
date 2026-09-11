<template>
  <div class="broadcast-message">
    <h2>📢 发布全体消息</h2>

    <!-- 发布表单（全宽卡片，风格与操作日志一致） -->
    <div class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="消息标题" prop="title">
              <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入消息标题，例如：中秋放假通知" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="消息类型" prop="messageType">
              <el-select v-model="form.messageType" placeholder="请选择消息类型" style="width: 100%">
                <el-option label="系统消息" value="SYSTEM" />
                <el-option label="业务消息" value="BUSINESS" />
                <el-option label="预警消息" value="WARNING" />
                <el-option label="审批通知" value="APPROVAL" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="消息内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="请输入消息内容，将下发给全体用户"
          />
        </el-form-item>

        <el-form-item label="发布范围">
          <el-tag type="info">全体启用账号</el-tag>
          <span class="range-tip">消息将发送给所有启用状态的账号</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="sending" @click="handleSend">发布消息</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sendMessage } from '@/api/message'

/** 发布表单 */
const formRef = ref()
const sending = ref(false)
const form = reactive({
  title: '',
  messageType: 'SYSTEM',
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  messageType: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }]
}

/** 发布：二次确认后调用全体发送接口 */
const handleSend = async () => {
  await formRef.value.validate()
  await ElMessageBox.confirm('确认将这条消息发送给全体用户吗？', '发布确认', {
    confirmButtonText: '确认发布',
    cancelButtonText: '取消',
    type: 'warning'
  })
  sending.value = true
  try {
    await sendMessage({
      sendAll: true, // 全体发送
      title: form.title,
      messageType: form.messageType,
      content: form.content
    })
    ElMessage.success(`发布成功，已发送给全体用户`)
    handleReset()
  } finally {
    sending.value = false
  }
}

/** 重置表单 */
const handleReset = () => {
  form.title = ''
  form.messageType = 'SYSTEM'
  form.content = ''
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.broadcast-message {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.broadcast-message h2 {
  margin: 0 0 16px 0;
  font-size: 20px;
}

.form-card {
  background: #fff;
  padding: 24px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  width: 100%;
  box-sizing: border-box;
}

.range-tip {
  margin-left: 8px;
  font-size: 13px;
  color: #909399;
}
</style>