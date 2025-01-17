import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3002,
        open: true,
        proxy: {
            '/myconext/api': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false
            },
            '/config': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false
            }
        }

    },
})
