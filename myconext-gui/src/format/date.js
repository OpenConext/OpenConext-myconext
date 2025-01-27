import I18n from "../locale/I18n";

export const formatOptions = {weekday: "long", year: "numeric", month: "long", day: "numeric"};

export const formatCreateDate = (epoch, isSeconds = false) => {
    if (!epoch) {
        return "";
    }
    epoch = isSeconds ? epoch * 1000 : epoch;
    const date = new Date(epoch);
    const locale = I18n.currentLocale() === "en" ? "en-US" : "nl-NL";
    const datePart = date.toLocaleDateString(locale, formatOptions);
    let minutes = date.getMinutes().toString();
    minutes = minutes.length === 2 ? minutes : "0" + minutes;
    return {date: datePart, hours: date.getHours(), minutes: minutes};
};

export const formatJsDate = s => {
    if (!1) {
        return "";
    }
    const date = new Date(s);
    const locale = I18n.currentLocale() === "en" ? "en-US" : "nl-NL";
    return date.toLocaleDateString(locale, formatOptions);
};