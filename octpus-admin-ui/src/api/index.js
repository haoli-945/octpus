import axios from 'axios'

const api = axios.create({
  baseURL: '/octpus/admin',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器
api.interceptors.response.use(
  res => {
    if (res.data.code === '000000') {
      return res.data.data
    }
    return Promise.reject(new Error(res.data.message || '请求失败'))
  },
  err => Promise.reject(err)
)

// ==================== Dashboard ====================
export const getDashboardStats = () => api.get('/dashboard/stats')

// ==================== System ====================
export const getSystemList = () => api.get('/system/list')
export const getSystemById = (id) => api.get(`/system/${id}`)
export const createSystem = (data) => api.post('/system', data)
export const updateSystem = (id, data) => api.put(`/system/${id}`, data)
export const deleteSystem = (id) => api.delete(`/system/${id}`)
export const toggleSystemStatus = (id) => api.patch(`/system/${id}/status`)

// ==================== Service ====================
export const getServiceList = (systemCode) => {
  const params = systemCode ? { systemCode } : {}
  return api.get('/service/list', { params })
}
export const getServiceById = (id) => api.get(`/service/${id}`)
export const createService = (data) => api.post('/service', data)
export const updateService = (id, data) => api.put(`/service/${id}`, data)
export const deleteService = (id) => api.delete(`/service/${id}`)
export const toggleServiceStatus = (id) => api.patch(`/service/${id}/status`)
export const flushServiceCache = () => api.post('/service/cache/flush')
