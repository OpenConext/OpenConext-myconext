import { apiFetch } from './apiFetch';
import type { User } from './me';

export type PasswordCodeVerification = { hash: string };

export type LoginPreferenceResponse = { token: string };

export const fetchOutstandingEmailLinks = () =>
    apiFetch<boolean>('/myconext/api/sp/outstanding-email-links');

export const generatePasswordCode = () =>
    apiFetch<void>('/myconext/api/sp/generate-password-code', {
        method: 'PUT',
        body: {},
    });

export const resendPasswordCode = () =>
    apiFetch<void>('/myconext/api/sp/resend-password-code');

// 401 for a wrong code
export const verifyPasswordCode = (code: string) =>
    apiFetch<PasswordCodeVerification>(
        '/myconext/api/sp/verify-password-code',
        { method: 'PUT', body: { code }, keepOnUnauthorized: true },
    );

// a null password deletes the current password
export const updatePassword = (
    userId: string,
    newPassword: string | null,
    hash: string,
) =>
    apiFetch<User>('/myconext/api/sp/update-password', {
        method: 'PUT',
        body: { userId, newPassword, hash },
    });

export const setLoginPreference = (loginPreference: string) =>
    apiFetch<LoginPreferenceResponse>('/myconext/api/sp/login-preference', {
        method: 'PUT',
        body: { loginPreference },
    });

// false when the hash of a mailed link is unknown or expired
export const isPasswordHashValid = (hash: string) =>
    apiFetch<boolean>(
        `/myconext/api/sp/password-reset-hash-valid?hash=${encodeURIComponent(hash)}`,
    );
