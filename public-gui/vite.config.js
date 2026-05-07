import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-oxc'
import svgr from 'vite-plugin-svgr'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react(), svgr()],
    server: {
        port: 3002,
        open: true,
        proxy: {
            '/config': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false
            },
        }
    },
})
