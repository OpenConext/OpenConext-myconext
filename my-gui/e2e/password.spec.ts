import type { Page } from '@playwright/test';

import { expect, test } from './support/fixtures';
import { waitForCode } from './support/mailpit';

// Saving a password continues on the login domain (account-gui), that sets the
// login preference and redirects back. That domain is not part of this stack.
const stubLoginDomain = async (page: Page) => {
    const requests: string[] = [];
    await page.route(/localhost:3000/, (route) => {
        requests.push(route.request().url());
        return route.fulfill({ status: 200, body: 'login domain' });
    });
    return requests;
};

// confirm, then enter the code that is mailed to the user
const confirmWithCode = async (
    page: Page,
    request: Parameters<typeof waitForCode>[0],
    email: string,
) => {
    const since = Date.now();
    await page.getByTestId('password-confirm').click();
    await page
        .getByTestId('password-code')
        .fill(await waitForCode(request, email, { since }));
};

test('change the password', async ({ page, request, user }) => {
    const requests = await stubLoginDomain(page);
    await page.goto('/security');
    await page.getByTestId('security-password-change').click();
    await confirmWithCode(page, request, user.email);

    await page.getByTestId('password-new').fill('Zebra-Quartz-Violin-93');
    await page
        .getByTestId('password-confirm-new')
        .fill('Zebra-Quartz-Violin-93');
    await page.getByTestId('password-save').click();

    await expect.poll(() => requests.join()).toContain('login-preference');
});

test('remove the password and add it again', async ({
    page,
    request,
    user,
}) => {
    const requests = await stubLoginDomain(page);
    await page.goto('/security');
    await page.getByTestId('security-password-change').click();
    await confirmWithCode(page, request, user.email);
    await page.getByTestId('password-delete').click();
    await page.getByTestId('password-delete-confirm').click();

    await expect(page.getByTestId('security-password')).toBeHidden();
    await expect(page.getByTestId('security-add-password')).toBeVisible();

    await page.getByTestId('security-add-password').click();
    await confirmWithCode(page, request, user.email);
    await page.getByTestId('password-new').fill('Zebra-Quartz-Violin-94');
    await page
        .getByTestId('password-confirm-new')
        .fill('Zebra-Quartz-Violin-94');
    await page.getByTestId('password-save').click();

    await expect.poll(() => requests.join()).toContain('login-preference');
});
