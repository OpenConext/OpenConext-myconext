import { test, expect } from '@playwright/test';

test('check generale page', async ({ page }) => {
  await page.goto('https://test.eduid.nl');
  await page.getByRole('link', {name: 'EN'}).click();
  await expect(page.getByRole('heading', { name: 'eduID', exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'My eduID' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create an eduID' }).first()).toBeVisible();
 
});