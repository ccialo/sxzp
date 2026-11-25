import request from './request'

/** 企业：发起实习协议 */
export function saveAgreement(data) {
  return request({
    url: '/agreement/save',
    method: 'post',
    data,
  })
}

/** 学生：我的协议列表 */
export function myAgreements(params) {
  return request({
    url: '/agreement/my',
    method: 'get',
    params,
  })
}

/** 企业：我发出的协议列表 */
export function sentAgreements(params) {
  return request({
    url: '/agreement/sent',
    method: 'get',
    params,
  })
}

/** 学生：签署协议 */
export function signAgreement(agreementId) {
  return request({
    url: `/agreement/sign/${agreementId}`,
    method: 'put',
  })
}

/** 学生：拒绝协议 */
export function rejectAgreement(agreementId) {
  return request({
    url: `/agreement/reject/${agreementId}`,
    method: 'put',
  })
}
