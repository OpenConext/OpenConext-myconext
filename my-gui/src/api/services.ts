import type { User } from './me';
import { putJson, request } from './request';

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
    request<OidcToken[]>('/myconext/api/sp/tokens');

// removes the eduID for the service and revokes its tokens
export const deleteServiceAndTokens = (
    serviceProviderEntityId: string,
    tokens: OidcToken[],
) =>
    putJson<User>('/myconext/api/sp/service', {
        serviceProviderEntityId,
        tokens,
    });

export const deleteTokens = (tokens: OidcToken[]) =>
    putJson<User>('/myconext/api/sp/tokens', { tokens });
