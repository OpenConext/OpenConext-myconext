import { test, expect } from '@playwright/test';

test('Inloggen op test.eduid.nl in het Nederlands', async ({ page }) => {
  await page.goto('http://localhost:3002/');
  await page.getByRole('link', { name: 'EN' }).click();
  await page.getByRole('button', { name: 'My eduID' }).click();
  await page.getByRole('link', { name: 'EN' }).click();
  await page.getByRole('textbox', { name: 'e.g. user@gmail.com' }).click();
  await page.getByRole('textbox', { name: 'e.g. user@gmail.com' }).fill('jdoe@example.com');
  await page.getByRole('link', { name: 'Next' }).click();
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill('secret');
  await page.getByRole('link', { name: 'Login', exact: true }).click();
  await page.waitForURL('http://localhost:3001/');
  await expect(page.getByRole('heading', { name: 'Hi Johnny!' })).toBeVisible();
  await expect(page.getByText('Manage your personal info,')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Personal info' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Data & activity' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Security' })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Account' })).toBeVisible();
});