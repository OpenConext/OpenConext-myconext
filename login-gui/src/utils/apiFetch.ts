export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';

export type FetchOptions = {
    method?: HttpMethod;
    body?: unknown;
};

export const apiFetch = async <T = void>(
    url: string,
    options?: FetchOptions,
): Promise<T> => {
    const hasBody = options?.body !== undefined;
    const res = await fetch(url, {
        method: options?.method ?? 'GET',
        credentials: 'same-origin',
        headers: {
            Accept: 'application/json',
            ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
        },
        ...(hasBody ? { body: JSON.stringify(options!.body) } : {}),
    });

    if (!res.ok) {
        throw res;
    }

    if (res.headers.get('content-type')?.includes('application/json')) {
        return (await res.json()) as Promise<T>;
    }

    return undefined as T;
};
