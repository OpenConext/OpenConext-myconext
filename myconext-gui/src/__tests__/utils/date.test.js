import {dateFromEpoch, getAffiliationsVerificationDate} from "../../utils/date";
import I18n from "../../locale/I18n";
import {formatOptions} from "../../format/date.js";

test("dateFromEpoch", () => {
    I18n.changeLocale("en");
    expect(dateFromEpoch(693097200000)).toBe("December 18, 1991");
});

test("getAffiliationsVerificationDate", () => {
    const input = "2025-01-15T00:00:00Z";
    const locale = I18n.currentLocale() === "en" ? "en-US" : "nl-NL";
    const result = getAffiliationsVerificationDate(new Date(input).toLocaleDateString(locale, formatOptions));

    expect(result.getFullYear()).toBe(2025);
    expect(result.getMonth()).toBe(6); // July is 6
    expect(result.getDate()).toBe(15);
});