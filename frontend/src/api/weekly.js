import request from './request'

/** 学生：提交周报 */
export function submitWeekly(data) {
  return request({
    url: '/weekly/submit',
    method: 'post',
    data,
  })
}

/** 学生：我的周报列表 */
export function myWeekly(params) {
  return request({
    url: '/weekly/my',
    method: 'get',
    params,
  })
}

/** 企业：收到的周报列表 */
export function receivedWeekly(params) {
  return request({
    url: '/weekly/received',
    method: 'get',
    params,
  })
}

/** 周报详情 */
export function weeklyDetail(reportId) {
  return request({
    url: `/weekly/detail/${reportId}`,
    method: 'get',
  })
}

/** 企业：评价周报 */
export function reviewWeekly(data) {
  return request({
    url: '/weekly/review',
    method: 'post',
    data,
  })
}
