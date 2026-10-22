import { expect, test } from './support/fixtures';

test('edit the chosen name', async ({ page }) => {
    await page.goto('/identity');
    await page.getByTestId('identity-chosen-name-edit').click();
    await page.getByTestId('edit-chosen-name-input').fill('Jonny');
    await page.getByTestId('edit-chosen-name-save').click();

    await expect(page.getByTestId('identity-chosen-name')).toContainText(
        'Jonny',
    );
});
