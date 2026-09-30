import en from "./en";
import nl from "./nl";

import {I18n as I18nRemote} from "i18n-js";

import {reportError} from "../api";

const I18n = new I18nRemote({
    en: en,
    nl: nl,
});

I18n.missingTranslation.register("report-error", (i18n, scope) => {
    reportError({"Missing translation": `${scope} in ${i18n.locale} translation`});
    return `[missing "${scope}" translation]`;
});
I18n.missingBehavior = "report-error";

export default I18n;
