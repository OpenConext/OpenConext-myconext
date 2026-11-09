import { apiFetch } from './apiFetch';
import type { PublicKeyCredential, User } from './me';

export type WebAuthnToken = { token: string };

export const startWebAuthnFlow = () =>
    apiFetch<WebAuthnToken>('/myconext/api/sp/security/webauthn');

// the server redirects back to `/credential?id=…&success=true|false`
export const fetchTestWebAuthnUrl = () =>
    apiFetch<{ url: string }>('/myconext/api/sp/testWebAuthnUrl');

export const updateCredential = (credential: PublicKeyCredential) =>
    apiFetch<User>('/myconext/api/sp/credential', {
        method: 'POST',
        body: credential,
    });

// the server deletes on PUT, like the old app calls it
export const deleteCredential = (credential: PublicKeyCredential) =>
    apiFetch<User>('/myconext/api/sp/credential', {
        method: 'PUT',
        body: credential,
    });
