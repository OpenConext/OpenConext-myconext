import { waitForCode } from '@myconext/shared-gui/e2e';
import { expect, test } from '@playwright/test';

import { createAuthenticationRequestId } from './utils/authnRequest';

test('opens the create account page', async ({ page, request }) => {
    const id = await createAuthenticationRequestId(request);
    await page.goto(`/request/${id}`);
    await expect(page).toHaveURL(new RegExp(`/request/${id}`));

    // Filling in the new user form
    const username = `e2e-user-${Date.now()}`;
    const email = `${username}@example.com`;
    await page.locator('#email').fill(email);
    await page.locator('#givenName').fill('E2E');
    await page.locator('#familyName').fill(username);
    await page.getByRole('checkbox').check();


    // Submit the form
    await page.getByTestId('request-submit').click();
    await expect(page).toHaveURL(new RegExp(`/code/${id}`));

    const code = await waitForCode(request, email);
    console.log(`One-time code for ${email}: ${code}`);

    // API only: Find an endpoint in backend that can verify the existance of the user

    // API only: Find and endpoint in backend that can remove the user
    // API only: Verify again
});
