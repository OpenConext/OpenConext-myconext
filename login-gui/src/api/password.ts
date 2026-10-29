import { apiFetch } from '../utils/apiFetch.ts';

export type PasswordLoginResponse = {
    url?: string;
    stepup?: boolean;
    explanation?: string;
};

export const passwordLoginRequest = async (
    email: string,
    password: string,
    authenticationRequestId: string,
) =>
    apiFetch<PasswordLoginResponse>('/myconext/api/idp/generate_code_request', {
        method: 'PUT',
        body: {
            user: { email, password },
            authenticationRequestId,
            usePassword: true,
        },
    });
