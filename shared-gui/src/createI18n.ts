import { I18n as I18nRemote } from 'i18n-js';
import type { Dict } from 'i18n-js';
import Cookies from 'js-cookie';

import { getParameterByName, replaceQueryParameter } from './queryParameters';

export type CreateI18nOptions = {
    // the available languages are the keys of the translations
    translations: Record<string, Dict>;
    // translations merged over the base ones, e.g. for an app customization
    overrides?: Record<string, Dict | undefined>;
};

const getPreferredLanguage = () => {
    const preferredLanguageFromParameter = getParameterByName('lang');
    if (preferredLanguageFromParameter) {
        return preferredLanguageFromParameter;
    }

    const preferredLanguageFromCookie = Cookies.get('lang');
    if (preferredLanguageFromCookie) {
        return preferredLanguageFromCookie;
    }

    const browserPreferredLanguage =
        typeof navigator === 'undefined'
            ? ''
            : navigator.language.toLowerCase().substring(0, 2);
    if (browserPreferredLanguage) {
        return browserPreferredLanguage;
    }

    return 'en';
};

export const createI18n = ({ translations, overrides }: CreateI18nOptions) => {
    const AVAILABLE_LANGUAGES = Object.keys(translations);
    const I18n = new I18nRemote(translations);

    if (overrides) {
        for (const [locale, override] of Object.entries(overrides)) {
            if (override) {
                I18n.store({ [locale]: override });
            }
        }
    }

    const preferredLanguage = getPreferredLanguage();
    I18n.locale = AVAILABLE_LANGUAGES.includes(preferredLanguage)
        ? preferredLanguage
        : 'en';

    I18n.missingTranslation.register('report-error', (_, scope) => {
        return `[missing "${scope}" translation]`;
    });
    I18n.missingBehavior = 'report-error';

    // Allows inserting always-available variables in translations
    const _t = I18n.t.bind(I18n);
    I18n.t = (scope, options = {}) =>
        _t(scope, {
            productName: _t('globalVariables.productName'),
            ...options,
        });

    const switchLocale = async (
        updatedLocale: string,
        domain?: string,
        persist?: (locale: string) => Promise<unknown>,
    ) => {
        Cookies.set('lang', updatedLocale, {
            expires: 365,
            secure: document.location.protocol.endsWith('https'),
            domain,
        });
        I18n.locale = updatedLocale;
        try {
            await persist?.(updatedLocale);
        } catch {
            // the cookie and query parameter still apply the language
        }
        window.location.search = replaceQueryParameter(
            window.location.search,
            'lang',
            updatedLocale,
        );
    };

    return { I18n, AVAILABLE_LANGUAGES, switchLocale };
};
