type AppLanguage = {
    language: string;
    en: string;
    nl: string;
    [key: string]: string;
};

type GlobalVariables = {
    productName: string;
}

export type AppTranslation = {
    globalVariables: GlobalVariables,
    code: string;
    languages: AppLanguage;
    [key: string]: string | Record<string, unknown>;
};

export type AppTranslationOverride = {
    globalVariables: Partial<GlobalVariables>;
    code: string;
    languages: Partial<AppLanguage>;
    [key: string]: string | Record<string, unknown>;
}
