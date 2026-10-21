import type { User } from './me';
import { putJson, request } from './request';

export type EmailCodeVerification = { hash: string };

// 409 when the address is taken, 406 when a password change is outstanding
// and `force` is not set
export const generateEmailCode = (email: string, force = false) =>
    putJson<User>(
        `/myconext/api/sp/generate-email-code${force ? '?force=true' : ''}`,
        { email },
    );

export const resendEmailCode = () =>
    request<unknown>('/myconext/api/sp/resend-email-code');

// 403 or 400 when the user has used all attempts
export const verifyEmailCode = (code: string) =>
    putJson<EmailCodeVerification>('/myconext/api/sp/verify-email-code', {
        code,
    });

// confirms the change with the hash from the mailed link or the verified code
export const confirmEmail = (hash: string) =>
    request<User>(
        `/myconext/api/sp/confirm-email?h=${encodeURIComponent(hash)}`,
    );
