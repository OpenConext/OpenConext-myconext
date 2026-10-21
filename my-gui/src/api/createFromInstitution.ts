import type { AuthorizationUrl } from './identity';
import { putJson, request } from './request';

export type InstitutionInfo = {
    given_name?: string;
    [key: string]: unknown;
};

export type InstitutionVerification = {
    // where to continue, for example back to a service provider
    location?: string;
};

// `forceAuth` makes the institution ask the user to log in again
export const startCreateFromInstitutionFlow = (
    forceAuth = false,
    returnTo?: string,
) => {
    const params = new URLSearchParams({ forceAuth: `${forceAuth}` });
    if (returnTo) {
        params.set('return_to', returnTo);
    }
    return request<AuthorizationUrl>(
        `/myconext/api/sp/create-from-institution?${params.toString()}`,
    );
};

export const fetchInstitutionInfo = (hash: string) =>
    request<InstitutionInfo>(
        `/myconext/api/sp/create-from-institution/info?hash=${encodeURIComponent(hash)}`,
    );

// 409 when the email is in use, 412 when the domain is not allowed
export const createInstitutionEduId = (
    email: string,
    hash: string,
    newUser: boolean,
) =>
    request<unknown>('/myconext/api/sp/create-from-institution/email', {
        method: 'POST',
        body: JSON.stringify({ email, hash, newUser }),
    });

export const institutionalEmailDomains = () =>
    request<string[]>(
        '/myconext/api/sp/create-from-institution/domain/institutional',
    );

export const allowedEmailDomains = () =>
    request<string[]>(
        '/myconext/api/sp/create-from-institution/domain/allowed',
    );

export const resendInstitutionMail = (hash: string) =>
    request<unknown>(
        `/myconext/api/sp/create-from-institution/resendMail?hash=${encodeURIComponent(hash)}`,
    );

// 403 or 400 when the user has used all attempts
export const verifyInstitutionEduId = (hash: string, code: string) =>
    putJson<InstitutionVerification>(
        '/myconext/api/sp/create-from-institution/verify',
        { code, hash },
    );
