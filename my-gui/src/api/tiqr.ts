import { SESSION_SCOPE, createEnrollmentApi } from '@myconext/shared-gui';
import type {
    BackupCodeResponse,
    SendPhoneCodeResponse,
    VerifyPhoneCodeResponse,
} from '@myconext/shared-gui';

import { apiFetch } from './apiFetch';
import type { User } from './me';

export type {
    BackupCodeResponse,
    EnrollmentStatus,
    StartEnrollmentResponse,
} from '@myconext/shared-gui';

const enrollment = createEnrollmentApi(apiFetch);

export const startEnrollment = () => enrollment.startEnrollment(SESSION_SCOPE);

export const { pollEnrollment } = enrollment;

export const sendDeactivationCode = () =>
    apiFetch<void>('/tiqr/sp/send-deactivation-phone-code');

// 429 when the maximum number of attempts is used
export const deactivateApp = (verificationCode: string) =>
    apiFetch<User>('/tiqr/sp/deactivate-app', {
        method: 'POST',
        body: { verificationCode },
    });

export type EnrollmentVerificationKey = { enrollmentVerificationKey: string };

export const sendPhoneCode = (phoneNumber: string) =>
    enrollment.sendPhoneCode(SESSION_SCOPE, phoneNumber);

export const verifyPhoneCode = (phoneVerification: string) =>
    enrollment.verifyPhoneCode(SESSION_SCOPE, phoneVerification);

export const generateBackupCode = () =>
    enrollment.generateBackupCode(SESSION_SCOPE);

// changing the recovery method of an active app, only after the app confirmed
export const regenerateBackupCode = () =>
    apiFetch<BackupCodeResponse>('/tiqr/sp/re-generate-backup-code');

export const resendPhoneCode = (phoneNumber: string) =>
    apiFetch<SendPhoneCodeResponse>('/tiqr/sp/re-send-phone-code', {
        method: 'POST',
        body: { phoneNumber },
    });

export const reverifyPhoneCode = (phoneVerification: string) =>
    apiFetch<VerifyPhoneCodeResponse>('/tiqr/sp/re-verify-phone-code', {
        method: 'POST',
        body: { phoneVerification },
    });

// the key is exchanged on the login domain, which sets the secure cookie
export const finishEnrollment = () =>
    apiFetch<EnrollmentVerificationKey>('/tiqr/sp/finish-enrollment');

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
    apiFetch<StartAuthentication>('/tiqr/sp/start-authentication', {
        method: 'POST',
        body: {},
    });

export const pollAuthentication = (sessionKey: string) =>
    apiFetch<PollAuthentication>(
        `/tiqr/sp/poll-authentication?sessionKey=${encodeURIComponent(sessionKey)}`,
    );

// marks the session as confirmed for the actions that follow
export const confirmStepUp = (sessionKey: string) =>
    apiFetch<unknown>('/tiqr/sp/confirm-step-up', {
        method: 'POST',
        body: { sessionKey },
    });

// the code the app shows when the phone is offline, 400 when invalid
export const manualResponse = (sessionKey: string, response: string) =>
    apiFetch<unknown>('/tiqr/sp/manual-response', {
        method: 'POST',
        body: { sessionKey, response },
    });
