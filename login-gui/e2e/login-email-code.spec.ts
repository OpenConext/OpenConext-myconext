import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { createAuthenticationRequestId } from './utils/authnRequest';

const EMAIL = 'e2e-email-code@example.com';
const ACS_URL_PATTERN =
    'https://engine.test.surfconext.nl/authentication/sp/consume-assertion';

test.describe('login-email-code', () => {
    test('logs in an existing user with the one-time code', async ({
        page,
        request,
    }) => {
        const id =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
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
            await expect(page).toHaveURL(new RegExp(`/code/${id}`));
            await expect(
                page.getByTestId('login-container-title'),
            ).toBeVisible();
            await expect(
                page.getByTestId('login-container-description'),
            ).toContainText(EMAIL);
        });

        const code = await test.step('read one-time code from mailpit', () =>
            waitForCode(request, EMAIL));

        await test.step('enter the code', async () => {
            await page.getByTestId('code-input').fill(code);
            await expect(page).toHaveURL(/\/remember\?h=/);
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            await expect(page.getByTestId('remember-yes')).toBeVisible();
            await expect(page.getByTestId('remember-no')).toBeVisible();

            // The server answers with an auto-submitted SAML POST form to the ACS URL of the
            // AuthnRequest, which is an external host that the test must not call
            await page.route(ACS_URL_PATTERN, (route) =>
                route.fulfill({ status: 200, body: 'ACS stub' }),
            );
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('remember-no').click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');

            // The stub answers the navigation, so the browser ends up on the ACS URL
            await expect(page).toHaveURL(ACS_URL_PATTERN);
            await expect(page.getByText('ACS stub')).toBeVisible();
        });
    });
});
