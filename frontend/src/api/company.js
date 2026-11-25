import request from './request'

/** 我的企业档案 */
export function getProfile() {
  return request({
    url: '/company/profile',
    method: 'get',
  })
}

/** 完善/修改企业资料 */
export function saveProfile(data) {
  return request({
    url: '/company/profile',
    method: 'put',
    data,
  })
}

/** 企业详情 */
export function detail(companyId) {
  return request({
    url: `/company/detail/${companyId}`,
    method: 'get',
  })
}

// ===== 管理端 - 企业认证审核 =====

/** 待审核企业列表 */
export function pendingCompanies(params) {
  return request({
    url: '/admin/company/pending',
    method: 'get',
    params,
  })
}

/** 企业列表 */
export function pageCompanies(params) {
  return request({
    url: '/admin/company/page',
    method: 'get',
    params,
  })
}

/** 审核企业认证 */
export function auditCompany(data) {
  return request({
    url: '/admin/company/audit',
    method: 'post',
    data,
  })
}
