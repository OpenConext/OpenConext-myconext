import { expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from './server';

const SMS_CODE_URL = `${SERVER_URL}/e2e/sms-code`;

const lastSmsCode = async (request: APIRequestContext) => {
    const response = await request.get(SMS_CODE_URL);
    return response.ok()
        ? ((await response.json()) as { code: string }).code
        : undefined;
};

// The mocked SMS service of the server (profile e2e) keeps the last code. Call
// this before the action that sends the SMS, the returned function then waits
// for a code that differs from the one before.
export const expectSmsCode = async (request: APIRequestContext) => {
    const before = await lastSmsCode(request);
    return async () => {
        let code = '';
        await expect
            .poll(async () => (code = (await lastSmsCode(request)) ?? ''), {
                message: 'no new sms code',
            })
            .not.toBe(before ?? '');
        return code;
    };
};
