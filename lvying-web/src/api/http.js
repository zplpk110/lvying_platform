/**
 * Axios 实例：基址 /api 由 Vite 代理到后端；请求自动带 JWT，401 时清空登录态。
 */
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

http.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      auth.clear()
      window.location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

export default http
