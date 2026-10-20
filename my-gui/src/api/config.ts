import { request } from './request';

export type Config = {
    isAuthenticated: boolean;
    loginUrl: string;
    [key: string]: unknown;
};

export const fetchConfig = () => request<Config>('/config');
