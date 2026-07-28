const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  traceId: string
}

export async function request<T = any>(
  url: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const response = await fetch(`${BASE_URL}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  })

  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }

  return response.json()
}

export { request }
