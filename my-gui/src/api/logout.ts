import { apiFetch } from './apiFetch';

export type LogoutResponse = {
    status: number;
};

export const logout = () => apiFetch<LogoutResponse>('/myconext/api/sp/logout');
