import { randomBytes } from 'node:crypto';

import { waitForCode } from '@myconext/shared-gui/e2e';
import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';
import { createAuthenticationRequestId } from './authnRequest';
import { generateOcra } from './ocra';

interface TiqrMetaData {
    service: { enrollmentUrl: string };
    identity: { identifier: string };
}

export interface AppDevice {
    identifier: string;
    secret: string;
}

/**
 * Plays the eduID app: retrieves the metadata of an enrollment and registers a device with it, the same
 * calls the app makes after scanning the QR code. The enrollment is PROCESSED afterwards.
 */
export const enrollAppDevice = async (
    request: APIRequestContext,
    enrollmentKey: string,
): Promise<AppDevice> => {
    const metaDataResponse = await request.get(`${SERVER_URL}/tiqr/metadata`, {
        params: { enrollment_key: enrollmentKey },
    });
    if (!metaDataResponse.ok()) {
        throw new Error(
            `tiqr/metadata failed with ${metaDataResponse.status()}`,
        );
    }
    const metaData = (await metaDataResponse.json()) as TiqrMetaData;

    const secret = randomBytes(32).toString('hex');
    const enrollmentResponse = await request.post(
        metaData.service.enrollmentUrl,
        {
            form: {
                userid: metaData.identity.identifier,
                secret,
                language: 'en',
                notificationType: 'APNS',
                notificationAddress: '1234567890',
                version: '2',
                operation: 'register',
            },
        },
    );
    // The server answers 200 with the body ERROR when the registration fails
    if (
        !enrollmentResponse.ok() ||
        (await enrollmentResponse.text()) !== 'OK'
    ) {
        throw new Error('tiqr/enrollment was not accepted by the server');
    }
    return { identifier: metaData.identity.identifier, secret };
};

/**
 * Gives an existing user a working eduID app (device and backup code as recovery) without a browser: logs in with the
 * one-time code and plays the app. Needed to test logins that require the app, the device is the only place where the
 * secret is known.
 */
export const enrollAppForUser = async (
    request: APIRequestContext,
    email: string,
): Promise<AppDevice> => {
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
    const device = await enrollAppDevice(request, enrollmentKey);

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
    return device;
};

/**
 * Plays the eduID app scanning the QR code of a login: answers the challenge in the QR code url (`url` of
 * `/tiqr/start-authentication`). The page polls the server and continues by itself.
 */
export const authenticateWithApp = async (
    request: APIRequestContext,
    device: AppDevice,
    authenticationUrl: string,
): Promise<void> => {
    const params = new URL(authenticationUrl).searchParams;
    const sessionKey = params.get('s');
    const challenge = params.get('q');
    if (!sessionKey || !challenge) {
        throw new Error(`No session or challenge in ${authenticationUrl}`);
    }

    const response = await request.post(`${SERVER_URL}/tiqr/authentication`, {
        form: {
            sessionKey,
            userId: device.identifier,
            response: generateOcra(device.secret, challenge, sessionKey),
            language: 'en',
            operation: 'login',
            notificationType: 'APNS',
            notificationAddress: '1234567890',
        },
    });
    // The server answers 200 with the body ERROR when the response is wrong
    if (!response.ok() || (await response.text()) !== 'OK') {
        throw new Error('tiqr/authentication was not accepted by the server');
    }
};
