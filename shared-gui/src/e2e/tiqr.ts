import { expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

export const TIQR_MOCK_URL =
    process.env.TIQR_MOCK_URL ?? 'http://localhost:8088';

// The tiqr-mock plays the eduID app: a GET on /enrollment/{key} retrieves the
// metadata (like scanning the qr code) and /enrolled/{key} completes the
// enrollment. The key is the enrollmentKey of the start-enrollment response.
export const completeEnrollment = async (
    request: APIRequestContext,
    enrollmentKey: string,
): Promise<void> => {
    const metadata = await request.get(
        `${TIQR_MOCK_URL}/enrollment/${enrollmentKey}`,
    );
    expect(metadata.ok()).toBeTruthy();
    const response = await request.get(
        `${TIQR_MOCK_URL}/enrolled/${enrollmentKey}`,
    );
    expect(response.ok()).toBeTruthy();
};

// Pending authentications are only listed as html, a link per sessionKey
const pendingSessionKeys = async (
    request: APIRequestContext,
): Promise<string[]> => {
    const response = await request.get(`${TIQR_MOCK_URL}/authentications`);
    const html = await response.text();
    return [...html.matchAll(/\/authentication\/([\w-]+)/g)].map((m) => m[1]);
};

/**
 * Answers a pending login of the app. Without `sessionKey` it takes the first pending authentication of the
 * mock, which is only safe when tests run one at a time. Tests in parallel must pass their own `sessionKey`
 * (the `s` parameter of the url in the `/tiqr/start-authentication` response).
 */
export const completeAuthentication = async (
    request: APIRequestContext,
    {
        fail = false,
        sessionKey,
    }: { fail?: boolean; sessionKey?: string } = {},
): Promise<void> => {
    let key = sessionKey ?? '';
    if (!key) {
        await expect
            .poll(async () => (key = (await pendingSessionKeys(request))[0]))
            .toBeTruthy();
    }
    const response = await request.get(
        `${TIQR_MOCK_URL}/authenticated/${key}${fail ? '?fail=true' : ''}`,
    );
    expect(response.ok()).toBeTruthy();
};

// Removes the app registration of the user, so that a test starts clean
export const deleteRegistration = async (
    request: APIRequestContext,
    userId: string,
): Promise<void> => {
    await request.get(`${TIQR_MOCK_URL}/delete-registration/${userId}`);
};
