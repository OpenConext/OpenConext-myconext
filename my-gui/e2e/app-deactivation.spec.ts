import { expectSmsCode } from '@myconext/shared-gui/e2e';

import { enrollApp } from './support/app';
import { expect, test } from './support/fixtures';
import { SERVER_URL } from './support/server';

test('deactivate the eduID app with an sms code', async ({
    page,
    request,
    user,
}) => {
    expect(user.registration?.notificationType).toBeUndefined();
    await enrollApp(page, request, 'phone');

    await page.getByTestId('security-app-toggle').click();
    await page.getByTestId('security-app-deactivate').click();

    const smsCode = await expectSmsCode(request, SERVER_URL);
    await page.getByTestId('deactivate-app-next').click();
    await page.getByTestId('deactivate-app-code').fill(await smsCode());
    // the code is submitted when complete

    await expect(page.getByTestId('security-app')).toBeHidden();
    await expect(page.getByTestId('security-activate-app')).toBeVisible();
});
