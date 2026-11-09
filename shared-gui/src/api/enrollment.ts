import type { ApiFetch } from '../utils/apiFetch';

// the server's enrollment states, polled until the app has processed it
export type EnrollmentStatus =
    'INITIALIZED' | 'RETRIEVED' | 'PROCESSED' | 'FINALIZED' | 'VALIDATED';

export type StartEnrollmentResponse = {
    enrollmentKey: string;
    // deep link that opens the eduID app
    url: string;
    // data URI of the QR code for `url`
    qrcode: string;
};

export type SendPhoneCodeResponse = {
    status: string;
};

export type VerifyPhoneCodeResponse = {
    redirect: string;
};

export type BackupCodeResponse = {
    recoveryCode: string;
    redirect: string;
};

export type EnrollmentScope =
    { kind: 'session' } | { kind: 'hash'; hash: string };

export const SESSION_SCOPE: EnrollmentScope = { kind: 'session' };

const endpoint = (scope: EnrollmentScope, name: string): string =>
    scope.kind === 'session'
        ? `/tiqr/sp/${name}`
        : `/tiqr/${name}?hash=${encodeURIComponent(scope.hash)}`;

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

export const createEnrollmentApi = (apiFetch: ApiFetch) => ({
    // 403 when the user already has an app registration
    startEnrollment: (scope: EnrollmentScope) =>
        apiFetch<StartEnrollmentResponse>(endpoint(scope, 'start-enrollment')),

    pollEnrollment: (enrollmentKey: string) =>
        apiFetch<EnrollmentStatus>(
            `/tiqr/poll-enrollment?enrollmentKey=${encodeURIComponent(enrollmentKey)}`,
        ),

    // 406: a recovery method is already finalized, 429: SMS rate limit reached
    sendPhoneCode: (scope: EnrollmentScope, phoneNumber: string) =>
        apiFetch<SendPhoneCodeResponse>(endpoint(scope, 'send-phone-code'), {
            method: 'POST',
            body: { phoneNumber },
        }),

    // 403: wrong code, 429: too many attempts
    verifyPhoneCode: (scope: EnrollmentScope, phoneVerification: string) =>
        apiFetch<VerifyPhoneCodeResponse>(
            endpoint(scope, 'verify-phone-code'),
            {
                method: 'POST',
                body: { phoneVerification },
            },
        ),

    // the server only hands out the code once; a repeat call fails with a 403
    generateBackupCode: (scope: EnrollmentScope) =>
        apiFetch<BackupCodeResponse>(endpoint(scope, 'generate-backup-code')),
});

export type EnrollmentApi = ReturnType<typeof createEnrollmentApi>;
