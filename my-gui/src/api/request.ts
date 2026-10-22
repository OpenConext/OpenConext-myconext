import I18n from '../locale/I18n';

let csrfToken: string | null = null;

export type RequestBehavior = {
    // set for a wrong verification code: the server answers 401 for it as well
    // as for a lost session, and a reload would close the dialog
    keepOnUnauthorized?: boolean;
};

export async function request<T>(
    path: string,
    options: RequestInit = {},
    { keepOnUnauthorized }: RequestBehavior = {},
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
    // the session is gone, a reload sends the user through the login
    if (res.status === 401 && !keepOnUnauthorized) {
        window.location.reload();
    }
    if (!res.ok) {
        throw res;
    }
    if (res.status === 204) {
        return undefined as T;
    }
    return (await res.json()) as T;
}

export const putJson = <T>(
    path: string,
    body: unknown,
    behavior?: RequestBehavior,
) => request<T>(path, { method: 'PUT', body: JSON.stringify(body) }, behavior);
