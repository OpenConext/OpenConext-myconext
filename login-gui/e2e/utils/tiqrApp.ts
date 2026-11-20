import {
    completeAuthentication,
    completeEnrollment,
    waitForCode,
} from '@myconext/shared-gui/e2e';
import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';
import { createAuthenticationRequestId } from './authnRequest';

/**
 * Gives an existing user a working eduID app (device and backup code as recovery) without a browser: logs in with the
 * one-time code and lets the tiqr-mock play the app. Needed to test logins that require the app.
 */
export const enrollAppForUser = async (
    request: APIRequestContext,
    email: string,
): Promise<void> => {
    const authenticationRequestId =
        await createAuthenticationRequestId(request);

    const codeRequestedAt = Date.now();
    const codeResponse = await request.put(
        `${SERVER_URL}/myconext/api/idp/generate_code_request`,
        { data: { user: { email }, authenticationRequestId } },
    );
    if (!codeResponse.ok()) {
        throw new Error(
            `generate_code_request failed with ${codeResponse.status()}`,
        );
    }

    const code = await waitForCode(request, email, { since: codeRequestedAt });
    const verifyResponse = await request.put(
        `${SERVER_URL}/myconext/api/idp/verify_code_request`,
        { data: { code, authenticationRequestId } },
    );
    if (!verifyResponse.ok()) {
        throw new Error(
            `verify_code_request failed with ${verifyResponse.status()}`,
        );
    }
    const { url } = (await verifyResponse.json()) as { url: string };
    const hash = new URL(url).searchParams.get('h');
    if (!hash) {
        throw new Error(`No hash in magic link ${url}`);
    }

    const startResponse = await request.get(
        `${SERVER_URL}/tiqr/start-enrollment`,
        { params: { hash } },
    );
    if (!startResponse.ok()) {
        throw new Error(
            `tiqr/start-enrollment failed with ${startResponse.status()}`,
        );
    }
    const { enrollmentKey } = (await startResponse.json()) as {
        enrollmentKey: string;
    };
    await completeEnrollment(request, enrollmentKey);

    // Only a recovery method finishes the registration, until then the user can't log in with the app
    const backupResponse = await request.get(
        `${SERVER_URL}/tiqr/generate-backup-code`,
        { params: { hash } },
    );
    if (!backupResponse.ok()) {
        throw new Error(
            `tiqr/generate-backup-code failed with ${backupResponse.status()}`,
        );
    }
};

/**
 * Lets the tiqr-mock answer the login QR code (`url` of `/tiqr/start-authentication`). The session key is passed
 * explicitly, otherwise parallel tests would answer each other's login. The page polls the server and continues by
 * itself.
 */
export const authenticateWithApp = async (
    request: APIRequestContext,
    authenticationUrl: string,
): Promise<void> => {
    const sessionKey = new URL(authenticationUrl).searchParams.get('s');
    if (!sessionKey) {
        throw new Error(`No session in ${authenticationUrl}`);
    }
    await completeAuthentication(request, { sessionKey });
};
