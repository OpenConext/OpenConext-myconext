import { chromium } from '@playwright/test';
import type { FullConfig } from '@playwright/test';

// The vite dev server compiles on demand, the first page load takes longer than
// the timeout of a test. Load the app once before the tests.
export default async function globalSetup(config: FullConfig) {
    const baseURL = config.projects[0].use.baseURL;
    const browser = await chromium.launch();
    const page = await browser.newPage({ baseURL });
    await page.goto('/security', { timeout: 120_000 });
    await page.waitForLoadState('networkidle', { timeout: 120_000 });
    await browser.close();
}
