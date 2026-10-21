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
    featureOidcTokenAPI?: boolean;
    // days a linked account without validated names stays valid
    expirationNonValidatedDurationDays?: number;
    createEduIDInstitutionEnabled?: boolean;
    createEduIDInstitutionLanding?: boolean;
    featureWarningEducationalEmailDomain?: boolean;
    featureAllowList?: boolean;
    myconextBaseUrl?: string;
    [key: string]: unknown;
};

export const fetchConfig = () => request<Config>('/config');
