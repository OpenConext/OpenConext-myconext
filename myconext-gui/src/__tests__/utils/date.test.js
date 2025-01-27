import {dateFromEpoch} from "../../utils/date";
import I18n from "../../locale/I18n";

test("dateFromEpoch", () => {
    I18n.changeLocale("en");
    expect(dateFromEpoch(693097200000)).toBe("December 18, 1991");
});