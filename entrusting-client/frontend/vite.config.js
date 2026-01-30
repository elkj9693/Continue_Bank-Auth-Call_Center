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
      port: parseInt(env.VITE_PORT) || 5175,
      strictPort: true,
      host: true,
      allowedHosts: [
        'localhost',
        '127.0.0.1',
        '.sandbox.novita.ai',
        '5175-izfws6awf7kw9bujey22n-c81df28e.sandbox.novita.ai'
      ],
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_URL || 'http://127.0.0.1:8085',
          changeOrigin: true,
        },
        '/trustee-api': {
          target: env.VITE_TRUSTEE_URL || 'http://127.0.0.1:8086',
          rewrite: (path) => path.replace(/^\/trustee-api/, '/api'),
          changeOrigin: true,
        }
      }
      // 포트 기본값: 위탁사 백엔드 8085, 수탁사 백엔드 8086
    },
  }
})
