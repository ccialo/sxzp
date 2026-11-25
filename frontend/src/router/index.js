import { createRouter, createWebHistory } from 'vue-router'
import publicRoutes from './modules/public'
import studentRoutes from './modules/student'
import companyRoutes from './modules/company'
import adminRoutes from './modules/admin'

// 基础布局
import BasicLayout from '@/layouts/BasicLayout.vue'
import PublicLayout from '@/layouts/PublicLayout.vue'

// 公共页面
import Home from '@/views/Home.vue'
import NotFound from '@/views/error/NotFound.vue'
import Forbidden from '@/views/error/Forbidden.vue'

export const constantRoutes = [
  {
    path: '/',
    component: Home,
    meta: { title: '首页' },
  },
  // 公共路由（部分使用 PublicLayout 包裹）
  {
    path: '/',
    component: PublicLayout,
    children: publicRoutes,
  },
  // 学生路由
  {
    path: '/student',
    component: BasicLayout,
    meta: { requireAuth: true, roles: ['student'] },
    children: studentRoutes,
  },
  // 企业路由
  {
    path: '/company',
    component: BasicLayout,
    meta: { requireAuth: true, roles: ['company'] },
    children: companyRoutes,
  },
  // 管理员路由
  {
    path: '/admin',
    component: BasicLayout,
    meta: { requireAuth: true, roles: ['admin'] },
    children: adminRoutes,
  },
  {
    path: '/screen',
    name: 'DataScreen',
    component: () => import('@/views/DataScreen.vue'),
    meta: { title: '数据大屏' },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: Forbidden,
    meta: { title: '无权访问' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
