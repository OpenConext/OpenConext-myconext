import {validEmail} from "../../constants/regexp";

test("Valid email", () => {
    expect(validEmail("nope")).toBe(false);
    expect(validEmail("nope@a")).toBe(false);
    expect(validEmail("nope@a#$%")).toBe(false);

    expect(validEmail("nope+test@a.com")).toBe(true);
    expect(validEmail("nope@a.com")).toBe(true);
    expect(validEmail("nope@aa.com")).toBe(true);

    expect(validEmail("karin.fanderhijn@bibliotheek-zoetermeer.nl")).toBe(true);
});