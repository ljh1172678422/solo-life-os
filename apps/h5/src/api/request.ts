import { useUserStore } from '@/stores/user'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * 统一后端响应格式（ARCHITECTURE §11 API Boundary）。
 * code = 0 表示成功，非 0 表示业务/系统错误。
 */
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  traceId: string
}

/**
 * 请求异常（HTTP 层错误或 code != 0）。
 * 调用方可通过 instanceof ApiError 区分业务错误与网络异常。
 */
export class ApiError extends Error {
  constructor(
    public readonly code: number,
    message: string,
    public readonly httpStatus?: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  /** 标记为认证请求，跳过 token 注入（login/register 自身不需要 token）。 */
  skipAuth?: boolean
}

/**
 * 统一请求入口 (CODE_RULES §2 Frontend: 禁组件直连 fetch，必经 api/ 封装)。
 *
 * 职责：
 * 1. 拼接 BASE_URL
 * 2. 注入 Content-Type 与 Authorization（除 skipAuth 请求）
 * 3. 序列化 body
 * 4. HTTP 非 2xx 抛 ApiError
 * 5. code != 0 抛 ApiError（401 时清除 token 并跳登录）
 */
export async function request<T = unknown>(
  url: string,
  options: RequestOptions = {},
): Promise<T> {
  const { body, headers, skipAuth, ...rest } = options

  const finalHeaders: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(headers as Record<string, string>),
  }

  if (!skipAuth) {
    const userStore = useUserStore()
    if (userStore.token) {
      finalHeaders['Authorization'] = `Bearer ${userStore.token}`
    }
  }

  const response = await fetch(`${BASE_URL}${url}`, {
    ...rest,
    headers: finalHeaders,
    body: body === undefined || body === null ? undefined : JSON.stringify(body),
  })

  // 401: token 失效，清除并跳转登录（避免循环依赖，直接用 location 跳转）
  if (response.status === 401) {
    const userStore = useUserStore()
    userStore.clearAuth()
    redirectToLogin()
    throw new ApiError(1003, '认证失败，请重新登录', 401)
  }

  // HTTP 层错误（非 2xx 且非 401）
  if (!response.ok) {
    throw new ApiError(response.status, `HTTP ${response.status}`, response.status)
  }

  const payload = (await response.json()) as ApiResponse<T>

  // 业务层错误
  if (payload.code !== 0) {
    throw new ApiError(payload.code, payload.message || '请求失败')
  }

  return payload.data
}

/** 避免在 request 模块直接 import router 造成循环依赖，用 uni 原生 API 跳转。 */
function redirectToLogin(): void {
  // uni-app H5 端 reLaunch 关闭所有页面栈，避免返回到需认证页面
  uni.reLaunch({ url: '/pages/login/index' })
}
