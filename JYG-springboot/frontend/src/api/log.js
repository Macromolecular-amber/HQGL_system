import request from '@/utils/request'

/**
 * 操作日志接口
 */

/** 分页查询操作日志 */
export function getLogPage(params) {
  return request({ url: '/log/page', method: 'get', params })
}
