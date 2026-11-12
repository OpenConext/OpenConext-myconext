import { expect, test } from 'vitest';

import en from '../../locale/en';
import nl from '../../locale/nl';

const flattenKeys = (value: unknown, parent = ''): string[] => {
    if (typeof value !== 'object' || value === null) {
        return [];
    }
    return Object.entries(value).flatMap(([key, child]) => [
        parent + key,
        ...flattenKeys(child, `${parent}${key}.`),
    ]);
};

test('All translations exists in all bundles', () => {
    expect(flattenKeys(nl)).toEqual(flattenKeys(en));
});
