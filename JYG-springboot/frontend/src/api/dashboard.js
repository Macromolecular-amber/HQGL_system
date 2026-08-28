import request from '@/utils/request'

/** 首页统计卡片数据 */
export function getStatistics() {
  return request({ url: '/api/dashboard/statistics', method: 'get' })
}

/** 待办审批列表 */
export function getTodos() {
  return request({ url: '/api/dashboard/todos', method: 'get' })
}

/** 消息通知列表 */
export function getMessages() {
  return request({ url: '/api/dashboard/messages', method: 'get' })
}

/** 近7天资产趋势 */
export function getTrend() {
  return request({ url: '/api/dashboard/trend', method: 'get' })
}

/** 领导驾驶舱聚合数据 */
export function getLeadershipData() {
  return request({ url: '/api/dashboard/leadership', method: 'get' })
}
