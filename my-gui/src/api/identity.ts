import type { LinkedAccount, User } from './me';
import { putJson, request } from './request';

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

type AuthorizationUrl = { url: string };

export const updateName = (name: NameUpdate) =>
    putJson<User>('/myconext/api/sp/update', name);

export const startLinkAccountFlow = () =>
    request<AuthorizationUrl>('/myconext/api/sp/oidc/link');

export const startVerifyAccountFlow = (
    idpScoping: 'idin' | 'eherkenning',
    bankId?: string,
) =>
    request<AuthorizationUrl>(
        `/myconext/api/sp/verify/link?idpScoping=${idpScoping}${
            bankId ? `&bankId=${encodeURIComponent(bankId)}` : ''
        }`,
    );

export const fetchIdinIssuers = () =>
    request<VerifyIssuer[]>('/myconext/api/sp/idin/issuers');

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
    request<ControlCode>('/myconext/api/sp/control-code', {
        method: 'POST',
        body: JSON.stringify(controlCode),
    });

export const deleteControlCode = () =>
    request<User>('/myconext/api/sp/control-code', { method: 'DELETE' });

// the server matches the account on these identifiers, the answer is the
// updated user
export const removeLinkedAccount = (account: LinkedAccount) =>
    putJson<User>('/myconext/api/sp/institution', {
        eduPersonPrincipalName: account.eduPersonPrincipalName,
        subjectId: account.subjectId,
        external: account.external,
        idpScoping: account.idpScoping,
        schacHomeOrganization: account.schacHomeOrganization,
    });

// the account the verified names are taken from, the answer is the updated user
export const preferLinkedAccount = (account: LinkedAccount) =>
    putJson<User>('/myconext/api/sp/prefer-linked-account', {
        eduPersonPrincipalName: account.eduPersonPrincipalName,
        subjectId: account.subjectId,
        external: account.external,
        idpScoping: account.idpScoping,
        schacHomeOrganization: account.schacHomeOrganization,
    });
