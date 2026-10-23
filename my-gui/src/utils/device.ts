export const isIos = (): boolean =>
    /iPad|iPhone|iPod/.test(navigator.userAgent) ||
    // iPadOS reports itself as a Mac
    (/Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints > 1);

export const isAndroid = (): boolean => /android/i.test(navigator.userAgent);

export const isMobile = (): boolean => isIos() || isAndroid();
