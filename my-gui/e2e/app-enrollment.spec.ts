import { enrollApp } from './support/app';
import { expect, test } from './support/fixtures';

test('enroll the eduID app with the tiqr-mock and a backup code', async ({
    page,
    request,
    user,
}) => {
    expect(user.registration?.notificationType).toBeUndefined();
    await enrollApp(page, request, 'backup');
});
