export function getParameterByName(
    name: string,
    windowLocationSearch = typeof window === 'undefined'
        ? ''
        : window.location.search,
) {
    const urlSearchParams = new URLSearchParams(windowLocationSearch);
    return urlSearchParams.get(name);
}

export function replaceQueryParameter(
    windowLocationSearch: string,
    name: string,
    value: string,
) {
    const urlSearchParams = new URLSearchParams(windowLocationSearch);
    urlSearchParams.set(name, value);
    return '?' + urlSearchParams.toString();
}
