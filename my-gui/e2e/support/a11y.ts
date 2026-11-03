import AxeBuilder from '@axe-core/playwright';
import type { Page } from '@playwright/test';

import { expect } from './fixtures';

// the WCAG 2.x A and AA rules
const WCAG_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];

// Fails the test with the violated rules, their impact and the offending elements.
export const expectNoA11yViolations = async (
    page: Page,
    options: { exclude?: string[] } = {},
) => {
    // dialogs fade in, the contrast of a half transparent element is not meaningful
    // (endless animations such as a caret are skipped)
    await page.evaluate(() =>
        Promise.all(
            document
                .getAnimations()
                .filter(
                    (animation) =>
                        animation.effect?.getComputedTiming().iterations !==
                        Infinity,
                )
                .map(async (animation) => {
                    try {
                        await animation.finished;
                    } catch {
                        // cancelled, nothing left to wait for
                    }
                }),
        ),
    );
    let builder = new AxeBuilder({ page }).withTags(WCAG_TAGS);
    for (const selector of options.exclude ?? []) {
        builder = builder.exclude(selector);
    }
    const { violations } = await builder.analyze();
    expect
        .soft(
            violations.map((violation) => ({
                rule: violation.id,
                impact: violation.impact,
                help: violation.help,
                targets: violation.nodes.map((node) => node.target.join(' ')),
            })),
        )
        .toEqual([]);
};
