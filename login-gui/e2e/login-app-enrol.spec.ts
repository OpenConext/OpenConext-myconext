import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN } from './constants';
import { createAuthenticationRequestId } from './utils/authnRequest';
import { resetSeededUsers } from './utils/e2eServer';
import { enrollAppDevice } from './utils/tiqrApp';

const EMAIL = 'e2e-app-nudge@example.com';

test.describe('login-app-enrol', () => {
    // The server stores the moment the nudge was shown and the enrollment is bound to the user
    test.beforeEach(async ({ request }) => {
        await resetSeededUsers(request);
    });

    test('enrols the eduID app from the nudge and continues the login', async ({
        page,
        request,
    }) => {
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

        await test.step('submit the form', async () => {
            await page.getByTestId('login-submit').click();
            await expect(page).toHaveURL(new RegExp(`/code/${id}`));
        });

        const code = await test.step('read one-time code from mailpit', () =>
            waitForCode(request, EMAIL));

        await test.step('enter the code', async () => {
            await page.getByTestId('code-input').fill(code);
            await expect(page).toHaveURL(/\/confirm\?h=/);
        });

        await test.step('choose to download the app', async () => {
            await page.getByTestId('nudge-app-download').click();
            await expect(page).toHaveURL(/\/getapp\?h=/);
        });

        const enrollmentKey =
            await test.step('continue once the app is installed', async () => {
                const startEnrollment = page.waitForResponse(
                    (response) =>
                        response.url().includes('/tiqr/start-enrollment') &&
                        response.ok(),
                );
                await page.getByTestId('getapp-next').click();
                await expect(page).toHaveURL(/\/enrollapp\?h=/);
                await expect(page.getByTestId('enrollapp-qr')).toBeVisible();

                const body = (await (await startEnrollment).json()) as {
                    enrollmentKey: string;
                };
                return body.enrollmentKey;
            });

        await test.step('scan the QR code with the app', async () => {
            await enrollAppDevice(request, enrollmentKey);
            await expect(page).toHaveURL(/\/recovery\?h=/);
        });

        await test.step('choose a backup code as recovery method', async () => {
            await page.getByTestId('item-list-backup-code').click();
            await expect(page).toHaveURL(/\/recovery-code\?h=/);
            await expect(page.getByTestId('recovery-code-value')).toBeVisible();
        });

        await test.step('continue after noting the backup code', async () => {
            await page.getByTestId('recovery-code-next').click();
            await expect(page).toHaveURL(/\/remember\?h=/);
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            // The assertion is posted to the ACS URL of the AuthnRequest, an external host the test must not call
            await page.route(ACS_URL_PATTERN, (route) =>
                route.fulfill({ status: 200, body: 'ACS stub' }),
            );
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByTestId('remember-no').click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');
            await expect(page.getByText('ACS stub')).toBeVisible();
        });
    });
});
