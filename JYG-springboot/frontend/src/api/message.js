import request from '@/utils/request'

/**
 * 消息中心接口
 */

/** 发送消息（管理员或系统调用） */
export function sendMessage(data) {
  return request({ url: '/message/send', method: 'post', data })
}

/** 标记单条消息已读 */
export function markAsRead(id) {
  return request({ url: `/message/read/${id}`, method: 'put' })
}

/** 当前用户全部消息标记已读 */
export function markAllAsRead() {
  return request({ url: '/message/read-all', method: 'put' })
}

/** 分页查询当前用户消息 */
export function getMessagePage(params) {
  return request({ url: '/message/page', method: 'get', params })
}

/** 获取当前用户未读数量 */
export function getUnreadCount() {
  return request({ url: '/message/unread-count', method: 'get' })
}

/** 获取最新 N 条消息（默认 5 条，首页用） */
export function getLatestMessages(limit = 5) {
  return request({ url: '/message/latest', method: 'get', params: { limit } })
}
