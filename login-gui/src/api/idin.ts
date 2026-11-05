import { queryOptions } from '@tanstack/react-query';

import { apiFetch } from '../utils/apiFetch.ts';

export type IdinIssuer = {
    id: string;
    name: string;
    logo?: string;
};

export const fetchIdinIssuers = async (): Promise<IdinIssuer[]> =>
    apiFetch<IdinIssuer[]>('/myconext/api/sp/idin/issuers');

export const idinIssuersQueryOptions = queryOptions({
    queryKey: ['idinIssuers'],
    queryFn: fetchIdinIssuers,
});

export const verifyLinkUrl = (
    id: string,
    scoping: 'idin' | 'eherkenning',
    bankId?: string,
): string => {
    const base = `/myconext/api/idp/verify/link/${encodeURIComponent(id)}?idpScoping=${scoping}`;
    return bankId ? `${base}&bankId=${encodeURIComponent(bankId)}` : base;
};
