import en from "./en";
import nl from "./nl";
import {reportError} from "../api";


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

const resolveTranslation = (dictionary, key) => {
    return key.split(".").reduce((value, part) => {
        if (!value || typeof value !== "object") {
            return undefined;
        }
        return value[part];
    }, dictionary);
};

let locale = "en"

const I18n = {
    changeLocale: lang => {
        locale = lang;
    },
    currentLocale: () => locale,
    t: (key, model = {}, fallback = null) => {
        const msg = resolveTranslation(translations[locale], key)
        if (!msg) {
            if (fallback) {
                return fallback;
            }
            reportError({"Missing translation": `${key} in ${locale} translation`});
            return `[missing "${key}" translation]`;
        }
        return format(msg, ...Object.values(model));
    }
};

export default I18n;
