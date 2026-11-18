import { request } from '@playwright/test';

import { SERVER_URL } from './constants';

// Runs once per `playwright test`, before the workers start, so parallel specs can't race on the reset
export default async function globalSetup(): Promise<void> {
    const context = await request.newContext();
    try {
        const response = await context.get(`${SERVER_URL}/e2e/reset`);
        if (!response.ok()) {
            throw new Error(`e2e/reset failed with ${response.status()}`);
        }
    } finally {
        await context.dispose();
    }
}
