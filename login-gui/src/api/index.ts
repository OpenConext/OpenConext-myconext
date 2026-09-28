export type Config = {
    isAuthenticated: boolean;
    loginUrl: string;
    [key: string]: unknown;
};

export function fetchConfig(): Promise<Config> {
    return fetch('/config', {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' },
    }).then((res) => {
        if (!res.ok) throw res;
        return res.json() as Promise<Config>;
    });
}
