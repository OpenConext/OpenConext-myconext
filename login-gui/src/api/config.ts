import { queryOptions } from '@tanstack/react-query';

import { apiFetch } from '../utils/apiFetch.ts';

export type Config = {
    magicLinkUrl: string;
    mobileAppRedirect: string;
    baseDomain: string;
    supportedLanguages: string;
    captchaEnabled: boolean;
    captchaSiteKey: string;
    myconextWebAuthnRedirectUrl: string;
    featureIdVerify: boolean;
    featureServiceDeskActive: boolean;
    [key: string]: unknown;
};

export const fetchConfig = async (): Promise<Config> =>
    apiFetch<Config>('/config');

export const configQueryOptions = queryOptions({
    queryKey: ['config'],
    queryFn: fetchConfig,
});
