import { expect } from '@playwright/test';
import type { APIRequestContext, Page } from '@playwright/test';

import { expectSmsCode } from './sms';
import { completeEnrollment } from './tiqr';

type Recovery = 'backup' | 'phone';

// Finishing the enrollment hands over to the login domain (account-gui), that
// sets the cookie and redirects back to the security page. That domain is not
// part of this stack, so answer with the redirect it would give.
const stubLoginDomain = async (page: Page) => {
    let finishKey = '';
    await page.route('**/register/*', async (route) => {
        finishKey = new URL(route.request().url()).pathname.split('/').pop()!;
        await route.fulfill({
            status: 302,
            headers: { location: new URL('/security', page.url()).href },
        });
    });
    return () => finishKey;
};

// Activates the eduID app for the user, the tiqr-mock is the app
export const enrollApp = async (
    page: Page,
    request: APIRequestContext,
    recovery: Recovery,
) => {
    const finishKey = await stubLoginDomain(page);

    await page.goto('/security');
    await page.getByTestId('security-activate-app').click();

    const enrollmentResponse = page.waitForResponse(
        '**/tiqr/sp/start-enrollment',
    );
    await page.getByTestId('activate-app-next').click();
    const { enrollmentKey } = (await (await enrollmentResponse).json()) as {
        enrollmentKey: string;
    };
    await expect(page.getByTestId('activate-app-enroll-qr')).toBeVisible();

    // the tiqr-mock acts as the app that scanned the qr code
    await completeEnrollment(request, enrollmentKey);

    if (recovery === 'backup') {
        await page.getByTestId('recovery-backup-option').click();
        await expect(page.getByTestId('recovery-code')).not.toBeEmpty();
        await page.getByTestId('recovery-backup-next').click();
    } else {
        await page.getByTestId('recovery-phone-option').click();
        await page.getByTestId('recovery-phone-input').fill('0612345678');
        const smsCode = await expectSmsCode(request);
        await page.getByTestId('recovery-phone-send').click();
        await page.getByTestId('recovery-sms-code').fill(await smsCode());
        // the code is submitted when complete
    }
    await page.getByTestId('recovery-congrats-next').click();

    await expect(page.getByTestId('security-app')).toBeVisible();
    expect(finishKey()).not.toBe('');
};
