import I18n from "../locale/I18n";

export function dateFromEpoch(epochMilli, includeTime = false) {
    const options = {month: "long", day: "numeric", year: "numeric"};
    const dateTimeFormat = new Intl.DateTimeFormat(`${I18n.currentLocale()}-${I18n.currentLocale().toUpperCase()}`, options)
    if (!includeTime) {
        const startOfDay = new Date(epochMilli - (epochMilli % 86400000));
        return dateTimeFormat.format(startOfDay);
    }
    const date = new Date(epochMilli);
    const dateFormatted = dateTimeFormat.format(date);
    const minutes = date.getMinutes();
    const minutesFormatted = minutes < 10 ? `0${minutes}` : minutes;
    const timeFormatted = ` ${I18n.t("security.tiqr.dateTimeOn")} ${date.getHours()}:${minutesFormatted}`;
    return `${dateFormatted}${timeFormatted}`
}

export function verificationCodeValidityDays(controlCode) {
    const millis = new Date().getTime() - controlCode.createdAt || new Date().getTime();
    return Math.floor(millis / (1000 * 60 * 60 * 24)) + 14;
}

export function months(locale) {
    return [
        {value: "JAN", label_en: "January", label_nl: "januari", days: 31},
        {value: "FEB", label_en: "February", label_nl: "februari", days: 28},
        {value: "MAR", label_en: "March", label_nl: "maart", days: 31},
        {value: "APR", label_en: "April", label_nl: "april", days: 30},
        {value: "MAY", label_en: "May", label_nl: "mei", days: 31},
        {value: "JUN", label_en: "June", label_nl: "juni", days: 30},
        {value: "JUL", label_en: "July", label_nl: "juli", days: 31},
        {value: "AUG", label_en: "August", label_nl: "augustus", days: 31},
        {value: "SEP", label_en: "September", label_nl: "september", days: 30},
        {value: "OCT", label_en: "October", label_nl: "oktober", days: 31},
        {value: "NOV", label_en: "November", label_nl: "november", days: 30},
        {value: "DEC", label_en: "December", label_nl: "december", days: 31}
    ].map(item => ({...item, label: item[`label_${locale}`]}));
}

export const range = (start, end, includeEnd = false, strings= false) =>
    Array.from({length: end - start + (includeEnd ? 1 : 0)}, (_, i) => strings ? (i + start).toString() : i + start);

export function getAffiliationsVerificationDate(createAtDate) {
    const verificationDate = new Date(createAtDate);
    verificationDate.setMonth(verificationDate.getMonth() + 6)
    return verificationDate
}
