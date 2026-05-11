import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        // 与 Spring Boot 后端保持一致：端口 8080，context-path 为 /api
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

