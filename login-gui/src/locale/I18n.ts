import { I18n as I18nRemote } from 'i18n-js';
import Cookies from 'js-cookie';

import { getParameterByName } from '../utils/queryParameters.ts';
import en from './en';
import nl from './nl';

const I18n = new I18nRemote({ en, nl });

// Todo: move to utils
const getPreferredLanguage = () => {
    const preferredLanguageFromParameter = getParameterByName('lang');
    if (preferredLanguageFromParameter) {
        return preferredLanguageFromParameter;
    }

    const preferredLanguageFromCookie = Cookies.get('lang');
    if (preferredLanguageFromCookie) {
        return preferredLanguageFromCookie;
    }

    const browserPreferredLanguage = navigator.language
        .toLowerCase()
        .substring(0, 2);
    if (browserPreferredLanguage) {
        return browserPreferredLanguage;
    }

    return 'en';
};

// Todo: there need to be a singe source of truth showing all available languages
const preferredLanguage = getPreferredLanguage();
I18n.locale =
    ['nl', 'en'].indexOf(preferredLanguage) === -1 ? 'en' : preferredLanguage;

I18n.missingTranslation.register('report-error', (_, scope) => {
    return `[missing "${scope}" translation]`;
});
I18n.missingBehavior = 'report-error';

// Allows inserting always-available variables in translations
const _t = I18n.t.bind(I18n);
I18n.t = (scope, options = {}) =>
    _t(scope, { productName: _t('globalVariables.productName'), ...options });

// Todo: not needed yet
// export const tArray = (scope, mapFn) =>
//     I18n.t(scope).map((_, i) => mapFn(I18n.t(`${scope}.${i}`), i));

export default I18n;
