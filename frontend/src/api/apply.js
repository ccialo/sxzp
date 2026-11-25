import request from './request'

/** 学生投递简历 */
export function submitApply(data) {
  return request({
    url: '/apply/submit',
    method: 'post',
    data,
  })
}

/** 学生撤回投递 */
export function withdrawApply(applyId) {
  return request({
    url: `/apply/withdraw/${applyId}`,
    method: 'put',
  })
}

/** 学生：我的投递列表 */
export function myApplies(params) {
  return request({
    url: '/apply/my',
    method: 'get',
    params,
  })
}

/** 企业：收到的投递列表 */
export function receivedApplies(params) {
  return request({
    url: '/apply/received',
    method: 'get',
    params,
  })
}

/** 企业：处理投递 */
export function auditApply(data) {
  return request({
    url: '/apply/audit',
    method: 'post',
    data,
  })
}
