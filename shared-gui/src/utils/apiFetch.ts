export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export type ApiFetchOptions = {
    method?: HttpMethod;
    body?: unknown;
    // prevents a reload on unauthorized, useful for verification code dialogs for example
    keepOnUnauthorized?: boolean;
};

export type ApiFetchConfig = {
    // sent as Accept-Language, so server-side messages match the GUI language
    getLocale?: () => string;
    reloadOnSessionLoss?: boolean;
};

export type ApiFetch = <T = void>(
    url: string,
    options?: ApiFetchOptions,
) => Promise<T>;

export const createApiFetch = (config: ApiFetchConfig = {}): ApiFetch => {
    let csrfToken: string | null = null;

    return async <T = void>(
        url: string,
        {
            method = 'GET',
            body,
            keepOnUnauthorized = false,
        }: ApiFetchOptions = {},
    ): Promise<T> => {
        const hasBody = body !== undefined;
        const headers: Record<string, string> = {
            Accept: 'application/json',
        };
        if (hasBody) {
            headers['Content-Type'] = 'application/json';
        }
        const locale = config.getLocale?.();
        if (locale) {
            headers['Accept-Language'] = locale;
        }
        if (csrfToken) {
            headers['X-CSRF-TOKEN'] = csrfToken;
        }

        const res = await fetch(url, {
            method,
            credentials: 'same-origin',
            redirect: config.reloadOnSessionLoss ? 'manual' : 'follow',
            headers,
            ...(hasBody ? { body: JSON.stringify(body) } : {}),
        });
        csrfToken = res.headers.get('x-csrf-token') ?? csrfToken;

        if (config.reloadOnSessionLoss) {
            if (res.type === 'opaqueredirect') {
                window.location.reload();
                return undefined as T;
            }
            if (res.status === 401 && !keepOnUnauthorized) {
                window.location.reload();
            }
        }
        if (!res.ok) {
            throw res;
        }
        if (
            res.status === 204 ||
            !res.headers.get('content-type')?.includes('application/json')
        ) {
            return undefined as T;
        }
        return (await res.json()) as T;
    };
};
