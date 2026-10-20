import { I18n as I18nRemote } from 'i18n-js';
import Cookies from 'js-cookie';

import {
    getParameterByName,
    replaceQueryParameter,
} from '../utils/queryParameters';
import en from './en';
import nl from './nl';
import pt from './pt';

export const AVAILABLE_LANGUAGES = ['nl', 'en', 'pt'];

const I18n = new I18nRemote({ en, nl, pt });

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
    _t(scope, { productName: _t('globalVariables.productName'), ...options });

export const switchLocale = async (
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

export default I18n;
