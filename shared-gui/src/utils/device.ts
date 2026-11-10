export const isIos = (): boolean =>
    /iPad|iPhone|iPod/.test(navigator.userAgent) ||
    // iPadOS reports itself as a Mac
    (/Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints > 1);

export const isAndroid = (): boolean => /android/i.test(navigator.userAgent);

export const isMobile = (): boolean => isIos() || isAndroid();

export type AppStoreUrls = {
    appleUrl: string;
    googleUrl: string;
};

// replace, so the back button does not return to this redirect
export const redirectToAppStore = ({
    appleUrl,
    googleUrl,
}: AppStoreUrls): void => {
    if (isIos()) {
        window.location.replace(appleUrl);
    } else if (isAndroid()) {
        window.location.replace(googleUrl);
    }
};

export const isTouchDevice = (): boolean =>
    'ontouchstart' in document.documentElement;
