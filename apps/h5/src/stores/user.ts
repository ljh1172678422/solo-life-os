import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { LoginResponse, UserProfile } from '@/api/types'

const TOKEN_KEY = 'solo_token'
const USER_KEY = 'solo_user'

/**
 * 用户认证状态 Store (ADR-0006 JWT)。
 *
 * 持久化策略：token 与基础用户信息写入 localStorage，刷新页面后保持登录态。
 * 401 时由 request 模块调用 clearAuth 清除。
 */
export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<UserProfile | null>(loadUser())

  const isLoggedIn = computed(() => !!token.value)
  const userId = computed(() => user.value?.id ?? null)
  const nickname = computed(() => user.value?.nickname ?? '')

  /** 登录成功后设置 token 与用户基础信息。 */
  function setAuth(auth: LoginResponse): void {
    token.value = auth.token
    localStorage.setItem(TOKEN_KEY, auth.token)
    const partial: Partial<UserProfile> = {
      id: auth.userId,
      nickname: auth.nickname,
    }
    // 仅持久化基础字段，完整资料后续通过 getUser 拉取
    localStorage.setItem(USER_KEY, JSON.stringify(partial))
    user.value = partial as UserProfile
  }

  /** 更新完整用户资料（getUserProfile 后调用）。 */
  function setUser(profile: UserProfile): void {
    user.value = profile
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
  }

  /** 清除认证状态（登出 / 401 触发）。 */
  function clearAuth(): void {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  /** 是否已登录（模板守卫用）。 */
  function requireAuth(): boolean {
    return isLoggedIn.value
  }

  return {
    token,
    user,
    isLoggedIn,
    userId,
    nickname,
    setAuth,
    setUser,
    clearAuth,
    requireAuth,
  }
})

function loadUser(): UserProfile | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}
