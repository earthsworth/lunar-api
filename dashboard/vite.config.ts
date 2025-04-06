import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    base: "./",
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080/api',
                changeOrigin: true,
                rewrite: path => path.replace(/^\/api/, '')
            },
            '/ws': {
                target: 'ws://localhost:8080/ws',
                ws: true,
                changeOrigin: true,
                rewrite: path => path.replace(/^\/ws/, '')
            }
        }
    }
})
