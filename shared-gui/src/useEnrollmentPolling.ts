import { useEffect, useState } from 'react';

import { useQuery } from '@tanstack/react-query';

const POLL_INTERVAL = 1000;
// the server keeps an enrollment open for 15 minutes
const ENROLLMENT_TIMEOUT = 15 * 60 * 1000;

export type UseEnrollmentPollingOptions<Status extends string> = {
    // undefined until the enrollment has been started
    enrollmentKey: string | undefined;
    poll: (enrollmentKey: string) => Promise<Status>;
    // polling stops once the app has processed the enrollment
    doneStatus: Status;
};

// polls the status of an eduID app enrollment until it is done or timed out
export const useEnrollmentPolling = <Status extends string>({
    enrollmentKey,
    poll,
    doneStatus,
}: UseEnrollmentPollingOptions<Status>) => {
    // keyed so a new enrollment is never considered timed out
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
