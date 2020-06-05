import init from "../init";
import {formatCreateDate} from "../../format/date";

init();

test("Format date", () => {
    const epoch = 1583337392000;
    const withoutSeconds = formatCreateDate(epoch);
    expect(withoutSeconds.date).toEqual("Wednesday, March 4, 2020");

    const withSeconds = formatCreateDate(epoch / 1000, true);
    expect(withSeconds.date).toEqual("Wednesday, March 4, 2020");
});