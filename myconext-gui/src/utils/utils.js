import {logout} from "../api/index.js";

export function isEmpty(obj) {
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

export const splitListSemantically = (arr, lastSeparator) => {
    return [arr.slice(0, -1).join(", "), arr.slice(-1)[0]].join(arr.length < 2 ? "" : ` ${lastSeparator} `);
}

export const stopEvent = e => {
    if (e !== undefined && e !== null) {
        e.preventDefault();
        e.stopPropagation();
        return false;
    }
    return true;
}

export const doLogOutAfterRateLimit = idpBaseUrl => {
    logout().then(() => {
        window.location.href = `${idpBaseUrl}/doLogout?param=${encodeURIComponent("ratelimit=true")}`;
    });
}

