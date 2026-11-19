import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN } from './constants';
import {
    addBrowserSessionCookie,
    createAuthenticationRequestId,
} from './utils/authnRequest';

const EMAIL = 'e2e-remember-method@example.com';
const PASSWORD = 'Zebra-Quartz-Violin-95';

test.describe('login-remember-method', () => {
    test('starts the next login with the method used last', async ({
        page,
        request,
    }) => {
        await addBrowserSessionCookie(page.context());
        // The server answers with an auto-submitted SAML POST form to an external ACS host
        await page.route(ACS_URL_PATTERN, (route) =>
            route.fulfill({ status: 200, body: 'ACS stub' }),
        );

        const firstId =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                await expect(page.getByTestId('login-email')).toBeVisible();
                return requestId;
            });

        await test.step('fill in the email address', async () => {
            await page.getByTestId('login-email').fill(EMAIL);
        });

        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page).toHaveURL(new RegExp(`/usepassword/${firstId}`));
            await expect(page.getByTestId('password-input')).toBeVisible();
        });

        const sentAt =
            await test.step('switch to the one-time code', async () => {
                await page.getByTestId('password-other-method').click();
                await expect(page).toHaveURL(new RegExp(`/options/${firstId}`));
                const start = Date.now();
                await page.getByTestId('item-list-useCode').click();
                await expect(page).toHaveURL(new RegExp(`/code/${firstId}`));
                return start;
            });

        const code = await test.step('read one-time code from mailpit', () =>
            waitForCode(request, EMAIL, { since: sentAt }));

        await test.step('enter the code', async () => {
            await page.getByTestId('code-input').fill(code);
            await expect(page).toHaveURL(/\/remember\?h=/);
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('remember-no').click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');
            await expect(page).toHaveURL(ACS_URL_PATTERN);
        });

        const secondId =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                await expect(
                    page.getByTestId('item-list-known-user'),
                ).toContainText(EMAIL);
                return requestId;
            });

        await test.step('continue with the remembered account and land on the one-time code', async () => {
            await page.getByTestId('item-list-known-user').click();
            await expect(page).toHaveURL(new RegExp(`/code/${secondId}`));
        });

        await test.step('switch to the password', async () => {
            await page.getByTestId('code-other-method').click();
            await expect(page).toHaveURL(new RegExp(`/options/${secondId}`));
            await page.getByTestId('item-list-usePassword').click();
            await expect(page).toHaveURL(
                new RegExp(`/usepassword/${secondId}`),
            );
        });

        await test.step('enter the password and receive the SAML assertion', async () => {
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('password-input').fill(PASSWORD);
            await page.getByTestId('password-submit').click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');
            await expect(page).toHaveURL(ACS_URL_PATTERN);
        });

        const thirdId =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                await expect(
                    page.getByTestId('item-list-known-user'),
                ).toContainText(EMAIL);
                return requestId;
            });

        await test.step('continue with the remembered account and land on the password', async () => {
            await page.getByTestId('item-list-known-user').click();
            await expect(page).toHaveURL(new RegExp(`/usepassword/${thirdId}`));
            await expect(page.getByTestId('password-input')).toBeVisible();
        });
    });
});
