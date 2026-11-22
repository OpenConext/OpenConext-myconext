import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN, PROFILE_MFA_ACR } from './constants';
import { createAuthenticationRequest } from './utils/authnRequest';
import { readSamlResponse } from './utils/samlResponse';

const SAML_NO_AUTHN_CONTEXT =
    'urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext';

test.describe('acr-mfa-app-required', () => {
    test.describe('user without the eduID app requests the MFA profile', () => {
        ['download', 'refuse'].forEach((choice) => {
            const email = `e2e-acr-mfa-no-app-${choice}@example.com`;

            test(`${choice}`, async ({ page, request }) => {
                const id =
                    await test.step('create authentication request', async () => {
                        const authenticationRequest =
                            await createAuthenticationRequest(request, {
                                acrValues: [PROFILE_MFA_ACR],
                            });
                        await page.goto(authenticationRequest.path);
                        await expect(
                            page.getByTestId('login-email'),
                        ).toBeVisible();
                        return authenticationRequest.id;
                    });

                await test.step('fill in the email address', async () => {
                    await page.getByTestId('login-email').fill(email);
                });

                const codeRequestedAt = await test.step(
                    'note the time before requesting the code',
                    () => Date.now(),
                );
                await test.step('submit the form', async () => {
                    await page.getByTestId('login-submit').click();
                    await expect(page).toHaveURL(new RegExp(`/code/${id}`));
                });

                const code =
                    await test.step('read one-time code from mailpit', () =>
                        waitForCode(request, email, {
                            since: codeRequestedAt,
                        }));

                await test.step('enter the code and is told the app is required', async () => {
                    await page.getByTestId('code-input').fill(code);
                    await expect(page).toHaveURL(/\/app-required\?h=/);
                    await expect(
                        page.getByTestId('app-required-refuse'),
                    ).toBeVisible();
                    await expect(
                        page.getByTestId('app-required-download'),
                    ).toBeVisible();
                });

                if (choice === 'download') {
                    await test.step('choose to download the app', async () => {
                        await page.getByTestId('app-required-download').click();
                        await expect(page).toHaveURL(/\/getapp\?h=/);
                    });
                } else {
                    await test.step('refuse the app and confirm the refusal', async () => {
                        await page.getByTestId('app-required-refuse').click();
                        await page
                            .getByTestId('app-required-confirm-refuse')
                            .click();
                        await expect(page).toHaveURL(/\/remember\?h=/);
                    });

                    await test.step('decline "stay signed in" and receive a SAML response without authentication context', async () => {
                        // The response is posted to the ACS URL of the AuthnRequest, an external host the test must not call
                        await page.route(ACS_URL_PATTERN, (route) =>
                            route.fulfill({ status: 200, body: 'ACS stub' }),
                        );
                        const acsRequest = page.waitForRequest(ACS_URL_PATTERN);
                        await page.getByTestId('remember-no').click();

                        const response = readSamlResponse(await acsRequest);
                        expect(response.statusCodes).toContain(
                            SAML_NO_AUTHN_CONTEXT,
                        );
                    });
                }
            });
        });
    });
});
