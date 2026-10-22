import type { Page } from '@playwright/test';

import { expect, test } from './support/fixtures';
import { waitForCode } from './support/mailpit';
import { TIQR_MOCK_URL } from './support/tiqr';

// The server runs with oidc.base-url pointing at the OIDC mock in the tiqr-mock,
// that offers a persona per scenario on its authorize page.
const loginAtInstitution = async (
    page: Page,
    persona: 'success' | 'no-attributes' | 'eppn-already-linked',
) => {
    await page.waitForURL(`${TIQR_MOCK_URL}/oidc/authorize**`);
    await page.getByTestId(`oidc-persona-${persona}`).click();
};

test('delete a linked institution', async ({ page }) => {
    await page.goto('/institutions');
    const cards = page.getByTestId('institution-card');
    await expect(cards.first()).toBeVisible();
    const before = await cards.count();

    await cards.first().getByTestId('institution-card-toggle').click();
    await cards.first().getByTestId('institution-card-delete').click();
    await page.getByTestId('delete-institution-confirm').click();

    await expect(cards).toHaveCount(before - 1);
});

test.describe('link an institution', () => {
    test('a new institution is added', async ({ page }) => {
        await page.goto('/institutions');
        const cards = page.getByTestId('institution-card');
        await expect(cards.first()).toBeVisible();
        const before = await cards.count();

        await page.getByTestId('institutions-add').click();
        await loginAtInstitution(page, 'success');

        await page.waitForURL(/localhost:3001/);
        await page.goto('/institutions');
        await expect(cards).toHaveCount(before + 1);
    });

    test('an institution without attributes is refused', async ({ page }) => {
        await page.goto('/institutions');
        await page.getByTestId('institutions-add').click();
        await loginAtInstitution(page, 'no-attributes');

        await expect(page).toHaveURL(/\/attribute-missing/);
    });

    test('an institution that is already linked is refused', async ({
        page,
    }) => {
        await page.goto('/institutions');
        await page.getByTestId('institutions-add').click();
        await loginAtInstitution(page, 'eppn-already-linked');

        await expect(page).toHaveURL(/\/eppn-already-linked/);
    });
});

test.describe('create an eduID from an institution', () => {
    test('the email is verified with a code from the mail', async ({
        page,
        request,
    }) => {
        const email = `e2e-${crypto.randomUUID()}@example.com`;

        await page.goto('/create-from-institution');
        await page.getByTestId('create-from-institution-start').click();
        await loginAtInstitution(page, 'success');

        await page.getByTestId('link-from-institution-email').fill(email);
        await page.getByTestId('link-from-institution-agree').click();
        await page.getByTestId('link-from-institution-submit').click();

        const code = await waitForCode(request, email);
        // the new account continues on the login domain (account-gui), that is
        // not part of this stack
        const loginDomain = page.waitForRequest(/localhost:3000/);
        await page.route(/localhost:3000/, (route) =>
            route.fulfill({ status: 200, body: 'login domain' }),
        );
        await page.getByTestId('verify-from-institution-code').fill(code);

        expect((await loginDomain).url()).toContain('localhost:3000');
    });

    test('an institution without attributes is refused', async ({ page }) => {
        await page.goto('/create-from-institution');
        await page.getByTestId('create-from-institution-start').click();
        await loginAtInstitution(page, 'no-attributes');

        await expect(page).toHaveURL(
            /\/create-from-institution\/attribute-missing/,
        );
    });

    test('an institution that is already linked is refused', async ({
        page,
    }) => {
        await page.goto('/create-from-institution');
        await page.getByTestId('create-from-institution-start').click();
        await loginAtInstitution(page, 'eppn-already-linked');

        await expect(page).toHaveURL(
            /\/create-from-institution\/eppn-already-linked/,
        );
    });
});
