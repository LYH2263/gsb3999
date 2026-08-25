import axios from 'axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

const api = axios.create({
  baseURL: 'http://localhost:8000/api',
  timeout: 5000
})

api.interceptors.request.use(config => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

api.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      ElMessage.error(res.data.message || '请求错误')
      return Promise.reject(new Error(res.data.message))
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      const store = useUserStore()
      store.logout()
      router.push('/login')
      ElMessage.error('认证失败，请重新登录')
    } else {
      ElMessage.error(err.response?.data?.message || '网络或服务器错误')
    }
    return Promise.reject(err)
  }
)
export default api
