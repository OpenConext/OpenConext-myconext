import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN } from './constants';
import {
    addBrowserSessionCookie,
    createAuthenticationRequestId,
} from './utils/authnRequest';

const EMAIL = 'e2e-password@example.com';
const PASSWORD = 'Zebra-Quartz-Violin-95';

test.describe('login-password', () => {
    test('logs in an existing user with the password', async ({
        page,
        request,
    }) => {
        const id =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await addBrowserSessionCookie(page.context());
                await page.goto(`/login/${requestId}`);
                await expect(page.getByTestId('login-container')).toBeVisible();
                await expect(page.getByTestId('login-email')).toBeVisible();
                return requestId;
            });

        await test.step('fill in the email address', async () => {
            await page.getByTestId('login-email').fill(EMAIL);
        });

        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page).toHaveURL(new RegExp(`/usepassword/${id}`));
            await expect(page.getByTestId('password-input')).toBeVisible();
        });

        await test.step('enter the password', async () => {
            await page.getByTestId('password-input').fill(PASSWORD);
            await page.getByTestId('password-submit').click();
            await expect(page).toHaveURL(/\/remember\?h=/);
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            await expect(page.getByTestId('remember-yes')).toBeVisible();
            await expect(page.getByTestId('remember-no')).toBeVisible();

            await page.route(ACS_URL_PATTERN, (route) =>
                route.fulfill({ status: 200, body: 'ACS stub' }),
            );
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('remember-no').click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');

            await expect(page).toHaveURL(ACS_URL_PATTERN);
            await expect(page.getByText('ACS stub')).toBeVisible();
        });
    });
});
