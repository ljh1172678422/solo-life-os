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

/**
 * Today Module 类型定义（对齐后端 DTO，CODE_RULES §2 禁 any）。
 */

/** 计划状态（对齐后端 PlanStatus，DATABASE_DESIGN §7 PLAN_STATUS）。 */
export type PlanStatus = 'PLANNING' | 'ONGOING' | 'COMPLETED' | 'CANCELLED'

/** 活动类型（对齐后端 ActivityType，DATABASE_DESIGN §7 ACTIVITY_TYPE）。 */
export type ActivityType =
  | 'WORK'
  | 'LEISURE'
  | 'SPORT'
  | 'STUDY'
  | 'SOCIAL'
  | 'EXPLORE'
  | 'REST'
  | 'OTHER'

/** 每日计划（对齐后端 DailyPlanResponse）。 */
export interface DailyPlan {
  id: number
  userId: number
  date: string
  status: PlanStatus
  createdTime: string
  updatedTime: string
}

/** 活动（对齐后端 ActivityResponse）。locationId / endTime 可空。 */
export interface Activity {
  id: number
  dailyPlanId: number
  title: string
  type: ActivityType
  locationId: number | null
  startTime: string
  endTime: string | null
  createdTime: string
  updatedTime: string
}

/** 创建每日计划请求体（userId 来自路径变量）。 */
export interface CreateDailyPlanRequest {
  date: string
}

/** 创建活动请求体（planId 来自路径变量）。type 可空，后端兜底为 OTHER。 */
export interface CreateActivityRequest {
  title: string
  type?: ActivityType | null
  startTime: string
}

/** 修改活动请求体（整体替换语义，endTime 可空）。 */
export interface UpdateActivityRequest {
  title: string
  type: ActivityType
  startTime: string
  endTime: string | null
}

/** 设置活动结束时间请求体（endTime 可空表示清除）。 */
export interface EndActivityRequest {
  endTime: string | null
}

/** 绑定活动地点请求体（Explore Module, Sprint 3 后可用）。 */
export interface LocateActivityRequest {
  locationId: number | null
}

/**
 * Explore Module 类型定义（对齐后端 TASK-0301~0304）。
 */

/** 地点类型（对齐后端 LocationType）。 */
export type LocationType =
  | 'CAFE'
  | 'BOOKSTORE'
  | 'PARK'
  | 'EXHIBITION'
  | 'SPORTS'
  | 'CINEMA'
  | 'RESTAURANT'
  | 'OTHER'

/** 收藏目标类型（对齐后端 FavoriteTarget）。 */
export type FavoriteTarget = 'LOCATION' | 'ROUTE' | 'ACTIVITY' | 'MOVIE'

/** 地点（对齐后端 LocationResponse）。 */
export interface Location {
  id: number
  name: string
  type: LocationType
  address: string
  city: string
  latitude: number
  longitude: number
  rating: number | null
  phone: string | null
  openingHours: string | null
  priceLevel: string | null
  tags: string | null
  description: string | null
  imageUrl: string | null
  createdTime: string
  updatedTime: string
}

/** 收藏（对齐后端 FavoriteResponse）。 */
export interface Favorite {
  id: number
  userId: number
  targetType: FavoriteTarget
  targetId: number
  targetName: string
  targetCover: string | null
  targetTypeLabel: string
  remark: string | null
  createdTime: string
}

/** 创建地点请求体。 */
export interface CreateLocationRequest {
  name: string
  type: LocationType
  address: string
  city: string
  latitude: number
  longitude: number
  rating?: number | null
  phone?: string | null
  openingHours?: string | null
  priceLevel?: string | null
  tags?: string | null
  description?: string | null
  imageUrl?: string | null
}

/** 更新地点请求体（部分字段可空）。 */
export interface UpdateLocationRequest {
  name: string
  type: LocationType
  address: string
  city: string
  latitude: number
  longitude: number
  rating?: number | null
  phone?: string | null
  openingHours?: string | null
  priceLevel?: string | null
  tags?: string | null
  description?: string | null
  imageUrl?: string | null
}

/** 创建收藏请求体。 */
export interface CreateFavoriteRequest {
  targetType: FavoriteTarget
  targetId: number
  targetName: string
  targetCover?: string | null
  targetTypeLabel?: string | null
  remark?: string | null
}
