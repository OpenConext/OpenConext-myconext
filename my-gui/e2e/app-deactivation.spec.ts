import { enrollApp } from './support/app';
import { expect, test } from './support/fixtures';
import { expectSmsCode } from './support/sms';

test('deactivate the eduID app with an sms code', async ({
    page,
    request,
    user,
}) => {
    expect(user.registration?.notificationType).toBeUndefined();
    await enrollApp(page, request, 'phone');

    await page.getByTestId('security-app-toggle').click();
    await page.getByTestId('security-app-deactivate').click();

    const smsCode = await expectSmsCode(request);
    await page.getByTestId('deactivate-app-next').click();
    await page.getByTestId('deactivate-app-code').fill(await smsCode());
    // the code is submitted when complete

    await expect(page.getByTestId('security-app')).toBeHidden();
    await expect(page.getByTestId('security-activate-app')).toBeVisible();
});
