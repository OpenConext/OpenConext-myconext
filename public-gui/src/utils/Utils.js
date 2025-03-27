export const isEmpty = obj => {
    if (obj === undefined || obj === null) {
        return true;
    }
    if (Array.isArray(obj)) {
        return obj.length === 0;
    }
    if (typeof obj === "string") {
        return obj.trim().length === 0;
    }
    if (typeof obj === "object") {
        return Object.keys(obj).length === 0;
    }
    return false;
}

export const stopEvent = e => {
    if (e !== undefined && e !== null) {
        e.preventDefault();
        e.stopPropagation();
        return false;
    }
    return true;
}

