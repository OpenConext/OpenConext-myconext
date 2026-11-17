import { expect, test } from '@playwright/test';

test('opens the homepage', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByTestId('login-container')).toBeVisible();
    await expect(page.getByTestId('login-container-title')).toBeVisible();
    await expect(page.getByTestId('login-container-description')).toBeVisible();

    await expect(page.getByTestId('landing-more-link')).toHaveAttribute(
        'href',
        'https://eduid.nl/home',
    );
});
