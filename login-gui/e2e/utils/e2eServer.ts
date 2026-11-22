import type { APIRequestContext } from '@playwright/test';

import { SERVER_URL } from '../constants';

export const resetSeededUsers = async (
    request: APIRequestContext,
): Promise<void> => {
    const response = await request.get(`${SERVER_URL}/e2e/reset`);
    if (!response.ok()) {
        throw new Error(`e2e/reset failed with ${response.status()}`);
    }
};

/**
 * Restores one seeded user, for a spec that changes its user and must survive a retry or a rerun
 * within the same `playwright test` run. Other users are left alone, so parallel specs don't clash.
 */
export const resetSeededUser = async (
    request: APIRequestContext,
    email: string,
): Promise<void> => {
    const response = await request.get(`${SERVER_URL}/e2e/reset`, {
        params: { email },
    });
    if (!response.ok()) {
        throw new Error(`e2e/reset for ${email} failed with ${response.status()}`);
    }
};

/**
 * Deletes a user created by a test through the e2e profile controller of the server.
 * Returns false when the user does not exist, so it is safe to call it as a cleanup.
 */
export const deleteUserByEmail = async (
    request: APIRequestContext,
    email: string,
): Promise<boolean> => {
    const response = await request.get(`${SERVER_URL}/e2e/delete-user`, {
        params: { email },
    });
    if (response.status() === 404) {
        return false;
    }
    if (!response.ok()) {
        throw new Error(`e2e/delete-user failed with ${response.status()}`);
    }
    return true;
};
