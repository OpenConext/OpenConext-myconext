import { expectNoA11yViolations } from './support/a11y';
import { test } from './support/fixtures';

const pages = [
    { name: 'home', path: '/' },
    { name: 'account', path: '/account' },
    { name: 'security', path: '/security' },
    { name: 'institutions', path: '/institutions' },
    { name: 'identity', path: '/identity' },
    { name: 'activity', path: '/activity' },
];

for (const { name, path } of pages) {
    test(`the ${name} page has no accessibility violations`, async ({
        page,
    }) => {
        await page.goto(path);
        // wait for the data to be rendered, the skeletons are not representative
        await page.waitForLoadState('networkidle');
        await expectNoA11yViolations(page);
    });
}
