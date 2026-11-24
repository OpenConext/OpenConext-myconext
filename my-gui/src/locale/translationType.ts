import type {
    AppTranslation as SharedAppTranslation,
    AppTranslationOverride as SharedAppTranslationOverride,
} from '@myconext/shared-gui';

type Language = 'en' | 'nl' | 'pt';

export type AppTranslation = SharedAppTranslation<Language>;
export type AppTranslationOverride = SharedAppTranslationOverride<Language>;
