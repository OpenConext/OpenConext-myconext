export function getParameterByName(
    name: string,
    windowLocationSearch = window.location.search,
) {
    const urlSearchParams = new URLSearchParams(windowLocationSearch);
    return urlSearchParams.get(name);
}
