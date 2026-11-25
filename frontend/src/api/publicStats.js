import request from './request'

/**
 * 数据大屏公开统计接口（无需登录）
 */

export function screenDashboard() {
  return request({ url: '/public/stats/dashboard', method: 'get' })
}

export function screenApplyTrend(params) {
  return request({ url: '/public/stats/apply-trend', method: 'get', params })
}

export function screenJobRanking(params) {
  return request({ url: '/public/stats/job-ranking', method: 'get', params })
}

export function screenJobTypeDistribution() {
  return request({ url: '/public/stats/job-type-distribution', method: 'get' })
}

export function screenCollegeDistribution(params) {
  return request({ url: '/public/stats/college-distribution', method: 'get', params })
}

export function screenCompanyAuthDistribution() {
  return request({ url: '/public/stats/company-auth-distribution', method: 'get' })
}

export function screenApplySuccess() {
  return request({ url: '/public/stats/apply-success', method: 'get' })
}

export function screenRecentApplies(params) {
  return request({ url: '/public/stats/recent-applies', method: 'get', params })
}
