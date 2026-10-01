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

export function redirectToLogin(loginUrl: string, redirectTo?: string): void {
    const url = new URL(loginUrl);
    url.searchParams.set('redirect_path', redirectTo || '/');
    url.searchParams.set('registration_id', 'my_conext');
    window.location.href = url.toString();
}
