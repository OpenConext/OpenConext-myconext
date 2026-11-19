import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';

/**
 * The service name the server shows for an authentication request, the `{{serviceName}}` in the copy.
 */
export const fetchServiceName = async (
    request: APIRequestContext,
    authenticationRequestId: string,
): Promise<string> => {
    const response = await request.get(
        `${SERVER_URL}/myconext/api/idp/service/name/${authenticationRequestId}`,
    );
    if (!response.ok()) {
        throw new Error(`service/name failed with ${response.status()}`);
    }
    return ((await response.json()) as { name: string }).name;
};
