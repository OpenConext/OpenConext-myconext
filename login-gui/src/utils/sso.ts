/**
 * Builds the URL to hand the browser back to the server's magic-link endpoint.
 * Returns null when params are missing or `redirect` does not point at `magicLinkUrl`,
 * so a crafted link cannot turn this page into an open redirect.
 */
export const getProceedUrl = (
    search: URLSearchParams,
    magicLinkUrl: string,
): string | null => {
    const redirect = search.get('redirect');
    const hash = search.get('h');
    if (!redirect || !hash || !magicLinkUrl) {
        return null;
    }

    let target: URL;
    let allowed: URL;
    try {
        target = new URL(redirect);
        allowed = new URL(magicLinkUrl);
    } catch {
        return null;
    }

    // A plain startsWith on strings would accept e.g. https://host.evil.com for https://host.
    const prefix = allowed.pathname.replace(/\/$/, '');
    if (
        target.origin !== allowed.origin ||
        (target.pathname !== prefix &&
            !target.pathname.startsWith(`${prefix}/`))
    ) {
        return null;
    }

    target.search = new URLSearchParams({ h: hash, force: 'true' }).toString();
    target.hash = '';
    return target.toString();
};
