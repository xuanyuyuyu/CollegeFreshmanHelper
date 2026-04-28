import axios from 'axios'

const http = axios.create({
  baseURL: '/',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('cfh_token')
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result && typeof result.code !== 'undefined') {
      if (result.code !== 200) {
        return Promise.reject(new Error(result.message || '请求失败'))
      }
      return result.data
    }
    return result
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络异常'
    return Promise.reject(new Error(message))
  }
)

export default http
