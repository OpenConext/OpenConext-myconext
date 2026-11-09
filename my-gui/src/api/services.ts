import { apiFetch } from './apiFetch';
import type { User } from './me';

export type TokenScope = {
    name: string;
    descriptions: Record<string, string>;
};

export type OidcToken = {
    id: string;
    clientId: string;
    type: string;
    scopes?: TokenScope[];
    // epoch milliseconds
    createdAt: number;
    expiresIn: number;
};

export const fetchTokens = () =>
    apiFetch<OidcToken[]>('/myconext/api/sp/tokens');

// removes the eduID for the service and revokes its tokens
export const deleteServiceAndTokens = (
    serviceProviderEntityId: string,
    tokens: OidcToken[],
) =>
    apiFetch<User>('/myconext/api/sp/service', {
        method: 'PUT',
        body: { serviceProviderEntityId, tokens },
    });

export const deleteTokens = (tokens: OidcToken[]) =>
    apiFetch<User>('/myconext/api/sp/tokens', {
        method: 'PUT',
        body: { tokens },
    });
