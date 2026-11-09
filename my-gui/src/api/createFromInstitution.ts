import { apiFetch } from './apiFetch';
import type { AuthorizationUrl } from './identity';

export type InstitutionInfo = {
    given_name?: string;
    [key: string]: unknown;
};

export type InstitutionVerification = {
    // where to continue, for example back to a service provider
    location?: string;
};

// `forceAuth` makes the institution ask the user to log in again,
// `loginHint` (an IdP entityID) makes the gateway skip the WAYF
export const startCreateFromInstitutionFlow = (
    forceAuth = false,
    returnTo?: string,
    loginHint?: string,
) => {
    // tells the server which GUI started the flow, so it ends up in login-gui
    const params = new URLSearchParams({
        forceAuth: `${forceAuth}`,
        app: 'my_gui',
    });
    if (returnTo) {
        params.set('return_to', returnTo);
    }
    if (loginHint) {
        params.set('login_hint', loginHint);
    }
    return apiFetch<AuthorizationUrl>(
        `/myconext/api/sp/create-from-institution?${params.toString()}`,
    );
};

export const fetchInstitutionInfo = (hash: string) =>
    apiFetch<InstitutionInfo>(
        `/myconext/api/sp/create-from-institution/info?hash=${encodeURIComponent(hash)}`,
    );

// 409 when the email is in use, 412 when the domain is not allowed
export const createInstitutionEduId = (
    email: string,
    hash: string,
    newUser: boolean,
) =>
    apiFetch<unknown>('/myconext/api/sp/create-from-institution/email', {
        method: 'POST',
        body: { email, hash, newUser },
    });

export const institutionalEmailDomains = () =>
    apiFetch<string[]>(
        '/myconext/api/sp/create-from-institution/domain/institutional',
    );

export const allowedEmailDomains = () =>
    apiFetch<string[]>(
        '/myconext/api/sp/create-from-institution/domain/allowed',
    );

export const resendInstitutionMail = (hash: string) =>
    apiFetch<unknown>(
        `/myconext/api/sp/create-from-institution/resendMail?hash=${encodeURIComponent(hash)}`,
    );

// 401 for a wrong code, 403 or 400 when the user has used all attempts
export const verifyInstitutionEduId = (hash: string, code: string) =>
    apiFetch<InstitutionVerification>(
        '/myconext/api/sp/create-from-institution/verify',
        { method: 'PUT', body: { code, hash }, keepOnUnauthorized: true },
    );
