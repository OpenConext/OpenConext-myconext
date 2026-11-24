import { createI18n } from '@myconext/shared-gui';

import { enOverride, nlOverride, ptOverride } from '../appCustomizations/';
import en from './en';
import nl from './nl';
import pt from './pt';

export const { I18n, AVAILABLE_LANGUAGES, switchLocale } = createI18n({
    translations: { en, nl, pt },
    overrides: { en: enOverride, nl: nlOverride, pt: ptOverride },
});

export const tArray = <T>(
    scope: string,
    mapFn: (translation: string, index: number) => T,
    options?: Record<string, string>,
): T[] => {
    // The translation lib types `t` as string, but array-valued scopes return string[] at runtime.
    const items = I18n.t(scope) as string | string[];
    if (!Array.isArray(items)) {
        return [];
    }
    return items.map((_, i) => mapFn(I18n.t(`${scope}.${i}`, options), i));
};

export default I18n;
