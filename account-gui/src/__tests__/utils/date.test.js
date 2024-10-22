import {dateFromEpoch} from "../../utils/date";

test("Date from epoch email", () => {
    expect(dateFromEpoch(1727876292383, false)).toBe("October 2, 2024");
});