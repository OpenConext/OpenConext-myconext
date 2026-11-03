import { createI18n } from '@myconext/shared-gui';

import { enOverride, nlOverride } from '../appCustomizations/';
import en from './en';
import nl from './nl';

export const { I18n, AVAILABLE_LANGUAGES, switchLocale } = createI18n({
    translations: { en, nl },
    overrides: { en: enOverride, nl: nlOverride },
});

export const tArray = <T>(
    scope: string,
    mapFn: (translation: string, index: number) => T,
    options?: Record<string, string>,
): T[] => {
    // The translation lib types `t` as string, but array-valued scopes return string[] at runtime.
    const items = I18n.t(scope) as unknown as readonly unknown[];
    return items.map((_, i) => mapFn(I18n.t(`${scope}.${i}`, options), i));
};

export default I18n;
