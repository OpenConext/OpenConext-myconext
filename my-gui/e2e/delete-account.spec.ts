import { expect, test } from './support/fixtures';

test('delete the account', async ({ page, request }) => {
    // the server clears the session and the login domain (account-gui), that is
    // not part of this stack, shows the goodbye
    const logout = page.waitForRequest(/localhost:3000\/doLogout/);
    await page.route(/localhost:3000/, (route) =>
        route.fulfill({ status: 200, body: 'login domain' }),
    );

    await page.goto('/account');
    await page.getByTestId('user-menu').click();
    await page.getByTestId('user-menu-delete-account').click();
    await page.getByTestId('delete-account-confirmation').fill('delete');
    await page.getByTestId('delete-account-confirm').click();

    expect((await logout).url()).toContain('delete%3Dtrue');
    const me = await request.get('/myconext/api/sp/me');
    expect(me.ok()).toBeFalsy();
});
