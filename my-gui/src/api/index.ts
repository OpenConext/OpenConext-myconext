export type Config = {
    isAuthenticated: boolean;
    loginUrl: string;
    [key: string]: unknown;
};

export async function fetchConfig(): Promise<Config> {
    const res = await fetch('/config', {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' },
    });
    if (!res.ok) {
        throw res;
    }
    return await ((await res.json()) as Promise<Config>);
}
