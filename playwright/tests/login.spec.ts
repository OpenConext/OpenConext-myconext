import { test, expect } from '@playwright/test';
import { loginNL } from './login';

test('Inloggen op test.eduid.nl in het Nederlands', async ({ page }) => {
  await login(page);
  await expect(page.getByText('Manage your personal info,')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Personal info' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Data & activity' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Security' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Account' })).toBeVisible();
});