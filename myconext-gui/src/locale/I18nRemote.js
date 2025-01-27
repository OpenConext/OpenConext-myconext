import {I18n as I18nRemote} from "i18n-js";

import en from "./en";
import nl from "./nl";

const I18nLocal = new I18nRemote({
    en: en,
    nl: nl,
});

I18nLocal.missingTranslation.register("report-error", (i18n, scope) => {
    return null;
});
I18nLocal.missingBehavior = "report-error";

export default I18nLocal;
