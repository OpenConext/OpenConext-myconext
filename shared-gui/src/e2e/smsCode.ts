import { expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

const lastSmsCode = async (
    request: APIRequestContext,
    serverUrl: string,
): Promise<string | undefined> => {
    const response = await request.get(`${serverUrl}/e2e/sms-code`);
    return response.ok()
        ? ((await response.json()) as { code: string }).code
        : undefined;
};

/**
 * The mocked SMS service of the server (profile e2e) only keeps the last code. Call this before the action
 * that sends the SMS, the returned function then waits for a code that differs from the one before.
 */
export const expectSmsCode = async (
    request: APIRequestContext,
    serverUrl: string,
): Promise<() => Promise<string>> => {
    const before = await lastSmsCode(request, serverUrl);
    return async () => {
        let code = '';
        await expect
            .poll(
                async () =>
                    (code = (await lastSmsCode(request, serverUrl)) ?? ''),
                { message: 'no new sms code' },
            )
            .not.toBe(before ?? '');
        return code;
    };
};
