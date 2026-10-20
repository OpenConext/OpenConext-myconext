import I18n from '../locale/I18n';

let csrfToken: string | null = null;

export async function request<T>(
    path: string,
    options: RequestInit = {},
): Promise<T> {
    const headers: Record<string, string> = {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        'Accept-Language': I18n.locale,
    };
    if (csrfToken) {
        headers['X-CSRF-TOKEN'] = csrfToken;
    }

    const res = await fetch(path, {
        credentials: 'same-origin',
        redirect: 'manual',
        ...options,
        headers,
    });
    csrfToken = res.headers.get('x-csrf-token') ?? csrfToken;

    if (res.type === 'opaqueredirect') {
        window.location.reload();
        return undefined as T;
    }
    if (!res.ok) {
        throw res;
    }
    if (res.status === 204) {
        return undefined as T;
    }
    return (await res.json()) as T;
}

export const putJson = <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) });
