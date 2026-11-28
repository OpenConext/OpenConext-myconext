import { expect, test, vi } from 'vitest';

import { configuration, me } from '../../api';
import { AUTH_STATUS, useAppStore } from '../../stores/AppStore';

vi.mock('../../api', () => ({
    configuration: vi.fn(() =>
        Promise.resolve({ isAuthenticated: true, accountBaseUrl: 'base' }),
    ),
    me: vi.fn(() =>
        Promise.resolve({ id: '1', name: 'Jane', serviceDeskMember: true }),
    ),
}));

test('loadSession fetches /config and /me only once', async () => {
    const { loadSession } = useAppStore.getState();

    // Concurrent calls (e.g. StrictMode double effects) and later calls
    // (e.g. tab switches) all share the first request
    const [first, second] = await Promise.all([loadSession(), loadSession()]);
    const third = await loadSession();

    expect([first, second, third]).toEqual([
        AUTH_STATUS.AUTHORIZED,
        AUTH_STATUS.AUTHORIZED,
        AUTH_STATUS.AUTHORIZED,
    ]);
    expect(configuration).toHaveBeenCalledTimes(1);
    expect(me).toHaveBeenCalledTimes(1);

    const state = useAppStore.getState();
    expect(state.authStatus).toBe(AUTH_STATUS.AUTHORIZED);
    expect(state.config.accountBaseUrl).toBe('base');
    expect(state.user.name).toBe('Jane');
});
