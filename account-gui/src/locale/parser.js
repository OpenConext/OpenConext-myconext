import en from "./en";

const parse = (dict, key, index, results = {}, stack = []) => {
    const value = dict[key];
    stack.push(key);
    if (typeof value === "object") {
        Object.keys(value).forEach((innerKey, innerIndex) => {
            parse(value, innerKey, innerIndex, results, stack);
        })
    } else {
        results[stack.join(".")] = value;
        stack.pop();
    }
    if (Object.keys(dict).length === (index + 1)) {
        stack.pop();
    }
}
const results = {};
Object.keys(en).forEach((key, index) => {
    parse(en, key, index, results);
});

console.log(results);

