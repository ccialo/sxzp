import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, clearToken } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 401 弹窗去重：多个请求同时 401 时只弹一次重新登录框
let handling401 = false

// 请求拦截：注入 JWT
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：拆 Result<T>
service.interceptors.response.use(
  (response) => {
    // blob 等原始响应直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 业务失败
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message
    // 开发诊断：打印失败的接口与方法，便于定位 401/403 来源
    console.error(
      `[api] ${(error.config?.method || '').toUpperCase()} ${error.config?.url || ''} -> ${status || 'network'} ${msg}`
    )
    if (status === 401) {
      // 公开接口（数据大屏等）匿名访问时不做登录态处理，直接返回失败
      if ((error.config?.url || '').startsWith('/public/')) {
        return Promise.reject(error)
      }
      const hasToken = !!getToken()
      clearToken()
      // 已登录用户 token 过期才提示；未登录匿名访问直接静默失败（如岗位检索页）
      if (hasToken && !handling401) {
        handling401 = true
        ElMessageBox.confirm('登录状态已失效，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            router.push({
              path: '/login',
              query: { redirect: router.currentRoute.value.fullPath },
            })
          })
          .catch(() => {})
          .finally(() => {
            handling401 = false
          })
      }
    } else if (status === 403) {
      ElMessage.error('权限不足：' + (msg || '无权访问'))
    } else {
      ElMessage.error(msg || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
