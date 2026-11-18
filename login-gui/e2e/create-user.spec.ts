import { randomUUID } from 'node:crypto';

import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { createAuthenticationRequestId } from './utils/authnRequest';
import { deleteUserByEmail } from './utils/e2eServer';
import { findEduIdByEmail } from './utils/remoteCreation';

const ACS_URL_PATTERN =
    'https://engine.test.surfconext.nl/authentication/sp/consume-assertion';

test.describe('create user', () => {
    let familyName: string;
    let email: string;

    test.beforeEach(() => {
        familyName = `e2e-user-${randomUUID()}`;
        email = `${familyName}@example.com`;
    });

    test.afterEach(async ({ request }) => {
        // Returns false when the test failed before the user was persisted, which is fine
        await deleteUserByEmail(request, email);
        expect(await findEduIdByEmail(request, email)).toBeNull();
    });

    test('registers a new user and completes the code verification', async ({
        page,
        request,
    }) => {
        const id =
            await test.step('create authentication request', async () => {
                const requestId = await createAuthenticationRequestId(request);
                await page.goto(`/request/${requestId}`);
                await expect(page.locator('#email')).toBeVisible();
                return requestId;
            });

        await test.step('fill in the new user form', async () => {
            await page.locator('#email').fill(email);
            await page.locator('#givenName').fill('E2E');
            await page.locator('#familyName').fill(familyName);
            await page.getByRole('checkbox').check();
        });

        await test.step('submit the form', async () => {
            await page.getByTestId('request-submit').click();
            await expect(page).toHaveURL(new RegExp(`/code/${id}`));
        });

        const code = await test.step('read one-time code from mailpit', () =>
            waitForCode(request, email));

        await test.step('enter the code', async () => {
            // OtpInput submits automatically once all digits are entered
            await page.getByRole('textbox').fill(code);
            await expect(page).toHaveURL(/\/request-success\?h=/);
        });

        await test.step('request success page is shown and can be continued', async () => {
            await expect(page.getByText(/has been created/i)).toBeVisible();
            await expect(
                page.getByText('We have also sent you a confirmation email.'),
            ).toBeVisible();
            await page.getByRole('button', { name: 'Next' }).click();
        });

        await test.step('decline "stay signed in" and receive the SAML assertion', async () => {
            await expect(page).toHaveURL(/\/remember\?h=/);
            await expect(page.getByText('Stay signed in?')).toBeVisible();

            // The server answers with an auto-submitted SAML POST form to the ACS URL of the
            // AuthnRequest, which is an external host that the test must not call
            await page.route(ACS_URL_PATTERN, (route) =>
                route.fulfill({ status: 200, body: 'ACS stub' }),
            );
            const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
            await page.getByRole('button', { name: 'No, not needed' }).click();

            const assertion = await acsRequest;
            expect(assertion.method()).toBe('POST');
            expect(assertion.postData()).toContain('SAMLResponse=');

            // The stub answers the navigation, so the browser ends up on the ACS URL
            await expect(page).toHaveURL(ACS_URL_PATTERN);
            await expect(page.getByText('ACS stub')).toBeVisible();
        });

        await test.step('verify if the user exists via API', async () => {
            expect(await findEduIdByEmail(request, email)).toBeTruthy();
        });
    });
});
