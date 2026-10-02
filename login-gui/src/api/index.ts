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

export type ServiceName = {
    name: string;
};

export async function fetchServiceName(id: string): Promise<ServiceName> {
    const res = await fetch(
        `/myconext/api/idp/service/name/${encodeURIComponent(id)}`,
        { credentials: 'same-origin', headers: { Accept: 'application/json' } },
    );
    if (!res.ok) {
        throw res;
    }
    return (await res.json()) as Promise<ServiceName>;
}

export async function fetchLoginMethods(email: string): Promise<string[]> {
    const res = await fetch('/myconext/api/idp/service/email', {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
        },
        body: JSON.stringify({ email }),
    });
    if (!res.ok) {
        throw res;
    }
    return (await res.json()) as Promise<string[]>;
}

export async function generateCodeRequest(
    email: string,
    authenticationRequestId: string,
): Promise<void> {
    const res = await fetch('/myconext/api/idp/generate_code_request', {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
        },
        body: JSON.stringify({ user: { email }, authenticationRequestId }),
    });
    if (!res.ok) {
        throw res;
    }
}

export type VerifyCodeResponse = {
    url: string;
    email: string;
};

export async function verifyCodeRequest(
    code: string,
    authenticationRequestId: string,
): Promise<VerifyCodeResponse> {
    console.log('verifyCodeRequest()', { code, authenticationRequestId });

    const res = await fetch('/myconext/api/idp/verify_code_request', {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
        },
        body: JSON.stringify({ code, authenticationRequestId }),
    });
    if (!res.ok) {
        throw res;
    }
    return (await res.json()) as Promise<VerifyCodeResponse>;
}
