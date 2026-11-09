import { apiFetch } from './apiFetch';
import type { User } from './me';

export type EmailCodeVerification = { hash: string };

// 409 when the address is taken, 406 when a password change is outstanding
// and `force` is not set
export const generateEmailCode = (email: string, force = false) =>
    apiFetch<User>(
        `/myconext/api/sp/generate-email-code${force ? '?force=true' : ''}`,
        { method: 'PUT', body: { email } },
    );

export const resendEmailCode = () =>
    apiFetch<unknown>('/myconext/api/sp/resend-email-code');

// 401 for a wrong code, 403 or 400 when the user has used all attempts
export const verifyEmailCode = (code: string) =>
    apiFetch<EmailCodeVerification>('/myconext/api/sp/verify-email-code', {
        method: 'PUT',
        body: { code },
        keepOnUnauthorized: true,
    });

// confirms the change with the hash from the mailed link or the verified code
export const confirmEmail = (hash: string) =>
    apiFetch<User>(
        `/myconext/api/sp/confirm-email?h=${encodeURIComponent(hash)}`,
    );
