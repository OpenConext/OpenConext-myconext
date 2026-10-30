import { type Plugin, defineConfig } from 'vite';

import { reactRouter } from '@react-router/dev/vite';

// The prerender of the SPA fallback starts a second vite instance whose file
// watchers keep the process alive, which stalls mvn install
const exitAfterBuild = (): Plugin => ({
    name: 'exit-after-build',
    apply: 'build',
    buildApp: {
        order: 'post',
        handler() {
            setTimeout(() => process.exit(0), 0);
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
        port: 3000,
        open: false,
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
        },
    },
});
