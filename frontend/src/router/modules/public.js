/**
 * 公共路由：匿名可访问
 */
export default [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/public/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/public/Register.vue'),
    meta: { title: '注册' },
  },
  {
    path: '/jobs',
    name: 'JobSearch',
    component: () => import('@/views/public/JobSearch.vue'),
    meta: { title: '岗位检索' },
  },
  {
    path: '/jobs/:id',
    name: 'JobDetail',
    component: () => import('@/views/public/JobDetail.vue'),
    meta: { title: '岗位详情' },
  },
  {
    path: '/companies/:id',
    name: 'CompanyDetail',
    component: () => import('@/views/public/CompanyDetail.vue'),
    meta: { title: '企业详情' },
  },
]
