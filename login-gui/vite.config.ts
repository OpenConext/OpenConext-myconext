import { existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { type Plugin, defineConfig } from 'vite';

import { reactRouter } from '@react-router/dev/vite';

// The prerender of the SPA fallback starts a second vite instance whose file
// watchers keep the process alive, which stalls mvn install
const exitAfterBuild = (): Plugin => ({
    name: 'exit-after-build',
    enforce: 'post',
    apply: 'build',
    buildApp: {
        order: 'post',
        handler() {
            // Exit only once the prerender (which runs concurrently with this hook) has written the
            // index.html; exiting at once aborts its request
            const indexHtml = resolve('build/client/index.html');
            const timer = setInterval(() => {
                if (existsSync(indexHtml)) {
                    clearInterval(timer);
                    process.exit(0);
                }
            }, 200);
            return Promise.resolve();
        },
    },
});

// https://vite.dev/config/
export default defineConfig({
    plugins: [reactRouter(), exitAfterBuild()],
    resolve: {
        tsconfigPaths: true,
    },
    // the prerender of the SPA fallback requests the preview server, avoid localhost resolving to ::1
    preview: {
        host: '127.0.0.1',
    },
    server: {
        port: 3004,
        open: false,
        warmup: {
            clientFiles: ['./src/root.tsx', './src/routes/**/*.tsx'],
        },
        proxy: {
            '/config': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/tiqr': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/myconext/api': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/register': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/servicedesk': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
            '/saml/guest-idp': {
                target: 'http://localhost:8081',
                changeOrigin: false,
                secure: false,
            },
        },
    },
});
