import { expect, test } from '@playwright/test';

import { getTranslation } from './utils/translations';

test('opens the homepage', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByTestId('login-container')).toBeVisible();
    await expect(page.getByTestId('login-container-title')).toHaveText(
        getTranslation('landing.title'),
    );
    await expect(page.getByTestId('login-container-description')).toHaveText(
        getTranslation('landing.description'),
    );

    await expect(page.getByTestId('landing-more-link')).toHaveAttribute(
        'href',
        'https://eduid.nl/home',
    );
});
