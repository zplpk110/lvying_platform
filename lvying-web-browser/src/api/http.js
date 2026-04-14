/**
 * Axios：/api 由 Vite 代理到后端；401 跳转登录。
 */
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import router from '../router'

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
      const redirect = router.currentRoute.value.fullPath
      router.replace({ name: 'login', query: redirect !== '/login' ? { redirect } : {} })
    }
    return Promise.reject(err)
  }
)

export default http
