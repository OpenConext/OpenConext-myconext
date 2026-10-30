import { useEffect, useRef, useState } from 'react';

import Cookies from 'js-cookie';

import { useMutation, useQuery } from '@tanstack/react-query';

import {
    type TiqrPollResponse,
    type TiqrStartResponse,
    tiqrPollAuthentication,
    tiqrStartAuthentication,
} from '../api';
import { LOGIN_PREFERENCE_COOKIE } from '../utils/loginMethods';

export type TiqrFlow =
    | { phase: 'loading' }
    | { phase: 'startError' }
    | { phase: 'timedOut' }
    | {
          phase: 'active';
          start: TiqrStartResponse;
          suspendedMinutes: number | null;
      };

const POLL_INTERVAL_MS = 1000;
const SESSION_TIMEOUT_MS = 15 * 60 * 1000;
const LOGIN_PREFERENCE_APP = 'useApp';

// The server returns epoch milliseconds, or epoch seconds in some code paths
// (TiqrController); the latter lands in the past and yields 0 ("try again").
const toSuspendedMinutes = ({
    status,
    suspendedUntil,
}: TiqrPollResponse): number | null => {
    if (status !== 'SUSPENDED') {
        return null;
    }

    const minutes = Math.round(
        ((suspendedUntil ?? 0) - Date.now()) / 1000 / 60,
    );

    return minutes < 1 ? 0 : minutes;
};

type PollResult = TiqrPollResponse & { suspendedMinutes: number | null };

const toPollResult = (poll: TiqrPollResponse): PollResult => ({
    ...poll,
    suspendedMinutes: toSuspendedMinutes(poll),
});

const completeLogin = (redirect: string, hash: string): void => {
    Cookies.set(LOGIN_PREFERENCE_COOKIE, LOGIN_PREFERENCE_APP, {
        expires: 365,
        secure: true,
        sameSite: 'Lax',
    });
    window.location.href = `${redirect}?h=${encodeURIComponent(hash)}`;
};

export const useTiqrAuthentication = (email: string, id: string): TiqrFlow => {
    // start-authentication creates a new tiqr session, so it must fire once
    const hasFired = useRef(false);
    const [timedOut, setTimedOut] = useState(false);

    const {
        mutate: startAuthentication,
        data: start,
        isError: startFailed,
    } = useMutation({
        mutationFn: () => tiqrStartAuthentication(email, id),
    });

    useEffect(() => {
        if (hasFired.current) {
            return;
        }

        hasFired.current = true;
        startAuthentication();
    }, [startAuthentication]);

    const sessionKey = start?.sessionKey ?? '';

    useEffect(() => {
        if (!sessionKey) {
            return;
        }

        const timer = setTimeout(() => setTimedOut(true), SESSION_TIMEOUT_MS);

        return () => clearTimeout(timer);
    }, [sessionKey]);

    const { data: poll, isError: pollFailed } = useQuery({
        queryKey: ['tiqrPoll', sessionKey],
        queryFn: () => tiqrPollAuthentication(sessionKey, id),
        select: toPollResult,
        // Without this, identical polls keep the same reference and `select`
        // would not re-run, freezing the suspension countdown
        structuralSharing: false,
        enabled: sessionKey !== '' && !timedOut,
        gcTime: 0,
        // The app-on-this-device flow backgrounds this tab
        refetchIntervalInBackground: true,
        refetchInterval: (query) =>
            query.state.data?.status === 'SUCCESS' ? false : POLL_INTERVAL_MS,
    });

    // An effect instead of the queryFn, so an in-flight poll that resolves
    // after the user navigated away cannot redirect anymore
    useEffect(() => {
        if (poll?.status !== 'SUCCESS' || !poll.redirect || !poll.hash) {
            return;
        }

        completeLogin(poll.redirect, poll.hash);
    }, [poll]);

    // A failing poll ends the session; the legacy page stopped polling silently
    if (timedOut || pollFailed) {
        return { phase: 'timedOut' };
    }

    if (startFailed) {
        return { phase: 'startError' };
    }

    if (!start) {
        return { phase: 'loading' };
    }

    return {
        phase: 'active',
        start,
        suspendedMinutes: poll?.suspendedMinutes ?? null,
    };
};
