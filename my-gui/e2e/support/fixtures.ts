import { test as base, expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from './server';
import { deleteRegistration } from './tiqr';

type Me = {
    id: string;
    email: string;
    registration?: { notificationType?: string };
};

type Fixtures = {
    // the user of the `test` profile, as seeded and without an app registration
    user: Me;
};

const fetchMe = async (request: APIRequestContext) => {
    const response = await request.get('/myconext/api/sp/me');
    // the user is gone after a test deleted the account
    return response.ok() ? ((await response.json()) as Me) : undefined;
};

export const test = base.extend<Fixtures>({
    user: [
        async ({ request }, provide) => {
            // the tests change and delete the user, so restore the seed first
            const before = await fetchMe(request);
            if (before?.registration?.notificationType) {
                await deleteRegistration(request, before.id);
            }
            const reset = await request.get(`${SERVER_URL}/e2e/reset`);
            expect(reset.ok()).toBeTruthy();
            const me = await fetchMe(request);
            expect(me).toBeDefined();
            await provide(me!);
        },
        { auto: true },
    ],
});

export { expect };
