import { apiFetch } from '../utils/apiFetch.ts';

export type Config = {
    isAuthenticated: boolean;
    loginUrl: string;
    captchaEnabled?: boolean;
    captchaSiteKey?: string;
    [key: string]: unknown;
};

export const fetchConfig = async (): Promise<Config> =>
    apiFetch<Config>('/config');
