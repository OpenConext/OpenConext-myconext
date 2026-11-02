import type {
    CredentialCreationOptionsJSON,
    PublicKeyCredentialWithAttestationJSON,
} from '@github/webauthn-json';
import { create } from '@github/webauthn-json';

import { apiFetch } from '../utils/apiFetch.ts';

export type WebAuthnRegistrationOptions = NonNullable<
    CredentialCreationOptionsJSON['publicKey']
>;

export type WebAuthnRegistrationFinishResponse = {
    location: string;
};

export class InvalidRegistrationTokenError extends Error {}

export type SecurityKeyRegistration =
    | { status: 'registered'; location: string }
    | { status: 'alreadyRegistered' };

export const webAuthnStartRegistration = (token: string) =>
    apiFetch<WebAuthnRegistrationOptions>(
        '/myconext/api/idp/security/webauthn/registration',
        { method: 'POST', body: { token } },
    );

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

// Register

// The three steps are inherently sequential: the browser needs the server's
// challenge, and the server needs the browser's credentials.
export const registerSecurityKey = async (
    token: string,
    name: string,
): Promise<SecurityKeyRegistration> => {
    let request: WebAuthnRegistrationOptions;
    try {
        request = await webAuthnStartRegistration(token);
    } catch {
        throw new InvalidRegistrationTokenError();
    }

    let credentials: PublicKeyCredentialWithAttestationJSON;
    try {
        credentials = await create({ publicKey: request });
    } catch {
        // Browser rejects creation when the key is already registered
        return { status: 'alreadyRegistered' };
    }

    const { location } = await webAuthnFinishRegistration(
        token,
        name,
        // rawId is not supported server-side
        JSON.stringify({ ...credentials, rawId: undefined }),
        request,
    );
    return { status: 'registered', location };
};
