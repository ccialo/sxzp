import request from './request'

/** 岗位检索 */
export function searchJobs(data) {
  return request({
    url: '/job/search',
    method: 'post',
    data,
  })
}

/** 岗位详情 */
export function jobDetail(jobId) {
  return request({
    url: `/job/detail/${jobId}`,
    method: 'get',
  })
}

/** 企业发布/修改岗位 */
export function saveJob(data) {
  return request({
    url: '/job/save',
    method: 'post',
    data,
  })
}

/** 企业：我的岗位列表 */
export function myJobs(params) {
  return request({
    url: '/job/my',
    method: 'get',
    params,
  })
}

/** 企业下线岗位 */
export function offlineJob(jobId) {
  return request({
    url: `/job/offline/${jobId}`,
    method: 'put',
  })
}

/** 企业重新提交岗位审核 */
export function resubmitJob(jobId) {
  return request({
    url: `/job/resubmit/${jobId}`,
    method: 'put',
  })
}

// ===== 管理端 - 岗位审核 =====

/** 岗位列表（管理员） */
export function adminJobPage(params) {
  return request({
    url: '/admin/job/page',
    method: 'get',
    params,
  })
}

/** 待审核岗位列表 */
export function adminJobPending(params) {
  return request({
    url: '/admin/job/pending',
    method: 'get',
    params,
  })
}

/** 审核岗位 */
export function auditJob(data) {
  return request({
    url: '/admin/job/audit',
    method: 'post',
    data,
  })
}
