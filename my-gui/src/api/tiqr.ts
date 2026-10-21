import type { User } from './me';
import { request } from './request';

// the server's enrollment states, polled until the app has processed it
export type EnrollmentStatus =
    'INITIALIZED' | 'RETRIEVED' | 'PROCESSED' | 'FINALIZED' | 'VALIDATED';

export type StartEnrollment = {
    enrollmentKey: string;
    // deep link that opens the eduID app
    url: string;
    // data URI of the QR code for `url`
    qrcode: string;
};

// fails with a 403 when the user already has an app registration
export const startEnrollment = () =>
    request<StartEnrollment>('/tiqr/sp/start-enrollment');

export const pollEnrollment = (enrollmentKey: string) =>
    request<EnrollmentStatus>(
        `/tiqr/poll-enrollment?enrollmentKey=${encodeURIComponent(enrollmentKey)}`,
    );

export const sendDeactivationCode = () =>
    request<void>('/tiqr/sp/send-deactivation-phone-code');

// 429 when the maximum number of attempts is used
export const deactivateApp = (verificationCode: string) =>
    request<User>('/tiqr/sp/deactivate-app', {
        method: 'POST',
        body: JSON.stringify({ verificationCode }),
    });

export type BackupCode = { recoveryCode: string };

export type EnrollmentVerificationKey = { enrollmentVerificationKey: string };

// the server answers 406 when a recovery method already exists
export const sendPhoneCode = (phoneNumber: string) =>
    request<unknown>('/tiqr/sp/send-phone-code', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber }),
    });

// 429 when the maximum number of attempts is used
export const verifyPhoneCode = (phoneVerification: string) =>
    request<unknown>('/tiqr/sp/verify-phone-code', {
        method: 'POST',
        body: JSON.stringify({ phoneVerification }),
    });

export const generateBackupCode = () =>
    request<BackupCode>('/tiqr/sp/generate-backup-code');

// changing the recovery method of an active app, only after the app confirmed
export const regenerateBackupCode = () =>
    request<BackupCode>('/tiqr/sp/re-generate-backup-code');

export const resendPhoneCode = (phoneNumber: string) =>
    request<unknown>('/tiqr/sp/re-send-phone-code', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber }),
    });

export const reverifyPhoneCode = (phoneVerification: string) =>
    request<unknown>('/tiqr/sp/re-verify-phone-code', {
        method: 'POST',
        body: JSON.stringify({ phoneVerification }),
    });

// the key is exchanged on the login domain, which sets the secure cookie
export const finishEnrollment = () =>
    request<EnrollmentVerificationKey>('/tiqr/sp/finish-enrollment');

export type StartAuthentication = {
    // data URI of the QR code for `url`
    qr: string;
    // deep link that opens the eduID app
    url: string;
    sessionKey: string;
    // without the cookie the app has not seen this browser and no push is sent
    tiqrCookiePresent: boolean;
};

export type AuthenticationStatus = 'PENDING' | 'SUSPENDED' | 'SUCCESS';

export type PollAuthentication = {
    status: AuthenticationStatus;
    // epoch milliseconds, when suspended
    suspendedUntil?: number;
};

// second-factor confirmation for destructive actions
export const startAuthentication = () =>
    request<StartAuthentication>('/tiqr/sp/start-authentication', {
        method: 'POST',
        body: JSON.stringify({}),
    });

export const pollAuthentication = (sessionKey: string) =>
    request<PollAuthentication>(
        `/tiqr/sp/poll-authentication?sessionKey=${encodeURIComponent(sessionKey)}`,
    );

// marks the session as confirmed for the actions that follow
export const confirmStepUp = (sessionKey: string) =>
    request<unknown>('/tiqr/sp/confirm-step-up', {
        method: 'POST',
        body: JSON.stringify({ sessionKey }),
    });

// the code the app shows when the phone is offline, 400 when invalid
export const manualResponse = (sessionKey: string, response: string) =>
    request<unknown>('/tiqr/sp/manual-response', {
        method: 'POST',
        body: JSON.stringify({ sessionKey, response }),
    });
