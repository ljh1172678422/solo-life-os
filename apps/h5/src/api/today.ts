import { request } from './request'
import type {
  Activity,
  CreateActivityRequest,
  CreateDailyPlanRequest,
  DailyPlan,
  EndActivityRequest,
  LocateActivityRequest,
  PlanStatus,
  UpdateActivityRequest,
} from './types'

/**
 * Today Module API（对齐后端 TASK-0204，13 个端点）。
 *
 * 后端契约：
 * - DailyPlan：7 个端点（create / getToday / list / getById / start / complete / cancel）
 * - Activity：6 个端点（add / list / getById / update / end / locate）
 * - 无 DELETE：DailyPlan / Activity 均通过 JPA @SQLDelete 软删除，控制器层未暴露
 * - 无 Activity start：活动无状态机，"开始" 即创建时设置 startTime
 * - getToday 无计划时返回 200 + data:null（非 404），调用方需处理 null
 */

/**
 * DailyPlan API。
 */

/** 创建每日计划（每用户每日唯一，DB uk_daily_plan_user_date 兜底）。 */
export function createDailyPlan(userId: number, payload: CreateDailyPlanRequest): Promise<DailyPlan> {
  return request<DailyPlan>(`/api/users/${userId}/plans`, { method: 'POST', body: payload })
}

/** 查询今日计划：返回 null 表示当日尚无计划（非异常）。 */
export function getTodayPlan(userId: number, date: string): Promise<DailyPlan | null> {
  const query = `?date=${encodeURIComponent(date)}`
  return request<DailyPlan | null>(`/api/users/${userId}/plans/today${query}`, { method: 'GET' })
}

/** 查询用户计划列表：三档逻辑（startDate+endDate 区间 / status 状态 / 全部）。 */
export function listDailyPlans(
  userId: number,
  filters?: { startDate?: string; endDate?: string; status?: PlanStatus },
): Promise<DailyPlan[]> {
  const params = new URLSearchParams()
  if (filters?.startDate) params.set('startDate', filters.startDate)
  if (filters?.endDate) params.set('endDate', filters.endDate)
  if (filters?.status) params.set('status', filters.status)
  const query = params.toString() ? `?${params.toString()}` : ''
  return request<DailyPlan[]>(`/api/users/${userId}/plans${query}`, { method: 'GET' })
}

/** 按计划 ID 查询。 */
export function getDailyPlan(planId: number): Promise<DailyPlan> {
  return request<DailyPlan>(`/api/plans/${planId}`, { method: 'GET' })
}

/** 开始计划：PLANNING → ONGOING。 */
export function startDailyPlan(planId: number): Promise<DailyPlan> {
  return request<DailyPlan>(`/api/plans/${planId}/start`, { method: 'POST' })
}

/** 完成计划：ONGOING → COMPLETED。 */
export function completeDailyPlan(planId: number): Promise<DailyPlan> {
  return request<DailyPlan>(`/api/plans/${planId}/complete`, { method: 'POST' })
}

/** 取消计划：PLANNING / ONGOING → CANCELLED。 */
export function cancelDailyPlan(planId: number): Promise<DailyPlan> {
  return request<DailyPlan>(`/api/plans/${planId}/cancel`, { method: 'POST' })
}

/**
 * Activity API。
 */

/** 在计划下添加活动（type 可空，后端兜底为 OTHER）。 */
export function createActivity(planId: number, payload: CreateActivityRequest): Promise<Activity> {
  return request<Activity>(`/api/plans/${planId}/activities`, { method: 'POST', body: payload })
}

/** 按计划 ID 查活动列表（按 startTime 正序）。 */
export function listActivities(planId: number): Promise<Activity[]> {
  return request<Activity[]>(`/api/plans/${planId}/activities`, { method: 'GET' })
}

/** 按活动 ID 查询。 */
export function getActivity(activityId: number): Promise<Activity> {
  return request<Activity>(`/api/activities/${activityId}`, { method: 'GET' })
}

/** 修改活动（整体替换语义，type 必填）。 */
export function updateActivity(
  planId: number,
  activityId: number,
  payload: UpdateActivityRequest,
): Promise<Activity> {
  return request<Activity>(`/api/plans/${planId}/activities/${activityId}`, {
    method: 'PUT',
    body: payload,
  })
}

/** 设置活动结束时间（endTime=null 表示清除）。 */
export function endActivity(activityId: number, payload: EndActivityRequest): Promise<Activity> {
  return request<Activity>(`/api/activities/${activityId}/end`, { method: 'POST', body: payload })
}

/** 绑定活动地点（Explore Module, Sprint 3 后可用）。 */
export function locateActivity(
  activityId: number,
  payload: LocateActivityRequest,
): Promise<Activity> {
  return request<Activity>(`/api/activities/${activityId}/locate`, { method: 'POST', body: payload })
}
