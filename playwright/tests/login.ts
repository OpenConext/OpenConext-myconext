import { test, expect } from '@playwright/test';

export const login = async (page: Page) => {
  await page.goto('http://localhost:3001/');
  await page.getByRole('link', { name: 'EN' }).click();
  await page.getByRole('button', { name: 'My eduID' }).click();
  await page.getByRole('link', { name: 'EN' }).click();
  await page.getByRole('textbox', { name: 'e.g. user@gmail.com' }).click();
  await page.getByRole('textbox', { name: 'e.g. user@gmail.com' }).fill('1234567890@surfguest.nl');
  await page.getByRole('link', { name: 'Next' }).click();
  await page.getByRole('textbox', { name: 'Password' }).click();
  await page.getByRole('textbox', { name: 'Password' }).fill('secret');
  await page.getByRole('link', { name: 'Login', exact: true }).click();
  await page.waitForURL('http://localhost:3001/');
  await expect(page.getByRole('heading', { name: 'Hi Johnny!' })).toBeVisible();
}
