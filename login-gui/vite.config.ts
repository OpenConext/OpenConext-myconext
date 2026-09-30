import { defineConfig } from 'vite';

import { reactRouter } from '@react-router/dev/vite';

// https://vite.dev/config/
export default defineConfig({
    plugins: [reactRouter()],
    resolve: {
        tsconfigPaths: true,
    },
    server: {
        port: 3002,
        open: true,
        proxy: {
            '/config': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/myconext/api': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
        },
    },
});
