import { request } from './request';

export type Config = {
    isAuthenticated: boolean;
    loginUrl: string;
    supportedLanguages?: string;
    emailSpamThresholdSeconds?: number;
    accountBaseUrl?: string;
    accountWebAuthUrl?: string;
    enableAccountLinking?: boolean;
    featureIdVerify?: boolean;
    featureWebAuthn?: boolean;
    useApp?: boolean;
    featureServiceDeskActive?: boolean;
    [key: string]: unknown;
};

export const fetchConfig = () => request<Config>('/config');
