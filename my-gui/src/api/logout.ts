import { request } from './request';

export type LogoutResponse = {
    status: number;
};

export const logout = () => request<LogoutResponse>('/myconext/api/sp/logout');
