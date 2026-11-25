import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

/**
 * 根据用户角色跳转到默认首页
 */
export function getHomeRoute(userType) {
  switch (userType) {
    case 'student':
      return '/student/applies'
    case 'company':
      return '/company/jobs'
    case 'admin':
      return '/admin/dashboard'
    default:
      return '/jobs'
  }
}

/**
 * 路由守卫：登录态、角色校验、用户信息恢复
 */
export function setupRouterGuard(router) {
  router.beforeEach(async (to, from, next) => {
    // 设置标题
    document.title = to.meta.title ? `${to.meta.title} - 校园实习招聘` : '校园实习招聘'

    const authStore = useAuthStore()
    const isLoggedIn = authStore.isLoggedIn

    // 需要登录
    if (to.meta.requireAuth) {
      if (!isLoggedIn) {
        return next({
          path: '/login',
          query: { redirect: to.fullPath },
        })
      }
      // 已有 token 但 user 为空（页面刷新），恢复用户信息
      if (!authStore.user) {
        try {
          await authStore.fetchMe()
        } catch {
          authStore.reset()
          return next({
            path: '/login',
            query: { redirect: to.fullPath },
          })
        }
      }
      // 角色校验
      const roles = to.meta.roles
      if (roles && roles.length && !roles.includes(authStore.userType)) {
        ElMessage.error('无权访问该页面')
        return next({ path: getHomeRoute(authStore.userType) })
      }
      return next()
    }

    // 已登录访问登录页/注册页，跳到对应首页
    if (isLoggedIn && (to.path === '/login' || to.path === '/register')) {
      return next({ path: getHomeRoute(authStore.userType) })
    }

    return next()
  })
}
