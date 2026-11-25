// `L` is the union of language codes an app supports, so each app decides which languages it must translate
type AppLanguage<L extends string> = { language: string } & Record<L, string> &
    Record<string, string>;

export type DefaultLanguage = 'en' | 'nl' | 'pt';

type GlobalVariables = {
    productName: string;
};

export type AppTranslation<L extends string = DefaultLanguage> = {
    globalVariables: GlobalVariables;
    code: string;
    languages: AppLanguage<L>;
    [key: string]: string | Record<string, unknown>;
};

export type AppTranslationOverride<L extends string = DefaultLanguage> = {
    globalVariables: Partial<GlobalVariables>;
    code: string;
    languages: Partial<AppLanguage<L>>;
    [key: string]: string | Record<string, unknown>;
};
