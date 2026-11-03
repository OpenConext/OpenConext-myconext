import type { Page } from '@playwright/test';

import { expectNoA11yViolations } from './support/a11y';
import { expect, test } from './support/fixtures';
import { waitForCode } from './support/mailpit';
import { expectSmsCode } from './support/sms';
import { completeEnrollment } from './support/tiqr';

// Scans the steps and modals of the flows. The flows are walked up to the point
// that the step is shown, the functional outcome is asserted in the other specs.
const scan = (page: Page, step: string) =>
    test.step(`a11y: ${step}`, () => expectNoA11yViolations(page));

// the login domain (account-gui) is not part of the stack
const stubLoginDomain = (page: Page) =>
    page.route(/localhost:3000/, (route) =>
        route.fulfill({ status: 200, body: 'login domain' }),
    );

test('password flow', async ({ page, request, user }) => {
    await stubLoginDomain(page);
    await page.goto('/security');
    await page.getByTestId('security-password-change').click();
    await scan(page, 'confirm the password change');

    const since = Date.now();
    await page.getByTestId('password-confirm').click();
    await expect(page.getByTestId('password-code')).toBeVisible();
    await scan(page, 'enter the mailed code');

    await page
        .getByTestId('password-code')
        .fill(await waitForCode(request, user.email, { since }));
    await expect(page.getByTestId('password-new')).toBeVisible();
    await scan(page, 'new password form');

    await page.getByTestId('password-delete').click();
    await expect(page.getByTestId('password-delete-confirm')).toBeVisible();
    await scan(page, 'confirm deleting the password');
});

test('passkey modals', async ({ page }) => {
    await page.goto('/security');
    await page.getByTestId('security-add-passkey').click();
    await expect(page.getByTestId('add-passkey-name')).toBeVisible();
    await scan(page, 'add a passkey');

    await page.reload();
    await page.getByTestId('security-passkey-change').first().click();
    await expect(page.getByTestId('edit-passkey-name')).toBeVisible();
    await scan(page, 'edit a passkey');

    await page.getByTestId('edit-passkey-delete').click();
    await expect(page.getByTestId('edit-passkey-delete-confirm')).toBeVisible();
    await scan(page, 'confirm deleting a passkey');
});

test('change email modal', async ({ page, request }) => {
    const email = `e2e-${crypto.randomUUID()}@example.com`;
    await page.goto('/security');
    await page.getByTestId('security-email-change').click();
    await expect(page.getByTestId('edit-email-input')).toBeVisible();
    await scan(page, 'change the email');

    await page.getByTestId('edit-email-input').fill(email);
    await page.getByTestId('edit-email-request').click();
    await expect(page.getByTestId('edit-email-code')).toBeVisible();
    await scan(page, 'enter the mailed code for the email');
    // keep the mailbox tidy for the other tests
    await waitForCode(request, email);
});

test('delete account modal', async ({ page }) => {
    await page.goto('/account');
    await page.getByTestId('user-menu').click();
    await scan(page, 'user menu');

    await page.getByTestId('user-menu-delete-account').click();
    await expect(page.getByTestId('delete-account-confirmation')).toBeVisible();
    await scan(page, 'delete the account');
});

test('eduID app activation and deactivation', async ({ page, request }) => {
    let finished = false;
    await page.route('**/register/*', async (route) => {
        finished = true;
        await route.fulfill({
            status: 302,
            headers: { location: new URL('/security', page.url()).href },
        });
    });

    await page.goto('/security');
    await page.getByTestId('security-activate-app').click();
    await scan(page, 'activate the app');

    const enrollmentResponse = page.waitForResponse(
        '**/tiqr/sp/start-enrollment',
    );
    await page.getByTestId('activate-app-next').click();
    const { enrollmentKey } = (await (await enrollmentResponse).json()) as {
        enrollmentKey: string;
    };
    await expect(page.getByTestId('activate-app-enroll-qr')).toBeVisible();
    await scan(page, 'scan the qr code');

    await completeEnrollment(request, enrollmentKey);
    await expect(page.getByTestId('recovery-backup-option')).toBeVisible();
    await scan(page, 'choose a recovery option');

    await page.getByTestId('recovery-backup-option').click();
    await expect(page.getByTestId('recovery-code')).not.toBeEmpty();
    await scan(page, 'backup code');

    await page.getByTestId('recovery-backup-next').click();
    await expect(page.getByTestId('recovery-congrats-next')).toBeVisible();
    await scan(page, 'enrollment congratulations');

    await page.getByTestId('recovery-congrats-next').click();
    await expect(page.getByTestId('security-app')).toBeVisible();
    expect(finished).toBe(true);

    await page.getByTestId('security-app-toggle').click();
    await page.getByTestId('security-app-deactivate').click();
    await expect(page.getByTestId('deactivate-app-next')).toBeVisible();
    await scan(page, 'deactivate the app');
});

test('recovery with a phone number', async ({ page, request }) => {
    await page.route('**/register/*', (route) =>
        route.fulfill({
            status: 302,
            headers: { location: new URL('/security', page.url()).href },
        }),
    );
    await page.goto('/security');
    await page.getByTestId('security-activate-app').click();
    const enrollmentResponse = page.waitForResponse(
        '**/tiqr/sp/start-enrollment',
    );
    await page.getByTestId('activate-app-next').click();
    const { enrollmentKey } = (await (await enrollmentResponse).json()) as {
        enrollmentKey: string;
    };
    await completeEnrollment(request, enrollmentKey);

    await page.getByTestId('recovery-phone-option').click();
    await expect(page.getByTestId('recovery-phone-input')).toBeVisible();
    await scan(page, 'recovery phone number');

    await page.getByTestId('recovery-phone-input').fill('0612345678');
    const smsCode = await expectSmsCode(request);
    await page.getByTestId('recovery-phone-send').click();
    await expect(page.getByTestId('recovery-sms-code')).toBeVisible();
    await scan(page, 'recovery sms code');
    await smsCode();
});

test('institution modals', async ({ page }) => {
    await page.goto('/institutions');
    const card = page.getByTestId('institution-card').first();
    await expect(card).toBeVisible();
    await card.getByTestId('institution-card-toggle').click();
    await scan(page, 'institution card expanded');

    await card.getByTestId('institution-card-delete').click();
    await expect(page.getByTestId('delete-institution-confirm')).toBeVisible();
    await scan(page, 'confirm deleting an institution');
});

test('create an eduID from an institution', async ({ page }) => {
    await page.goto('/create-from-institution');
    await expect(
        page.getByTestId('create-from-institution-start'),
    ).toBeVisible();
    await scan(page, 'create from institution');
});
