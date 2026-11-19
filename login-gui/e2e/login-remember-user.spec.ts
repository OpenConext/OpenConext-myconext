import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN } from './constants';
import {
    addBrowserSessionCookie,
    createAuthenticationRequestId,
} from './utils/authnRequest';

const EMAIL = 'e2e-remember-login@example.com';
const PASSWORD = 'Zebra-Quartz-Violin-95';

test.describe('login-remember-user', () => {
    test('offers the account of a successful login on the next visit', async ({
        page,
        request,
    }) => {
        await test.step('create authentication request', async () => {
            const requestId = await createAuthenticationRequestId(request);
            await addBrowserSessionCookie(page.context());
            await page.goto(`/login/${requestId}`);
            await expect(page.getByTestId('login-email')).toBeVisible();
        });

        await test.step('fill in the email address', async () => {
            await page.getByTestId('login-email').fill(EMAIL);
        });

        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page.getByTestId('password-input')).toBeVisible();
        });

        await test.step('enter the password', async () => {
            await page.getByTestId('password-input').fill(PASSWORD);
            await page.getByTestId('password-submit').click();
            await expect(page).toHaveURL(/\/remember\?h=/);
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            await page.route(ACS_URL_PATTERN, (route) =>
                route.fulfill({ status: 200, body: 'ACS stub' }),
            );
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('remember-no').click();
            await acsRequest;
            await expect(page).toHaveURL(ACS_URL_PATTERN);
        });

        const id =
            await test.step('return to login with a new authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                return requestId;
            });

        await test.step('see the remembered account instead of the email form', async () => {
            await expect(page.getByTestId('item-list-known-user')).toContainText(
                EMAIL,
            );
            await expect(page.getByTestId('login-email')).toHaveCount(0);
        });

        await test.step('continue with the remembered account', async () => {
            await page.getByTestId('item-list-known-user').click();
            await expect(page).toHaveURL(new RegExp(`/usepassword/${id}`));
            await expect(page.getByTestId('password-input')).toBeVisible();
        });
    });
});
