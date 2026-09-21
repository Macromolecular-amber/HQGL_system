import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  // 部署在 nginx 的 /app/ 路径，资源引用需带前缀
  base: '/app/',
  // 独立端口：不占用现有 Web 前端 dev server 的 5173，互不干扰
  server: {
    port: 5174,
    strictPort: true,
  },
})
