import I18n from "i18n-js";
import en from "../locale/en";
import nl from "../locale/nl";

test("Init", () => {});

export default init => {
    //we need to use them, otherwise the imports are deleted when organizing them
    expect(I18n.locale).toBe("en");
    expect(en).toBeDefined();
    expect(nl).toBeDefined();

}