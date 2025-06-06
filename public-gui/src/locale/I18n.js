import en from "./en";
import nl from "./nl";

import {I18n as I18nRemote} from "i18n-js";

import {isEmpty} from "../utils/Utils";
import Cookies from "js-cookie";

const I18n = new I18nRemote({
    en: en,
    nl: nl,
});

// DetermineLanguage based on parameter, cookie and finally navigator
const urlSearchParams = new URLSearchParams(window.location.search);
let parameterByName = urlSearchParams.get("lang");

if (isEmpty(parameterByName)) {
    parameterByName = Cookies.get("lang");
}

if (isEmpty(parameterByName)) {
    parameterByName = navigator.language.toLowerCase().substring(0, 2);
}
const path = window.location.pathname;

if (path === "/servicedesk-en") {
    parameterByName = "en";
}

if (path === "/servicedesk") {
    parameterByName = "nl";
}

if (["nl", "en"].indexOf(parameterByName) === -1) {
    parameterByName = "en";
}
I18n.locale = parameterByName;

I18n.missingTranslation.register("report-error", (i18n, scope) => {
    return `[missing "${scope}" translation]`;
});
I18n.missingBehavior = "report-error";

export default I18n;
