import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import { defineConfig, globalIgnores } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

import js from '@eslint/js';

export default defineConfig([
    globalIgnores(['dist', '.react-router']),
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            js.configs.recommended,
            tseslint.configs.recommended,
            reactHooks.configs.flat.recommended,
            reactRefresh.configs.vite,
        ],
        languageOptions: {
            globals: globals.browser,
        },
        rules: {
            curly: ['error', 'all'],
            'no-console': ['error', { allow: ['error', 'warn'] }],
            'no-restricted-syntax': [
                'error',
                {
                    selector: 'CallExpression[callee.property.name="then"]',
                    message: 'Avoid .then(). Use async/await instead.',
                },
                {
                    selector: 'CallExpression[callee.property.name="catch"]',
                    message:
                        'Avoid .catch(). Use try/catch with async/await instead.',
                },
                {
                    selector: 'CallExpression[callee.property.name="finally"]',
                    message:
                        'Avoid .finally(). Use try/finally with async/await instead.',
                },
            ],
        },
    },
    {
        // React Router's Vite plugin handles HMR for these route module exports
        files: ['src/routes/**/route.tsx'],
        rules: {
            'react-refresh/only-export-components': [
                'error',
                {
                    allowExportNames: [
                        'clientLoader',
                        'clientAction',
                        'loader',
                        'action',
                        'meta',
                        'links',
                        'handle',
                        'headers',
                        'shouldRevalidate',
                        'ErrorBoundary',
                        'HydrateFallback',
                    ],
                },
            ],
        },
    },
]);
