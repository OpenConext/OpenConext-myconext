import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { ACS_URL_PATTERN, LINKING_ACRS } from './constants';
import { createAuthenticationRequest } from './utils/authnRequest';
import { readSamlResponse } from './utils/samlResponse';
import { fetchServiceName } from './utils/serviceName';
import { getTranslation } from './utils/translations';

const SAML_SUCCESS = 'urn:oasis:names:tc:SAML:2.0:status:Success';

test.describe('acr-account-linking', () => {
    ['missing', 'satisfied'].forEach((outcome) => {
        test.describe(`user ${outcome === 'missing' ? 'needs' : 'does not need'} stepup`, () => {
            // Parameterized test for ACR's:
            // - linked-institution
            // - validate-names
            // - validate-names-external
            // - iap-medium
            // - iap-high
            LINKING_ACRS.forEach(({ name, acr, explanation }) => {
                const email = `e2e-acr-${name}-${outcome}@example.com`;

                test(`${name}`, async ({ page, request }) => {
                    const id =
                        await test.step('create authentication request', async () => {
                            const authenticationRequest =
                                await createAuthenticationRequest(request, {
                                    acrValues: [acr],
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

                    if (outcome === 'missing') {
                        await test.step('enter the code', async () => {
                            await page.getByTestId('code-input').fill(code);
                        });

                        await test.step('is sent to the stepup page that explains the requirement', async () => {
                            await expect(page).toHaveURL(
                                new RegExp(
                                    `/stepup/${id}\\?explanation=${explanation}$`,
                                ),
                            );
                            const serviceName = await fetchServiceName(
                                request,
                                id,
                            );

                            await expect(
                                page.getByTestId('login-container-title'),
                            ).toHaveText(getTranslation('stepup.title'));
                            await expect(
                                page.getByTestId('login-container-description'),
                            ).toHaveText(
                                getTranslation(
                                    `stepup.${explanation}.description`,
                                    'en',
                                    { serviceName },
                                ),
                            );
                            await expect(
                                page.getByTestId('stepup-alert'),
                            ).toHaveText(
                                getTranslation(`stepup.${explanation}.alert`),
                            );
                            await expect(
                                page.getByTestId('stepup-footer'),
                            ).toHaveText(
                                getTranslation(
                                    `stepup.${explanation}.footer`,
                                    'en',
                                    { serviceName },
                                ),
                            );
                            await expect(
                                page.getByTestId('stepup-continue'),
                            ).toBeVisible();
                        });
                    } else {
                        await test.step('enter the code', async () => {
                            await page.getByTestId('code-input').fill(code);
                            await expect(page).toHaveURL(/\/remember\?h=/);
                        });

                        await test.step('decline "stay signed in" and receive the SAML assertion with the requested ACR', async () => {
                            // The assertion is posted to the ACS URL of the AuthnRequest, an external host the test must not call
                            await page.route(ACS_URL_PATTERN, (route) =>
                                route.fulfill({
                                    status: 200,
                                    body: 'ACS stub',
                                }),
                            );
                            const acsRequest =
                                page.waitForRequest(ACS_URL_PATTERN);
                            await page.getByTestId('remember-no').click();

                            const assertion = readSamlResponse(
                                await acsRequest,
                            );
                            expect(assertion.statusCodes).toEqual([
                                SAML_SUCCESS,
                            ]);
                            expect(assertion.authnContextClassRef).toBe(acr);
                        });
                    }
                });
            });
        });
    });
});
