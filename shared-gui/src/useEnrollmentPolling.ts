import { useEffect, useState } from 'react';

import { useQuery } from '@tanstack/react-query';

const POLL_INTERVAL = 1000;
const ENROLLMENT_TIMEOUT = 15 * 60 * 1000;

export type UseEnrollmentPollingOptions<Status extends string> = {
    // undefined until the enrollment has been started
    enrollmentKey: string | undefined;
    poll: (enrollmentKey: string) => Promise<Status>;
    doneStatus: Status;
};

// polls the status of an eduID app enrollment until it is done or timed out
export const useEnrollmentPolling = <Status extends string>({
    enrollmentKey,
    poll,
    doneStatus,
}: UseEnrollmentPollingOptions<Status>) => {
    const [timedOutKey, setTimedOutKey] = useState<string>();
    const timedOut =
        enrollmentKey !== undefined && timedOutKey === enrollmentKey;

    useEffect(() => {
        if (!enrollmentKey) {
            return;
        }
        const timer = setTimeout(
            () => setTimedOutKey(enrollmentKey),
            ENROLLMENT_TIMEOUT,
        );
        return () => clearTimeout(timer);
    }, [enrollmentKey]);

    const { data: status } = useQuery({
        queryKey: ['enrollment', enrollmentKey],
        queryFn: () => poll(enrollmentKey as string),
        enabled: enrollmentKey !== undefined && !timedOut,
        refetchInterval: (query) =>
            query.state.data === doneStatus ? false : POLL_INTERVAL,
        gcTime: 0,
    });

    return { status, timedOut };
};
