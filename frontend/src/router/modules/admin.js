/**
 * 管理员路由
 */
export default [
  {
    path: 'dashboard',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '仪表盘' },
  },
  {
    path: 'analysis',
    name: 'AdminDataAnalysis',
    component: () => import('@/views/admin/DataAnalysis.vue'),
    meta: { title: '数据分析' },
  },
  {
    path: 'companies',
    name: 'AdminCompanyAudit',
    component: () => import('@/views/admin/CompanyAudit.vue'),
    meta: { title: '企业审核' },
  },
  {
    path: 'jobs',
    name: 'AdminJobAudit',
    component: () => import('@/views/admin/JobAudit.vue'),
    meta: { title: '岗位审核' },
  },
]
