import { useEffect, useRef } from 'react';
import { Navigate, useNavigate, useSearchParams } from 'react-router';

import { useEnrollmentPolling } from '@myconext/shared-gui';
import { useMutation } from '@tanstack/react-query';

import { pollEnrollment, startEnrollment } from '../../api';
import { type EnrollAppState, EnrollAppView, Loading } from '../../components';
import I18n from '../../locale/I18n';

export default function EnrollApp() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const hash = searchParams.get('h') ?? '';

    // start-enrollment creates a new enrollment, so it must fire once
    const hasFired = useRef(false);

    const {
        mutate: start,
        data: enrollment,
        isError,
        error,
    } = useMutation({
        mutationFn: () => startEnrollment(hash),
    });

    useEffect(() => {
        if (!hash || hasFired.current) {
            return;
        }

        hasFired.current = true;
        start();
    }, [hash, start]);

    const { status, timedOut } = useEnrollmentPolling({
        enrollmentKey: enrollment?.enrollmentKey,
        poll: pollEnrollment,
        doneStatus: 'PROCESSED',
    });

    const processed = status === 'PROCESSED';

    useEffect(() => {
        if (processed) {
            navigate(`/recovery?h=${encodeURIComponent(hash)}`, {
                replace: true,
            });
        }
    }, [processed, hash, navigate]);

    if (!hash) {
        return <Navigate to="/expired" replace />;
    }

    const toResume = () =>
        navigate(`/recovery?h=${encodeURIComponent(hash)}`, { replace: true });

    let state: EnrollAppState;
    if (isError) {
        // The server refuses a second registration with a 403
        state =
            error instanceof Response && error.status === 403
                ? { phase: 'existing' }
                : { phase: 'error' };
    } else if (timedOut) {
        state = { phase: 'timedOut' };
    } else if (!enrollment) {
        return <Loading title={I18n.t('enrollApp.scanTitle')} />;
    } else if (status === 'RETRIEVED' || processed) {
        state = { phase: 'finish' };
    } else {
        state = { phase: 'scan', start: enrollment };
    }

    return <EnrollAppView state={state} onResume={toResume} />;
}
