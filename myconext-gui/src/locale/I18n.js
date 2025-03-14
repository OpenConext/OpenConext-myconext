import en from "./js/en/strings.json";
import nl from "./js/nl/strings.json";
import {reportError} from "../api";
import I18nLocal from "./I18nRemote";


const translations = {
    en: en,
    nl: nl,
};

const format = (msg, ...args) => {
    let result = msg;
    for (let i = 0; i < args.length; i++) {
        const pos = i + 1;
        result = result.replace("%" + pos + "$s", args[i]);
    }
    return result;
};

let locale = "en"

const I18n = {
    changeLocale: lang => {
        locale = lang;
        I18nLocal.locale = lang;
    },
    currentLocale: () => locale,
    t: (key, model = {}, fallback = null) => {
        const msg = translations[locale][key]
        if (!msg) {
            if (fallback) {
                return fallback;
            }
            const oldTranslation = I18nLocal.t(key, model);
            if (oldTranslation) {
                return oldTranslation;
            }
            reportError({"Missing translation": `${key} in ${locale} translation`});
            return `[missing "${key}" translation]`;
        }
        return format(msg, ...Object.values(model));
    }
};

export default I18n;
