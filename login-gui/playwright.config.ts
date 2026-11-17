import { defineConfig, devices } from '@playwright/test';

const port = 3004;

export default defineConfig({
    testDir: './e2e',
    use: {
        testIdAttribute: 'data-testid',
        locale: 'en-US',
        baseURL: process.env.E2E_BASE_URL ?? `http://localhost:${port}`,
    },
    projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
    webServer: process.env.E2E_BASE_URL
        ? undefined
        : {
              command: `yarn dev --port ${port} --no-open`,
              url: `http://localhost:${port}`,
              reuseExistingServer: true,
              timeout: 120_000,
          },
});
