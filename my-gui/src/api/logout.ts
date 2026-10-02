export type LogoutResponse = {
    status: number;
};

export async function logout(): Promise<LogoutResponse> {
    const res = await fetch('/myconext/api/sp/logout', {
        credentials: 'same-origin',
        headers: { Accept: 'application/json' },
    });
    if (!res.ok) {
        throw res;
    }
    return (await res.json()) as LogoutResponse;
}
