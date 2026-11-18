export const SERVER_URL = process.env.E2E_SERVER_URL ?? 'http://localhost:8081';

// AssertionConsumerServiceURL in the AuthnRequest of utils/authnRequest.ts
export const ACS_URL_PATTERN =
    'https://engine.test.surfconext.nl/authentication/sp/consume-assertion';
