import en from '../../src/locale/en';
import nl from '../../src/locale/nl';

const translations = { en, nl } as const;

export type Language = keyof typeof translations;

/**
 * Resolves a dot-separated translation key (e.g. `landing.title`) from the
 * app's locale files. Does not apply app overrides or interpolation.
 */
export const getTranslation = (
    translationKey: string,
    language: Language = 'en',
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

    return current;
};
