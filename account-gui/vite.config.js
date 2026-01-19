import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import legacy from '@vitejs/plugin-legacy'

export default defineConfig({
    plugins: [
        svelte({
            /* plugin options */
        }),
        legacy({
            targets: ['ie >= 11'],
            additionalLegacyPolyfills: ['regenerator-runtime/runtime'],
        })
    ],
    server: {
        port: 3000,
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
            },
            '/register': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false
            },
            '/tiqr': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false
            }
        }
    },
});