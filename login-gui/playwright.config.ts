import { defineConfig, devices } from '@playwright/test';

const port = 3004;

export default defineConfig({
    testDir: './e2e',
    forbidOnly: Boolean(process.env.CI),
    retries: process.env.CI ? 1 : 0,
    reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
    use: {
        testIdAttribute: 'data-testid',
        locale: 'en-US',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
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
