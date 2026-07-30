import { request } from './request'
import type {
  CreateTagRequest,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  Tag,
  UpdatePreferenceRequest,
  UpdateProfileRequest,
  UserPreference,
  UserProfile,
} from './types'

/**
 * Auth API（ADR-0006 JWT）。
 */

/** 登录：账号（邮箱/手机号）+ 密码 -> JWT token。 */
export function login(payload: LoginRequest): Promise<LoginResponse> {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: payload,
    skipAuth: true,
  })
}

/**
 * User API。
 */

/** 注册新用户（含 BCrypt 哈希密码 + 默认偏好）。 */
export function registerUser(payload: RegisterRequest): Promise<UserProfile> {
  return request<UserProfile>('/api/users', {
    method: 'POST',
    body: payload,
    skipAuth: true,
  })
}

/** 获取用户资料。 */
export function getUser(userId: number): Promise<UserProfile> {
  return request<UserProfile>(`/api/users/${userId}`, { method: 'GET' })
}

/** 更新用户资料（昵称 / 头像 / 城市）。 */
export function updateUserProfile(userId: number, payload: UpdateProfileRequest): Promise<UserProfile> {
  return request<UserProfile>(`/api/users/${userId}`, { method: 'PUT', body: payload })
}

/**
 * User Preference API。
 */

/** 获取用户偏好。 */
export function getUserPreference(userId: number): Promise<UserPreference> {
  return request<UserPreference>(`/api/users/${userId}/preference`, { method: 'GET' })
}

/** 更新用户偏好。 */
export function updateUserPreference(userId: number, payload: UpdatePreferenceRequest): Promise<UserPreference> {
  return request<UserPreference>(`/api/users/${userId}/preference`, { method: 'PUT', body: payload })
}

/**
 * Tag API。
 */

/** 创建标签。 */
export function createTag(userId: number, payload: CreateTagRequest): Promise<Tag> {
  return request<Tag>(`/api/users/${userId}/tags`, { method: 'POST', body: payload })
}

/** 查询用户标签（可选按类型筛选）。 */
export function listTags(userId: number, type?: string): Promise<Tag[]> {
  const query = type ? `?type=${encodeURIComponent(type)}` : ''
  return request<Tag[]>(`/api/users/${userId}/tags${query}`, { method: 'GET' })
}
