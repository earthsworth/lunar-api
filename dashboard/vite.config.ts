import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
    plugins: [react(), tailwindcss()],
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
