import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';

// `remote-users` in application.yml, the studielink user has the remote-creation scope
const REMOTE_CREATION_AUTH =
    process.env.E2E_REMOTE_CREATION_AUTH ?? 'studielink:secret';

/**
 * Asks the remote-creation API whether an eduID exists for the email, `/sp/me` is no option because
 * the e2e profile always answers with the seeded user. Returns the eduID value, or null when the
 * user does not exist. Note that the endpoint computes and saves an eduID for the remote provider.
 */
export const findEduIdByEmail = async (
    request: APIRequestContext,
    email: string,
): Promise<string | null> => {
    const response = await request.get(
        `${SERVER_URL}/api/remote-creation/email-eduid-exists`,
        {
            params: { email },
            headers: {
                Authorization: `Basic ${Buffer.from(REMOTE_CREATION_AUTH).toString('base64')}`,
            },
        },
    );
    if (response.status() === 404) {
        return null;
    }
    if (!response.ok()) {
        throw new Error(`email-eduid-exists failed with ${response.status()}`);
    }
    return ((await response.json()) as { eduIDValue: string }).eduIDValue;
};
