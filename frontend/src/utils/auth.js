const TOKEN_KEY = 'sxzp_token'
const USER_KEY = 'sxzp_user'

/**
 * 解析 JWT payload 的过期时间（exp，单位秒）；无法解析时返回 0。
 */
function getTokenExpireAt(token) {
  try {
    const part = token.split('.')[1]
    if (!part) return 0
    const b64 = part.replace(/-/g, '+').replace(/_/g, '/')
    const padded = b64 + '='.repeat((4 - (b64.length % 4)) % 4)
    const payload = JSON.parse(atob(padded))
    return Number(payload.exp) || 0
  } catch {
    return 0
  }
}

export function getToken() {
  const token = localStorage.getItem(TOKEN_KEY) || ''
  // token 已过期则视为未登录并清理本地登录态，避免带着失效 token 进入受保护页面
  const exp = getTokenExpireAt(token)
  if (exp > 0 && exp * 1000 <= Date.now()) {
    clearToken()
    return ''
  }
  return token
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}
