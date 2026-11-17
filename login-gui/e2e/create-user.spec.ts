import { expect, test } from '@playwright/test';

import { createAuthenticationRequestId } from './utils/authnRequest';

test('opens the create account page', async ({ page, request }) => {
    const id = await createAuthenticationRequestId(request);
    await page.goto(`/request/${id}`);
    await expect(page).toHaveURL(new RegExp(`/request/${id}`));

    // asert fields are visible (add data-testid to the fields)
    // Create a new user with the fields

    // API only: Find an endpoint in backend that can verify the existance of the user

    // API only: Find and endpoint in backend that can remove the user
    // API only: Verify again
});
