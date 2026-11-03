import { apiFetch } from '../utils/apiFetch.ts';

export type ServiceName = {
    name: string;
};

export const fetchServiceName = async (id: string): Promise<ServiceName> =>
    apiFetch<ServiceName>(
        `/myconext/api/idp/service/name/${encodeURIComponent(id)}`,
    );

export const fetchServiceNameByHash = async (
    hash: string,
): Promise<ServiceName> =>
    apiFetch<ServiceName>(
        `/myconext/api/idp/service/hash/${encodeURIComponent(hash)}`,
    );

export const fetchLoginMethods = async (email: string): Promise<string[]> =>
    apiFetch<string[]>('/myconext/api/idp/service/email', {
        method: 'POST',
        body: { email },
    });
