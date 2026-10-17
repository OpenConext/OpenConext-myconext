export function isEmpty(obj: unknown) {
    if (obj === undefined || obj === null) {
        return true;
    }
    if (Array.isArray(obj)) {
        return obj.length === 0;
    }
    if (typeof obj === 'string') {
        return obj.trim().length === 0;
    }
    if (obj instanceof Date) {
        // An invalid Date has NaN as time value, and NaN !== NaN
        return Number.isNaN(obj.getTime());
    }
    if (typeof obj === 'object') {
        return Object.keys(obj).length === 0;
    }
    return false;
}
