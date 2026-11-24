import en from '../../src/locale/en';
import nl from '../../src/locale/nl';
import pt from '../../src/locale/pt';

const translations = { en, nl, pt } as const;

export type Language = keyof typeof translations;

/**
 * Resolves a dot-separated translation key (e.g. `landing.title`) from the
 * app's locale files. `{{productName}}` and the given `params` (e.g. `serviceName`) are interpolated
 */
export const getTranslation = (
    translationKey: string,
    language: Language = 'en',
    params: Record<string, string> = {},
): string => {
    let current: unknown = translations[language];

    for (const part of translationKey.split('.')) {
        if (
            typeof current !== 'object' ||
            current === null ||
            !Object.hasOwn(current, part)
        ) {
            throw new Error(
                `Translation key "${translationKey}" not found for language "${language}"`,
            );
        }
        current = (current as Record<string, unknown>)[part];
    }

    if (typeof current !== 'string') {
        throw new Error(
            `Translation key "${translationKey}" does not resolve to a string for language "${language}"`,
        );
    }

    // Mirrors createI18n, which makes productName available in every translation
    const { productName } = translations[language].globalVariables;
    return Object.entries({ productName, ...params }).reduce(
        (text, [name, value]) => text.replaceAll(`{{${name}}}`, value),
        current,
    );
};
