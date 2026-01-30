import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [
      react(),
      tailwindcss(),
    ],
    server: {
      port: parseInt(env.VITE_PORT) || 5176,
      strictPort: true,
      host: true,
      allowedHosts: [
        'localhost',
        '127.0.0.1',
        '.sandbox.novita.ai',
        '5176-izfws6awf7kw9bujey22n-c81df28e.sandbox.novita.ai'
      ],
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8086',
          changeOrigin: true,
          secure: false,
        }
      }
      // 포트 기본값: 수탁사 백엔드 8086
    },
  }
})
