/**
 * User Module 类型定义（对齐后端 DTO，CODE_RULES §2 禁 any）。
 */

/** 用户状态枚举（对齐后端 UserStatus）。 */
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'BANNED'

/** 预算等级（对齐后端 BudgetLevel）。 */
export type BudgetLevel = 'LOW' | 'MEDIUM' | 'HIGH'

/** 标签类型（对齐后端 TagType）。 */
export type TagType = 'INTEREST' | 'GOAL' | 'STYLE'

/** 用户资料。 */
export interface UserProfile {
  id: number
  nickname: string
  avatar: string | null
  email: string | null
  phone: string | null
  city: string | null
  status: UserStatus
  createdTime: string
}

/** 用户偏好。 */
export interface UserPreference {
  id: number
  userId: number
  interest: string | null
  budget: BudgetLevel | null
  lifestyle: string | null
  createdTime: string
  updatedTime: string
}

/** 标签。 */
export interface Tag {
  id: number
  userId: number
  name: string
  type: TagType
  createdTime: string
}

/** 登录请求体。 */
export interface LoginRequest {
  account: string
  password: string
}

/** 登录响应。 */
export interface LoginResponse {
  token: string
  userId: number
  nickname: string
}

/** 注册请求体。 */
export interface RegisterRequest {
  nickname: string
  email: string | null
  phone: string | null
  password: string
}

/** 更新资料请求体。 */
export interface UpdateProfileRequest {
  nickname: string
  avatar: string | null
  city: string | null
}

/** 更新偏好请求体。 */
export interface UpdatePreferenceRequest {
  interest: string | null
  budget: BudgetLevel | null
  lifestyle: string | null
}

/** 创建标签请求体。 */
export interface CreateTagRequest {
  name: string
  type: TagType
}
