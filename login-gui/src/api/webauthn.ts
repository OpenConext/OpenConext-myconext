import type { CredentialRequestOptionsJSON } from '@github/webauthn-json';

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
