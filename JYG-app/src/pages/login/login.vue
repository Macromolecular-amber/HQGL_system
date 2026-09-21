<template>
  <view class="login-page">
    <view class="login-card">
      <view class="app-title">🏛️ 机关事务后勤APP</view>
      <view class="app-sub">后勤服务，掌上办理</view>

      <view class="form-item">
        <text class="label">用户名</text>
        <input class="input" v-model="username" placeholder="请输入用户名" placeholder-class="ph" />
      </view>
      <view class="form-item">
        <text class="label">密码</text>
        <input class="input" v-model="password" password placeholder="请输入密码" placeholder-class="ph" />
      </view>

      <button class="login-btn" :loading="loading" :disabled="loading" @click="handleLogin">登 录</button>
      <view class="tip">账号由系统管理员分配，忘记密码请联系管理员</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { request } from '@/utils/request'
import { setLoginState } from '@/utils/auth'

const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  if (!username.value || !password.value) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    // 复用现有后端登录接口：POST /api/auth/login
    const data = await request({
      url: '/api/auth/login',
      method: 'POST',
      data: { username: username.value, password: password.value }
    })
    setLoginState(data.token, data.userInfo)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/index/index' }), 600)
  } catch (e) {
    // 错误已由 request 统一提示
  } finally {
    loading.value = false
  }
}
</script>

<style>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 120rpx 48rpx;
  box-sizing: border-box;
}
.login-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 60rpx 48rpx;
}
.app-title {
  font-size: 44rpx;
  font-weight: 700;
  text-align: center;
  color: #333;
}
.app-sub {
  font-size: 26rpx;
  color: #999;
  text-align: center;
  margin: 12rpx 0 60rpx;
}
.form-item {
  margin-bottom: 36rpx;
}
.label {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}
.input {
  background: #f5f7fa;
  border-radius: 12rpx;
  padding: 24rpx;
  font-size: 30rpx;
}
.ph {
  color: #c0c4cc;
}
.login-btn {
  margin-top: 24rpx;
  background: #409eff;
  color: #fff;
  font-size: 34rpx;
  letter-spacing: 8rpx;
}
.tip {
  margin-top: 32rpx;
  font-size: 24rpx;
  color: #bbb;
  text-align: center;
}
</style>