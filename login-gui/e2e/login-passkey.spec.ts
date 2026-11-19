import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN } from './constants';
import {
    addBrowserSessionCookie,
    createAuthenticationRequestId,
} from './utils/authnRequest';
import {
    MYCONEXT_URL_PATTERN,
    addVirtualAuthenticator,
} from './utils/virtualAuthenticator';

const EMAIL = 'e2e-passkey-login@example.com';
const REGISTRATION_TOKEN = 'e2e-passkey-login-token';

test.describe('login-passkey', () => {
    test('logs in with a passkey', async ({ page, request }) => {
        await addBrowserSessionCookie(page.context());
        await page.route(ACS_URL_PATTERN, (route) =>
            route.fulfill({ status: 200, body: 'ACS stub' }),
        );
        await page.route(MYCONEXT_URL_PATTERN, (route) =>
            route.fulfill({ status: 200, body: 'myconext stub' }),
        );

        await test.step('attach a virtual authenticator', () =>
            addVirtualAuthenticator(page));

        // The server has no other way to get a credential into the virtual authenticator
        await test.step('register a passkey', async () => {
            await page.goto(
                `/webauthn?token=${REGISTRATION_TOKEN}&name=e2e-passkey`,
            );
            await page.getByTestId('webauthn-submit').click();
            await expect(page).toHaveURL(/localhost:3003\/security/);
        });

        const id =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/login/${requestId}`);
                await expect(page.getByTestId('login-email')).toBeVisible();
                return requestId;
            });

        await test.step('fill in the email address', async () => {
            await page.getByTestId('login-email').fill(EMAIL);
        });

        // Registering stores the passkey as the preferred method
        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page).toHaveURL(new RegExp(`/usewebauthn/${id}`));
            await expect(page.getByTestId('usewebauthn-submit')).toBeVisible();
        });

        await test.step('log in with the passkey', async () => {
            await page.getByTestId('usewebauthn-submit').click();
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
    });
});
