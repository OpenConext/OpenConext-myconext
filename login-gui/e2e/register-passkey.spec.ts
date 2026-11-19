import { expect, test } from '@playwright/test';

import {
    MYCONEXT_URL_PATTERN,
    addVirtualAuthenticator,
} from './utils/virtualAuthenticator';

// Seeded as `webAuthnIdentifier` of e2e-passkey-register
const REGISTRATION_TOKEN = 'e2e-passkey-register-token';

test.describe('register-passkey', () => {
    test('registers a passkey with the registration token', async ({
        page,
    }) => {
        await page.route(MYCONEXT_URL_PATTERN, (route) =>
            route.fulfill({ status: 200, body: 'myconext stub' }),
        );

        await test.step('attach a virtual authenticator', () =>
            addVirtualAuthenticator(page));

        await test.step('open the registration page', async () => {
            await page.goto(
                `/webauthn?token=${REGISTRATION_TOKEN}&name=e2e-passkey`,
            );
            await expect(page.getByTestId('webauthn-submit')).toBeVisible();
        });

        await test.step('create the passkey and return to the account', async () => {
            await page.getByTestId('webauthn-submit').click();
            await expect(page).toHaveURL(/localhost:3003\/security/);
        });

        await test.step('the registration token cannot be used again', async () => {
            await page.goto(
                `/webauthn?token=${REGISTRATION_TOKEN}&name=e2e-passkey`,
            );
            await page.getByTestId('webauthn-submit').click();
            await expect(page).toHaveURL(/\/404/);
        });
    });
});
