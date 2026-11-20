import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '../utils/apiFetch.ts';

// Subset of the account returned for iDIN / eIDAS style external links.
export type ExternalLinkedAccount = {
    issuer?: { id: string; name: string };
    firstName?: string;
    legalLastName?: string;
    // epoch millis at midnight UTC, only delivered by iDIN and eIDAS accounts
    dateOfBirth?: number | null;
    idpScoping?: string;
};

export type UserInfo = {
    externalLinkedAccounts?: ExternalLinkedAccount[];
};

export const fetchUserInfoByHash = async (hash: string): Promise<UserInfo> =>
    apiFetch<UserInfo>(`/myconext/api/idp/me/${encodeURIComponent(hash)}`);

export const useUserInfoByHash = (hash: string) =>
    useQuery({
        queryKey: ['userInfoByHash', hash],
        queryFn: () => fetchUserInfoByHash(hash),
        enabled: hash !== '',
    });
