import type { Page } from '@playwright/test';

// The redirect target after a passkey registration (myconext_redirect_url), an app that does not run in the e2e stack
export const MYCONEXT_URL_PATTERN = 'http://localhost:3003/**';

/**
 * Attaches a software passkey to the browser. It lives as long as the page, so a credential registered
 * in a test can be used for the login later in the same test. Chromium only.
 */
export const addVirtualAuthenticator = async (page: Page): Promise<void> => {
    const cdp = await page.context().newCDPSession(page);
    await cdp.send('WebAuthn.enable');
    await cdp.send('WebAuthn.addVirtualAuthenticator', {
        options: {
            protocol: 'ctap2',
            transport: 'internal',
            hasResidentKey: true,
            hasUserVerification: true,
            isUserVerified: true,
            automaticPresenceSimulation: true,
        },
    });
};
