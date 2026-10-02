import { apiFetch } from '../utils/apiFetch.ts';

export const generateCodeRequest = async (
    email: string,
    authenticationRequestId: string,
) =>
    apiFetch('/myconext/api/idp/generate_code_request', {
        method: 'PUT',
        body: { user: { email }, authenticationRequestId },
    });

export type VerifyCodeResponse = {
    url: string;
    email: string;
};

export const verifyCodeRequest = async (
    code: string,
    authenticationRequestId: string,
) =>
    apiFetch<VerifyCodeResponse>('/myconext/api/idp/verify_code_request', {
        method: 'PUT',
        body: { code, authenticationRequestId },
    });
