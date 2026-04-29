import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    // 监听所有网络接口，允许外部设备和内网穿透访问
    host: '0.0.0.0',
    port: 5173,

    // 允许 Cpolar 的域名访问，解决 Blocked request 报错
    allowedHosts: ['.cpolar.cn', '.cpolar.io', '.cpolar.top','.trycloudflare.com'],

    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/actuator': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      },
      '/pictures': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
})
