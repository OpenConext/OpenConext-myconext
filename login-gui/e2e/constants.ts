export const SERVER_URL = process.env.E2E_SERVER_URL ?? 'http://localhost:8081';

// AssertionConsumerServiceURL in the AuthnRequest of utils/authnRequest.ts
export const ACS_URL_PATTERN =
    'https://engine.test.surfconext.nl/authentication/sp/consume-assertion';

export const LINKING_ACRS = [
    {
        name: 'linked-institution',
        acr: 'https://eduid.nl/trust/linked-institution',
        explanation: 'linked_institution',
    },
    {
        name: 'validate-names',
        acr: 'https://eduid.nl/trust/validate-names',
        explanation: 'validate_names',
    },
    {
        name: 'validate-names-external',
        acr: 'https://eduid.nl/trust/validate-names-external',
        explanation: 'validate_names_external',
    },
    {
        name: 'iap-medium',
        acr: 'https://refeds.org/assurance/IAP/medium',
        explanation: 'any_link',
    },
    {
        name: 'iap-high',
        acr: 'https://refeds.org/assurance/IAP/high',
        explanation: 'any_link',
    },
] as const;

export const PROFILE_MFA_ACR = 'https://refeds.org/profile/mfa';

export const withMfa = (acr: string): string => `${acr}/mfa`;
