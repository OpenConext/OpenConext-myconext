import { apiFetch } from './apiFetch';
import type { LinkedAccount, User } from './me';

// only the sent fields change, but the server resets chosenName to givenName
// when givenName is sent without chosenName
export type NameUpdate = Partial<{
    chosenName: string;
    givenName: string;
    familyName: string;
}>;

export type VerifyIssuer = {
    id: string;
    name: string;
    logo?: string;
};

export type AuthorizationUrl = { url: string };

export const updateName = (name: NameUpdate) =>
    apiFetch<User>('/myconext/api/sp/update', { method: 'PUT', body: name });

export const startLinkAccountFlow = () =>
    apiFetch<AuthorizationUrl>('/myconext/api/sp/oidc/link');

export const startVerifyAccountFlow = (
    idpScoping: 'idin' | 'eherkenning',
    bankId?: string,
) =>
    apiFetch<AuthorizationUrl>(
        `/myconext/api/sp/verify/link?idpScoping=${idpScoping}${
            bankId ? `&bankId=${encodeURIComponent(bankId)}` : ''
        }`,
    );

export const fetchIdinIssuers = () =>
    apiFetch<VerifyIssuer[]>('/myconext/api/sp/idin/issuers');

export type ControlCode = {
    firstName: string;
    lastName: string;
    // `<day> <MON> <year>`, for example `6 MAY 1931`
    dayOfBirth: string;
    code: string;
    createdAt: number;
};

export type ControlCodeRequest = Pick<
    ControlCode,
    'firstName' | 'lastName' | 'dayOfBirth'
>;

export const createControlCode = (controlCode: ControlCodeRequest) =>
    apiFetch<ControlCode>('/myconext/api/sp/control-code', {
        method: 'POST',
        body: controlCode,
    });

export const deleteControlCode = () =>
    apiFetch<User>('/myconext/api/sp/control-code', { method: 'DELETE' });

// the server matches the account on these identifiers, the answer is the
// updated user
export const removeLinkedAccount = (account: LinkedAccount) =>
    apiFetch<User>('/myconext/api/sp/institution', {
        method: 'PUT',
        body: {
            eduPersonPrincipalName: account.eduPersonPrincipalName,
            subjectId: account.subjectId,
            external: account.external,
            idpScoping: account.idpScoping,
            schacHomeOrganization: account.schacHomeOrganization,
        },
    });

// the account the verified names are taken from, the answer is the updated user
export const preferLinkedAccount = (account: LinkedAccount) =>
    apiFetch<User>('/myconext/api/sp/prefer-linked-account', {
        method: 'PUT',
        body: {
            eduPersonPrincipalName: account.eduPersonPrincipalName,
            subjectId: account.subjectId,
            external: account.external,
            idpScoping: account.idpScoping,
            schacHomeOrganization: account.schacHomeOrganization,
        },
    });
