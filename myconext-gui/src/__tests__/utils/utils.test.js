import {splitListSemantically} from "../../utils/utils";

test("Split list semantically", () => {
    expect(splitListSemantically([], "and")).toBe("");
    expect(splitListSemantically([1,2,3], "and")).toBe("1, 2 and 3");
});