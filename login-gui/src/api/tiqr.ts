import { apiFetch } from '../utils/apiFetch.ts';

export type TiqrStartResponse = {
    sessionKey: string;
    url: string;
    // Base64 data URI of the QR code
    qr: string;
    tiqrCookiePresent: boolean;
};

export type TiqrPollStatus = 'SUCCESS' | 'SUSPENDED' | (string & {});

export type TiqrPollResponse = {
    status: TiqrPollStatus;
    redirect?: string;
    hash?: string;
    suspendedUntil?: number;
};

export const tiqrStartAuthentication = (
    email: string,
    authenticationRequestId: string,
) =>
    apiFetch<TiqrStartResponse>('/tiqr/start-authentication', {
        method: 'POST',
        body: { email, authenticationRequestId },
    });

export const tiqrPollAuthentication = (
    sessionKey: string,
    authenticationRequestId: string,
) =>
    apiFetch<TiqrPollResponse>(
        `/tiqr/poll-authentication?sessionKey=${encodeURIComponent(sessionKey)}&id=${encodeURIComponent(authenticationRequestId)}`,
    );

export const tiqrManualResponse = (sessionKey: string, response: string) =>
    apiFetch('/tiqr/manual-response', {
        method: 'POST',
        body: { sessionKey, response },
    });
