import type { User } from './me';
import { putJson, request } from './request';

export type PasswordCodeVerification = { hash: string };

export type LoginPreferenceResponse = { token: string };

export const fetchOutstandingEmailLinks = () =>
    request<boolean>('/myconext/api/sp/outstanding-email-links');

export const generatePasswordCode = () =>
    putJson<void>('/myconext/api/sp/generate-password-code', {});

export const resendPasswordCode = () =>
    request<void>('/myconext/api/sp/resend-password-code');

export const verifyPasswordCode = (code: string) =>
    putJson<PasswordCodeVerification>('/myconext/api/sp/verify-password-code', {
        code,
    });

// a null password deletes the current password
export const updatePassword = (
    userId: string,
    newPassword: string | null,
    hash: string,
) =>
    putJson<User>('/myconext/api/sp/update-password', {
        userId,
        newPassword,
        hash,
    });

export const setLoginPreference = (loginPreference: string) =>
    putJson<LoginPreferenceResponse>('/myconext/api/sp/login-preference', {
        loginPreference,
    });
