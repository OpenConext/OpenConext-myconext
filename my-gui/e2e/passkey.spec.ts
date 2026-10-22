import { expect, test } from './support/fixtures';

// Registering a passkey and testing it happen on the login domain (account-gui),
// that is not part of this stack. Here the hand over and the management of the
// seeded passkey are tested.
test('adding a passkey continues on the login domain', async ({ page }) => {
    const handover = page.waitForRequest(/localhost:3000\/webauthn/);
    await page.route(/localhost:3000/, (route) =>
        route.fulfill({ status: 200, body: 'login domain' }),
    );

    await page.goto('/security');
    await page.getByTestId('security-add-passkey').click();
    await page.getByTestId('add-passkey-name').fill('E2E key');
    await page.getByTestId('add-passkey-start').click();

    const url = new URL((await handover).url());
    expect(url.searchParams.get('name')).toBe('E2E key');
    expect(url.searchParams.get('token')).toBeTruthy();
});

test('rename and delete a passkey', async ({ page }) => {
    await page.goto('/security');
    const passkey = page.getByTestId('security-passkey').first();
    await expect(passkey).toBeVisible();

    await page.getByTestId('security-passkey-change').first().click();
    await page.getByTestId('edit-passkey-name').fill('Renamed key');
    await page.getByTestId('edit-passkey-update').click();
    await expect(passkey).toContainText('Renamed key');

    await page.getByTestId('security-passkey-change').first().click();
    await page.getByTestId('edit-passkey-delete').click();
    await page.getByTestId('edit-passkey-delete-confirm').click();
    await expect(page.getByTestId('security-passkey')).toHaveCount(0);
});
