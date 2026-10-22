import { expect, test } from './support/fixtures';
import { waitForCode } from './support/mailpit';

test('change the email with a code from the mail', async ({
    page,
    request,
    user,
}) => {
    const email = `e2e-${crypto.randomUUID()}@example.com`;
    await page.goto('/security');
    await expect(page.getByTestId('security-email')).toContainText(user.email);

    await page.getByTestId('security-email-change').click();
    await page.getByTestId('edit-email-input').fill(email);
    const since = Date.now();
    await page.getByTestId('edit-email-request').click();

    await page
        .getByTestId('edit-email-code')
        .fill(await waitForCode(request, email, { since }));

    await expect(page.getByTestId('security-email')).toContainText(email);
});
