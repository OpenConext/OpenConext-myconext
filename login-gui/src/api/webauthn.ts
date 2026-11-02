import type {
    CredentialCreationOptionsJSON,
    CredentialRequestOptionsJSON,
} from '@github/webauthn-json';

import { apiFetch } from '../utils/apiFetch.ts';

export type WebAuthnStartResponse = {
    publicKeyCredentialRequestOptions: NonNullable<
        CredentialRequestOptionsJSON['publicKey']
    >;
};

export type WebAuthnFinishResponse = {
    url: string;
};

export const webAuthnStartAuthentication = (
    email: string,
    authenticationRequestId: string,
) =>
    apiFetch<WebAuthnStartResponse>(
        '/myconext/api/idp/security/webauthn/authentication',
        {
            method: 'POST',
            body: { email, authenticationRequestId, test: false },
        },
    );

export type WebAuthnRegistrationOptions = NonNullable<
    CredentialCreationOptionsJSON['publicKey']
>;

export const webAuthnStartRegistration = (token: string) =>
    apiFetch<WebAuthnRegistrationOptions>(
        '/myconext/api/idp/security/webauthn/registration',
        { method: 'POST', body: { token } },
    );


export type WebAuthnRegistrationFinishResponse = {
    location: string;
};    
// `credentials` is a JSON *string*: the server parses it itself.
export const webAuthnFinishRegistration = (
    token: string,
    name: string,
    credentials: string,
    request: WebAuthnRegistrationOptions,
) =>
    apiFetch<WebAuthnRegistrationFinishResponse>(
        '/myconext/api/idp/security/webauthn/registration',
        { method: 'PUT', body: { token, name, credentials, request } },
    );

// `credentials` is a JSON *string*: the server parses it itself.
export const webAuthnFinishAuthentication = (
    credentials: string,
    authenticationRequestId: string,
) =>
    apiFetch<WebAuthnFinishResponse>(
        '/myconext/api/idp/security/webauthn/authentication',
        {
            method: 'PUT',
            body: { credentials, authenticationRequestId },
        },
    );
