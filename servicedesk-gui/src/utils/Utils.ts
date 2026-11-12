export function stopEvent(
    e?: { preventDefault: () => void; stopPropagation: () => void } | null,
) {
    if (e !== undefined && e !== null) {
        e.preventDefault();
        e.stopPropagation();
        return false;
    }
    return true;
}

export function isEmpty(obj: unknown): boolean {
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
        return Number.isNaN(obj.getTime());
    }
    if (typeof obj === 'object') {
        return Object.keys(obj).length === 0;
    }
    return false;
}

export const sanitizeURL = (url: string): string => {
    const protocol = new URL(url).protocol;
    return ['https:', 'http:'].includes(protocol) ? url : 'about:blank';
};

export const distinctValues = <T extends Record<string, unknown>>(
    arr: T[],
    attribute: keyof T,
): T[] => {
    const seen = new Set<unknown>();
    return arr.filter((obj) => {
        if (seen.has(obj[attribute])) {
            return false;
        }
        seen.add(obj[attribute]);
        return true;
    });
};
