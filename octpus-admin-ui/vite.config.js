import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3010,
    host: '0.0.0.0',
    proxy: {
      '/octpus': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/service.do': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
