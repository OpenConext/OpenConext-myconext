import {validPassword, passwordTooLong} from "../../validation/regexp";

test("Valid password", () => {
    expect(validPassword("nope")).toBe(false);

    //8+ characters is valid, regardless of MFA
    expect(validPassword("eightchr")).toBe(true);
    expect(validPassword("fifteencharacte")).toBe(true);
    expect(validPassword("abcdefghijklmnop")).toBe(true);

    //No composition rules
    expect(validPassword("#@$%abcdefA1")).toBe(true);
    expect(validPassword("alllowercase")).toBe(true);

    //Unicode characters count as a single character each, not by UTF-16 code units or bytes
    expect(validPassword("😀".repeat(7))).toBe(false);
    expect(validPassword("😀".repeat(8))).toBe(true);
});

test("Password too long", () => {
    expect(passwordTooLong("a".repeat(128))).toBe(false);
    expect(passwordTooLong("a".repeat(129))).toBe(true);
    expect(passwordTooLong("😀".repeat(128))).toBe(false);
    expect(passwordTooLong("😀".repeat(129))).toBe(true);
});
