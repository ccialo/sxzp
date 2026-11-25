import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getToken, setToken, clearToken, getUser, setUser } from '@/utils/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)
  const userType = computed(() => user.value?.userType || '')
  const profileId = computed(() => user.value?.profileId || null)
  const realName = computed(() => user.value?.realName || user.value?.account || '')

  /** 登录 */
  async function login(form) {
    const data = await authApi.login(form)
    token.value = data.token
    user.value = {
      userId: data.userId,
      account: data.account,
      userType: data.userType,
      realName: data.realName,
      profileId: data.profileId,
    }
    setToken(data.token)
    setUser(user.value)
    return data
  }

  /** 注册 */
  async function register(form) {
    const data = await authApi.register(form)
    token.value = data.token
    user.value = {
      userId: data.userId,
      account: data.account,
      userType: data.userType,
      realName: data.realName,
      profileId: data.profileId,
    }
    setToken(data.token)
    setUser(user.value)
    return data
  }

  /** 获取当前用户（刷新页面后恢复） */
  async function fetchMe() {
    const data = await authApi.me()
    // me 返回的是 LoginUser，含 userId/account/userType，但可能没有 profileId
    // 保留 localStorage 中之前存的 profileId（避免丢失）
    const prev = getUser()
    user.value = {
      userId: data.userId,
      account: data.account,
      userType: data.userType,
      realName: data.realName || prev?.realName || data.account,
      profileId: prev?.profileId || null,
    }
    setUser(user.value)
    return user.value
  }

  /** 退出登录 */
  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 忽略后端失败
    }
    reset()
  }

  function reset() {
    token.value = ''
    user.value = null
    clearToken()
  }

  return {
    token,
    user,
    isLoggedIn,
    userType,
    profileId,
    realName,
    login,
    register,
    fetchMe,
    logout,
    reset,
  }
})
