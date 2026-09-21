/**
 * 请求封装：对接现有后勤系统后端（JWT 认证）
 * 接口路径/参数/返回结构与 Web 端完全一致，APP 仅作为新客户端接入
 * 【兼容性】不修改后端任何接口，仅新增本封装
 */

// 后端地址（条件编译：H5 用本地 8080 调试；APP/小程序指向正式服务器）
// 打包 APP 前：把 APP_BASE_URL 改成你的服务器地址（如 http://192.168.1.100:8080）
// #ifdef H5
const BASE_URL = 'http://localhost:8080'
// #endif
// #ifndef H5
const APP_BASE_URL = 'http://192.168.1.100:8080' // TODO: 打包前改为实际服务器地址
const BASE_URL = APP_BASE_URL
// #endif

const SUCCESS_CODE = 200

/**
 * 统一请求
 * @param {Object} options { url, method, data }
 * @returns Promise<data> 直接返回 Result 解包后的 data
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: (BASE_URL + options.url),
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        // HTTP 层 401：token 失效 → 清除登录态回登录页
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          reject(new Error('未登录'))
          return
        }
        // HTTP 层 403：权限不足
        if (res.statusCode === 403) {
          uni.showToast({ title: '权限不足，无法访问该功能', icon: 'none' })
          reject(new Error('权限不足'))
          return
        }
        // 后端统一解包 { code, message, data }
        const body = res.data
        if (body && typeof body === 'object' && body.code !== undefined) {
          if (body.code === SUCCESS_CODE) {
            resolve(body.data)
          } else {
            uni.showToast({ title: body.message || '请求失败', icon: 'none' })
            reject(new Error(body.message || '请求失败'))
          }
          return
        }
        resolve(body)
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请检查连接', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request