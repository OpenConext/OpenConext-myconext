import I18n from "i18n-js";

const formatOptions = {weekday: "long", year: "numeric", month: "long", day: "numeric"};

export const formatCreateDate = epoch => {
    const date = new Date(0);
    date.setUTCSeconds(epoch);
    const locale = I18n.locale === "en" ? "en-US" : "nl-NL";
    const datePart = date.toLocaleDateString(locale, formatOptions);
    let minutes = date.getMinutes().toString();
    minutes = minutes.length === 2 ? minutes : "0" + minutes;
    return I18n.t("format.creationDate", {date: datePart, hours: date.getHours(), minutes: minutes});
};