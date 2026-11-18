import { type Page, expect, test } from '@playwright/test';

import { type Language, getTranslation } from './utils/translations';

const selectLanguage = async (page: Page, language: Language) => {
    await page.getByTestId('dropdown-trigger').click();
    await page.getByTestId(`language-${language}`).click();
};

const expectLanguage = async (page: Page, language: Language) => {
    await expect(page.getByTestId('dropdown-trigger')).toHaveText(
        language.toUpperCase(),
    );
    await expect(page.getByTestId('login-container-title')).toHaveText(
        getTranslation('landing.title', language),
    );
    await expect(page.getByTestId('login-container-description')).toHaveText(
        getTranslation('landing.description', language),
    );
};

test('switches language between EN and NL', async ({ page }) => {
    await test.step('defaults to EN', async () => {
        await page.goto('/');
        await expectLanguage(page, 'en');
    });

    await test.step('switches to NL', async () => {
        await selectLanguage(page, 'nl');
        await expectLanguage(page, 'nl');
    });

    await test.step('switches back to EN', async () => {
        await selectLanguage(page, 'en');
        await expectLanguage(page, 'en');
    });
});
