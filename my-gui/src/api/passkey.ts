import type { PublicKeyCredential, User } from './me';
import { request } from './request';

export type WebAuthnToken = { token: string };

export const startWebAuthnFlow = () =>
    request<WebAuthnToken>('/myconext/api/sp/security/webauthn');

// the server redirects back to `/credential?id=…&success=true|false`
export const fetchTestWebAuthnUrl = () =>
    request<{ url: string }>('/myconext/api/sp/testWebAuthnUrl');

export const updateCredential = (credential: PublicKeyCredential) =>
    request<User>('/myconext/api/sp/credential', {
        method: 'POST',
        body: JSON.stringify(credential),
    });

// the server deletes on PUT, like the old app calls it
export const deleteCredential = (credential: PublicKeyCredential) =>
    request<User>('/myconext/api/sp/credential', {
        method: 'PUT',
        body: JSON.stringify(credential),
    });
