import { expect, test } from '@playwright/test';

import {
    ACS_URL_PATTERN,
    LINKING_ACRS,
    PROFILE_MFA_ACR,
    withMfa,
} from './constants';
import {
    addBrowserSessionCookie,
    createAuthenticationRequest,
} from './utils/authnRequest';
import { readSamlResponse } from './utils/samlResponse';
import { fetchServiceName } from './utils/serviceName';
import { authenticateWithApp, enrollAppForUser } from './utils/tiqrApp';
import { getTranslation } from './utils/translations';

const SAML_SUCCESS = 'urn:oasis:names:tc:SAML:2.0:status:Success';

// The plain MFA profile has no linking requirement, so it can only be satisfied
const MFA_ACRS = [
    {
        name: 'profile',
        acr: PROFILE_MFA_ACR,
        explanation: undefined,
    },
    ...LINKING_ACRS.map(({ name, acr, explanation }) => ({
        name,
        acr: withMfa(acr),
        explanation,
    })),
];

test.describe('acr-mfa-app', () => {
    ['missing', 'satisfied'].forEach((outcome) => {
        test.describe(`user ${outcome === 'missing' ? 'needs' : 'does not need'} stepup`, () => {
            // Parameterized test for ACR's with the eduID app:
            // - profile/mfa
            // - linked-institution/mfa
            // - validate-names/mfa
            // - validate-names-external/mfa
            // - iap-medium/mfa
            // - iap-high/mfa
            MFA_ACRS.filter(
                ({ explanation }) => outcome === 'satisfied' || explanation,
            ).forEach(({ name, acr, explanation }) => {
                const email = `e2e-acr-mfa-${name}-${outcome}@example.com`;

                test(`${name}`, async ({ page, request }) => {
                    await test.step('enrol the eduID app for the user', () =>
                        enrollAppForUser(request, email));

                    const id =
                        await test.step('create authentication request', async () => {
                            const authenticationRequest =
                                await createAuthenticationRequest(request, {
                                    acrValues: [acr],
                                });
                            await addBrowserSessionCookie(page.context());
                            await page.goto(authenticationRequest.path);
                            await expect(
                                page.getByTestId('login-email'),
                            ).toBeVisible();
                            return authenticationRequest.id;
                        });

                    await test.step('fill in the email address', async () => {
                        await page.getByTestId('login-email').fill(email);
                    });

                    const authenticationUrl =
                        await test.step('submit the form, the app is chosen as the login method', async () => {
                            const startAuthentication = page.waitForResponse(
                                (response) =>
                                    response
                                        .url()
                                        .includes(
                                            '/tiqr/start-authentication',
                                        ) && response.ok(),
                            );
                            await page.getByTestId('login-submit').click();
                            await expect(page).toHaveURL(
                                new RegExp(`/useapp/${id}`),
                            );

                            const body = (await (
                                await startAuthentication
                            ).json()) as { url: string };
                            return body.url;
                        });

                    await test.step('scan the QR code with the app', () =>
                        authenticateWithApp(request, authenticationUrl));

                    if (outcome === 'missing') {
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
                        await test.step('decline "stay signed in" and receive the SAML assertion with the requested ACR', async () => {
                            await expect(page).toHaveURL(/\/remember\?h=/);

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
