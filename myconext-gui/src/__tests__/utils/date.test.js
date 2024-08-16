import {dateFromEpoch} from "../../utils/date";
import I18n from "i18n-js";

test("dateFromEpoch", () => {
    I18n.locale = "en";
    expect(dateFromEpoch(693097200000)).toBe("December 19, 1991");
});