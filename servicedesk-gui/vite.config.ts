import { defineConfig } from 'vite';
import svgr from 'vite-plugin-svgr';

import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react(),
        svgr({
            // svgr options: https://react-svgr.com/docs/options/
            svgrOptions: {
                exportType: 'default',
                ref: true,
                svgo: false,
                titleProp: true,
            },
            include: '**/*.svg',
        }),
    ],
    server: {
        port: 3003,
        open: true,
        proxy: {
            '/myconext/api': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/config': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/doLogout': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
        },
    },
});
