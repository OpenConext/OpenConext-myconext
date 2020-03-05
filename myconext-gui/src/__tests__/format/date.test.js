import init from "../init";
import {formatCreateDate} from "../../format/date";

init();

test("Format date", () => {
    const epoch = 1583337392;
    const enS = formatCreateDate(epoch);
    expect(enS).toBe("Your eduID account was created on Wednesday, March 4, 2020 at 16:56");
});