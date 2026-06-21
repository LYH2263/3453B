import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

interface ApiResponse<T = unknown> {
    code: number
    message: string
    data: T
}

const instance = axios.create({
    baseURL: '/api',
    timeout: 10000
})

instance.interceptors.request.use(
    (config) => {
        const userStr = localStorage.getItem('user')
        if (userStr) {
            const user = JSON.parse(userStr)
            if (user.token) {
                config.headers.Authorization = `Bearer ${user.token}`
            }
        }
        return config
    },
    (error) => Promise.reject(error)
)

instance.interceptors.response.use(
    (response) => {
        const res = response.data as ApiResponse
        if (res.code !== 200) {
            ElMessage.error(res.message || '操作失败')
            return Promise.reject(new Error(res.message || '操作失败'))
        }
        return res.data as any
    },
    (error) => {
        let message = '网络错误，请稍后再试'
        if (error.response) {
            const { status, data } = error.response
            if (status === 401) {
                message = '登录已过期，请重新登录'
                localStorage.removeItem('user')
                window.location.href = '/login'
            } else if (status === 403) {
                message = '没有权限访问该资源'
            } else if (data && data.message) {
                message = data.message
            }
        } else if (error.message && error.message.includes('timeout')) {
            message = '请求超时'
        }
        ElMessage.error(message)
        return Promise.reject(error)
    }
)

const request = {
    get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
        return instance.get(url, config) as Promise<T>
    },
    post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
        return instance.post(url, data, config) as Promise<T>
    },
    put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
        return instance.put(url, data, config) as Promise<T>
    },
    delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
        return instance.delete(url, config) as Promise<T>
    }
}

export default request
