import { request } from './request'

export function checkHealth() {
  return request<string>('/health')
}
