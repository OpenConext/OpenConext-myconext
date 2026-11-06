import { apiFetch } from '../utils/apiFetch.ts';

export type EnrollmentStatus =
    'INITIALIZED' | 'RETRIEVED' | 'PROCESSED' | 'FINALIZED' | 'VALIDATED';

export type StartEnrollmentResponse = {
    enrollmentKey: string;
    // Deep link that opens the eduID app
    url: string;
    // Base64 data URI of the QR code for `url`
    qrcode: string;
};

export type BackupCodeResponse = {
    recoveryCode: string;
    redirect: string;
};

// Fails with a 403 when the user already has an app registration
export const startEnrollment = (hash: string) =>
    apiFetch<StartEnrollmentResponse>(
        `/tiqr/start-enrollment?hash=${encodeURIComponent(hash)}`,
    );

export const pollEnrollment = (enrollmentKey: string) =>
    apiFetch<EnrollmentStatus>(
        `/tiqr/poll-enrollment?enrollmentKey=${encodeURIComponent(enrollmentKey)}`,
    );

export type VerifyPhoneCodeResponse = {
    redirect: string;
};

export type SendError = 'finalized' | 'rateLimited' | 'failed';

export const toSendError = (error: unknown): SendError => {
    if (error instanceof Response) {
        if (error.status === 406) {
            return 'finalized';
        }
        if (error.status === 429) {
            return 'rateLimited';
        }
    }
    return 'failed';
};

// 406: a recovery method is already finalized, 429: SMS rate limit reached
export const sendPhoneCode = (hash: string, phoneNumber: string) =>
    apiFetch(`/tiqr/send-phone-code?hash=${encodeURIComponent(hash)}`, {
        method: 'POST',
        body: { phoneNumber },
    });

// 403: wrong code, 429: too many attempts
export const verifyPhoneCode = (hash: string, phoneVerification: string) =>
    apiFetch<VerifyPhoneCodeResponse>(
        `/tiqr/verify-phone-code?hash=${encodeURIComponent(hash)}`,
        { method: 'POST', body: { phoneVerification } },
    );

// The server only hands out the code once; a repeat call fails with a 403
export const generateBackupCode = (hash: string) =>
    apiFetch<BackupCodeResponse>(
        `/tiqr/generate-backup-code?hash=${encodeURIComponent(hash)}`,
    );
