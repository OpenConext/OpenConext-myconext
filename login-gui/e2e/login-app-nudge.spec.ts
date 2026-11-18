import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { createAuthenticationRequestId } from './utils/authnRequest';
import { resetSeededUsers } from './utils/e2eServer';

const EMAIL = 'e2e-app-nudge@example.com';

test.describe('login-app-nudge', () => {
    // The server stores the moment the nudge was shown, therefore a second run of the same test would not be nudged
    test.beforeEach(async ({ request }) => {
        await resetSeededUsers(request);
    });

    test('shows the app nudge after the one-time code and continues when skipped', async ({
        page,
        request,
    }) => {
        const id =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                await expect(page.getByTestId('login-container')).toBeVisible();
                await expect(page.locator('#email')).toBeVisible();
                return requestId;
            });

        await test.step('fill in the email address', async () => {
            await page.locator('#email').fill(EMAIL);
        });

        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page).toHaveURL(new RegExp(`/code/${id}`));
        });

        const code = await test.step('read one-time code from mailpit', () =>
            waitForCode(request, EMAIL));

        await test.step('enter the code', async () => {
            await page.getByRole('textbox').fill(code);
            await expect(page).toHaveURL(/\/confirm\?h=/);
        });

        await test.step('app nudge is shown', async () => {
            await expect(
                page.getByTestId('login-container-title'),
            ).toBeVisible();
            await expect(page.getByTestId('nudge-app-download')).toBeVisible();
            await expect(page.getByTestId('nudge-app-skip')).toBeVisible();
        });

        await test.step('skip the nudge and continue the login', async () => {
            await page.getByTestId('nudge-app-skip').click();
            await expect(page).toHaveURL(/\/remember\?h=/);
            await expect(page.getByTestId('remember-yes')).toBeVisible();
            await expect(page.getByTestId('remember-no')).toBeVisible();
        });
    });
});
