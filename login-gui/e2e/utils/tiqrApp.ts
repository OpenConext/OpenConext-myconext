import { randomBytes } from 'node:crypto';

import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';

interface TiqrMetaData {
    service: { enrollmentUrl: string };
    identity: { identifier: string };
}

/**
 * Plays the eduID app: retrieves the metadata of an enrollment and registers a device with it, the same
 * calls the app makes after scanning the QR code. The enrollment is PROCESSED afterwards.
 */
export const enrollAppDevice = async (
    request: APIRequestContext,
    enrollmentKey: string,
): Promise<void> => {
    const metaDataResponse = await request.get(`${SERVER_URL}/tiqr/metadata`, {
        params: { enrollment_key: enrollmentKey },
    });
    if (!metaDataResponse.ok()) {
        throw new Error(
            `tiqr/metadata failed with ${metaDataResponse.status()}`,
        );
    }
    const metaData = (await metaDataResponse.json()) as TiqrMetaData;

    const enrollmentResponse = await request.post(
        metaData.service.enrollmentUrl,
        {
            form: {
                userid: metaData.identity.identifier,
                secret: randomBytes(32).toString('hex'),
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
};
