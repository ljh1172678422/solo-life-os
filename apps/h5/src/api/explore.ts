import { request } from './request'
import type {
  CreateFavoriteRequest,
  CreateLocationRequest,
  Favorite,
  FavoriteTarget,
  Location,
  LocationType,
  UpdateLocationRequest,
} from './types'

/**
 * Explore Module API（对齐后端 TASK-0304 LocationController + FavoriteController）。
 *
 * 后端契约：
 * - Location：5 端点（create / list / getById / nearby / update）
 * - Favorite：4 端点（create / delete / list / check）
 * - 所有端点需 JWT Header，@AuthenticationPrincipal userId 自动注入
 */

/* ------------------------------------------------------------------ */
/* Location API                                                         */
/* ------------------------------------------------------------------ */

/** 创建地点（后端兜底同名校验，并发冲突转 409 BusinessException）。 */
export function createLocation(payload: CreateLocationRequest): Promise<Location> {
  return request<Location>('/api/locations', { method: 'POST', body: payload })
}

/**
 * 查询地点列表。
 *
 * 后端支持 3 档过滤：
 * 1. 仅 city（按城市返回所有地点）
 * 2. city + type（按类型过滤）
 * 3. 仅 page + size（全量分页，调试用）
 *
 * @param filters.city 城市名，空则不传
 * @param filters.type 地点类型枚举，空则不传
 */
export function listLocations(filters?: {
  city?: string
  type?: LocationType
  page?: number
  size?: number
}): Promise<Location[]> {
  const params = new URLSearchParams()
  if (filters?.city) params.set('city', filters.city)
  if (filters?.type) params.set('type', filters.type)
  if (filters?.page !== undefined) params.set('page', String(filters.page))
  if (filters?.size !== undefined) params.set('size', String(filters.size))
  const query = params.toString() ? `?${params.toString()}` : ''
  return request<Location[]>(`/api/locations${query}`, { method: 'GET' })
}

/** 按 id 获取地点详情。 */
export function getLocationById(id: number): Promise<Location> {
  return request<Location>(`/api/locations/${id}`, { method: 'GET' })
}

/**
 * 附近搜索（按经纬度矩形范围，默认 2km）。
 *
 * 后端逻辑：latitude -0.01 / +0.01 × longitude -0.01 / +0.01
 * （0.01° ≈ 1.1km，即 2km × 2km 范围）。
 */
export function getNearbyLocations(params: {
  latitude: number
  longitude: number
  radiusKm?: number
  type?: LocationType
  limit?: number
}): Promise<Location[]> {
  const qs = new URLSearchParams()
  qs.set('latitude', String(params.latitude))
  qs.set('longitude', String(params.longitude))
  if (params.radiusKm !== undefined) qs.set('radiusKm', String(params.radiusKm))
  if (params.type) qs.set('type', params.type)
  if (params.limit !== undefined) qs.set('limit', String(params.limit))
  return request<Location[]>(`/api/locations/nearby?${qs.toString()}`, { method: 'GET' })
}

/** 更新地点信息（业务不变量校验：营业时间 / 评分范围 / 类型）。 */
export function updateLocation(id: number, payload: UpdateLocationRequest): Promise<Location> {
  return request<Location>(`/api/locations/${id}`, { method: 'PUT', body: payload })
}

/* ------------------------------------------------------------------ */
/* Favorite API                                                         */
/* ------------------------------------------------------------------ */

/** 添加收藏（后端兜底 uk_user_target 唯一性，重复添加幂等返回已存在记录）。 */
export function createFavorite(payload: CreateFavoriteRequest): Promise<Favorite> {
  return request<Favorite>('/api/favorites', { method: 'POST', body: payload })
}

/** 取消收藏（软删除，deleted_time 置为当前时间）。 */
export function deleteFavorite(targetType: FavoriteTarget, targetId: number): Promise<void> {
  return request<void>(`/api/favorites?targetType=${targetType}&targetId=${targetId}`, {
    method: 'DELETE',
  })
}

/**
 * 查询收藏列表。
 *
 * @param targetType 不传返回全部，按创建时间 DESC
 */
export function listFavorites(targetType?: FavoriteTarget): Promise<Favorite[]> {
  const query = targetType ? `?targetType=${targetType}` : ''
  return request<Favorite[]>(`/api/favorites${query}`, { method: 'GET' })
}

/** 检查单个目标的收藏状态。返回 true=已收藏 / false=未收藏。 */
export function checkFavorite(targetType: FavoriteTarget, targetId: number): Promise<boolean> {
  return request<boolean>(`/api/favorites/check?targetType=${targetType}&targetId=${targetId}`, {
    method: 'GET',
  })
}
