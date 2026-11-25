import request from './request'

/** 仪表盘总览 */
export function dashboard() {
  return request({
    url: '/admin/stats/dashboard',
    method: 'get',
  })
}

/** 热门岗位排行 */
export function jobRanking(params) {
  return request({
    url: '/admin/stats/job-ranking',
    method: 'get',
    params,
  })
}

/** 投递趋势 */
export function applyTrend(params) {
  return request({
    url: '/admin/stats/apply-trend',
    method: 'get',
    params,
  })
}

/** 岗位类型分布 */
export function jobTypeDistribution() {
  return request({
    url: '/admin/stats/job-type-distribution',
    method: 'get',
  })
}

/** 学生学院分布 */
export function collegeDistribution(params) {
  return request({
    url: '/admin/stats/college-distribution',
    method: 'get',
    params,
  })
}

/** 企业认证状态分布 */
export function companyAuthDistribution() {
  return request({
    url: '/admin/stats/company-auth-distribution',
    method: 'get',
  })
}

/** 学生投递成功率（整体 + 按岗位类型） */
export function applySuccess() {
  return request({
    url: '/admin/stats/apply-success',
    method: 'get',
  })
}

/** 最近投递动态 */
export function recentApplies(params) {
  return request({
    url: '/admin/stats/recent-applies',
    method: 'get',
    params,
  })
}
