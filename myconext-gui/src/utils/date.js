import I18n from "i18n-js";

export function dateFromEpoch(epochMilli, includeTime = false) {
    const options = {month: "long", day: "numeric", year: "numeric"};
    const dateTimeFormat = new Intl.DateTimeFormat(`${I18n.locale}-${I18n.locale.toUpperCase()}`, options)
    if (!includeTime) {
        const startOfDay = new Date(epochMilli - (epochMilli % 86400000));
        return dateTimeFormat.format(startOfDay);
    }
    const date = new Date(epochMilli);
    const dateFormatted = dateTimeFormat.format(date);
    const minutes = date.getMinutes() ;
    const minutesFormatted = minutes < 10 ? `0${minutes}` : minutes;
    const timeFormatted = ` ${I18n.t("security.tiqr.dateTimeOn")} ${date.getHours()}:${minutesFormatted}`;
    return `${dateFormatted}${timeFormatted}`
}
