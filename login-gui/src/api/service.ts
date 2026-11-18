import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '../utils/apiFetch.ts';

const SERVICE_NAME_STALE_TIME_MS = 5 * 60 * 1000;

export type ServiceName = {
    name: string;
};

// Service name
export const fetchServiceName = async (id: string): Promise<ServiceName> =>
    apiFetch<ServiceName>(
        `/myconext/api/idp/service/name/${encodeURIComponent(id)}`,
    );
export const useServiceName = (id: string, enabled = true) =>
    useQuery({
        queryKey: ['serviceName', id],
        queryFn: () => fetchServiceName(id),
        enabled: enabled && id !== '',
        staleTime: SERVICE_NAME_STALE_TIME_MS,
    });

// Service name by hash
export const fetchServiceNameByHash = async (
    hash: string,
): Promise<ServiceName> =>
    apiFetch<ServiceName>(
        `/myconext/api/idp/service/hash/${encodeURIComponent(hash)}`,
    );
export const useServiceNameByHash = (hash: string, enabled = true) =>
    useQuery({
        queryKey: ['serviceNameByHash', hash],
        queryFn: () => fetchServiceNameByHash(hash),
        enabled: enabled && hash !== '',
        staleTime: SERVICE_NAME_STALE_TIME_MS,
    });

// Login methods
export const fetchLoginMethods = async (email: string): Promise<string[]> =>
    apiFetch<string[]>('/myconext/api/idp/service/email', {
        method: 'POST',
        body: { email },
    });
