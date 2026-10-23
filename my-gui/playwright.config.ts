import { defineConfig, devices } from '@playwright/test';

// The tests expect the stack to be running: mongo and mailpit (docker compose),
// myconext-server with the `test` profile (:8081, every request is authenticated
// as the seeded user) and the tiqr-mock (:8088). The my-gui dev server is
// started here unless it is already running.
const port = 3001;

export default defineConfig({
    testDir: './e2e',
    globalSetup: './e2e/global-setup.ts',
    // all tests share the one user of the `test` profile
    fullyParallel: false,
    workers: 1,
    forbidOnly: Boolean(process.env.CI),
    retries: process.env.CI ? 1 : 0,
    reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
    use: {
        baseURL: process.env.E2E_BASE_URL ?? `http://localhost:${port}`,
        testIdAttribute: 'data-testid',
        trace: 'retain-on-failure',
        screenshot: 'only-on-failure',
    },
    projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
    webServer: process.env.E2E_BASE_URL
        ? undefined
        : {
              command: 'yarn vite --port 3001 --no-open',
              url: `http://localhost:${port}`,
              reuseExistingServer: true,
              timeout: 120_000,
          },
});
