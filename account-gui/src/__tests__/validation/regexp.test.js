import {validEmail} from "../../validation/regexp";

test("Valid email", () => {
    expect(validEmail("nope")).toBe(false);
    expect(validEmail("nope@a")).toBe(false);
    expect(validEmail("nope@a#$%")).toBe(false);

    expect(validEmail("nope@a.com")).toBe(true);
    expect(validEmail("nope@aa.com")).toBe(true);
});