import { expect } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

const MAILPIT_URL = process.env.MAILPIT_URL ?? 'http://localhost:8025';
// MP_UI_AUTH in docker-compose.yaml protects the API as well
const MAILPIT_AUTH = process.env.MAILPIT_AUTH ?? 'user:password';

const headers = {
    Authorization: `Basic ${Buffer.from(MAILPIT_AUTH).toString('base64')}`,
};

type MessageSummary = { ID: string; Subject: string; Created: string };

// the server refuses a code within a second after it was created
const CODE_DELAY = 1100;

// Waits for a mail to the address and returns the first six digit code in it,
// the one-time codes of the server mail templates. Resolves when the server
// accepts the code. `since` ignores older mails to the same address.
export const waitForCode = async (
    request: APIRequestContext,
    to: string,
    { subject, since }: { subject?: RegExp; since?: number } = {},
): Promise<string> => {
    let code = '';
    await expect
        .poll(
            async () => {
                const search = await request.get(
                    `${MAILPIT_URL}/api/v1/search`,
                    { headers, params: { query: `to:"${to}"` } },
                );
                const { messages = [] } = (await search.json()) as {
                    messages?: MessageSummary[];
                };
                const message = messages.find(
                    (m) =>
                        (!subject || subject.test(m.Subject)) &&
                        (!since || Date.parse(m.Created) >= since),
                );
                if (!message) {
                    return '';
                }
                const detail = await request.get(
                    `${MAILPIT_URL}/api/v1/message/${message.ID}`,
                    { headers },
                );
                const { Text } = (await detail.json()) as { Text: string };
                code = Text.match(/\b\d{6}\b/)?.[0] ?? '';
                if (code) {
                    await new Promise((resolve) =>
                        setTimeout(
                            resolve,
                            Date.parse(message.Created) +
                                CODE_DELAY -
                                Date.now(),
                        ),
                    );
                }
                return code;
            },
            { message: `no code mail for ${to}`, timeout: 15_000 },
        )
        .not.toBe('');
    return code;
};
