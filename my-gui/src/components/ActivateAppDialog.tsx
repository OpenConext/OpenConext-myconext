import { useState } from 'react';

import {
    InstallAppQr,
    isMobile,
    isTouchDevice,
    useEnrollmentPolling,
} from '@myconext/shared-gui';
import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { pollEnrollment, startEnrollment } from '../api';
import type { StartEnrollmentResponse } from '../api';
import I18n from '../locale/I18n';
import { refreshUser } from '../store/me';
import './ActivateAppDialog.scss';
import { RecoveryDialog } from './RecoveryDialog';
import { StoreBadges } from './StoreBadges';

type ActivateAppDialogProps = {
    onClose: () => void;
};

// mounted only while open, so every flow starts at the download step
export const ActivateAppDialog = ({ onClose }: ActivateAppDialogProps) => {
    const [resume, setResume] = useState(false);

    const {
        mutate: start,
        data: enrollment,
        isPending,
        isError,
        error,
    } = useMutation({
        mutationFn: startEnrollment,
    });

    const { status, timedOut } = useEnrollmentPolling({
        enrollmentKey: enrollment?.enrollmentKey,
        poll: pollEnrollment,
        doneStatus: 'PROCESSED',
    });

    const processed = status === 'PROCESSED';

    // the security page shows the app once the enrollment is processed
    const handleClose = () => {
        if (enrollment) {
            void refreshUser();
        }
        onClose();
    };

    // the server refuses a second registration with a 403
    const existingRegistration =
        isError && error instanceof Response && error.status === 403;

    // the recovery method is the last step, shown in its own dialog
    if (processed || resume) {
        return <RecoveryDialog onClose={handleClose} />;
    }

    return (
        <Dialog open onOpenChange={(open) => !open && handleClose()}>
            <DialogContent>
                {!enrollment && !isError && (
                    <>
                        <DialogHeader>
                            <DialogTitle>{I18n.t('app.get.title')}</DialogTitle>
                            <DialogDescription>
                                {I18n.t('app.get.info')}
                            </DialogDescription>
                        </DialogHeader>
                        <div className="activate-app__stores">
                            {!isMobile() && (
                                <div className="activate-app__qr">
                                    <InstallAppQr
                                        title={I18n.t('app.get.qr')}
                                    />
                                </div>
                            )}
                            <StoreBadges />
                        </div>
                        <p className="activate-app__after">
                            {I18n.t('app.get.after')}
                        </p>
                        <DialogFooter>
                            <Button variant="outline" onClick={onClose}>
                                {I18n.t('app.get.cancel')}
                            </Button>
                            <Button
                                data-testid="activate-app-next"
                                disabled={isPending}
                                onClick={() => start()}
                            >
                                {isPending && <Spinner />}
                                {I18n.t('app.get.next')}
                            </Button>
                        </DialogFooter>
                    </>
                )}

                {isError && (
                    <>
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t(
                                    existingRegistration
                                        ? 'app.enroll.existingTitle'
                                        : 'common.error',
                                )}
                            </DialogTitle>
                            {existingRegistration && (
                                <DialogDescription>
                                    {I18n.t('app.enroll.existingInfo')}
                                </DialogDescription>
                            )}
                        </DialogHeader>
                        <DialogFooter>
                            <Button variant="outline" onClick={handleClose}>
                                {I18n.t('app.enroll.close')}
                            </Button>
                            {existingRegistration && (
                                <Button onClick={() => setResume(true)}>
                                    {I18n.t('app.enroll.resume')}
                                </Button>
                            )}
                        </DialogFooter>
                    </>
                )}

                {enrollment && (
                    <EnrollmentStep
                        enrollment={enrollment}
                        status={status}
                        timedOut={timedOut}
                        onClose={handleClose}
                    />
                )}
            </DialogContent>
        </Dialog>
    );
};

type EnrollmentStepProps = {
    enrollment: StartEnrollmentResponse;
    status?: string;
    timedOut: boolean;
    onClose: () => void;
};

const EnrollmentStep = ({
    enrollment,
    status,
    timedOut,
    onClose,
}: EnrollmentStepProps) => {
    const retrieved = status === 'RETRIEVED';

    const title = timedOut
        ? 'app.enroll.timeoutTitle'
        : retrieved
          ? 'app.enroll.finishTitle'
          : 'app.enroll.scanTitle';
    const info = timedOut
        ? 'app.enroll.timeoutInfo'
        : retrieved
          ? 'app.enroll.finishInfo'
          : 'app.enroll.scanInfo';

    return (
        <>
            <DialogHeader>
                <DialogTitle>{I18n.t(title)}</DialogTitle>
                <DialogDescription>{I18n.t(info)}</DialogDescription>
            </DialogHeader>
            {!timedOut && !retrieved && (
                <div className="activate-app__enroll">
                    <img
                        data-testid="activate-app-enroll-qr"
                        className="activate-app__enroll-qr"
                        src={enrollment.qrcode}
                        alt={I18n.t('app.enroll.qr')}
                    />
                    {isTouchDevice() && (
                        <Button
                            render={<a href={enrollment.url} />}
                            variant="outline"
                        >
                            {I18n.t('app.enroll.open')}
                        </Button>
                    )}
                </div>
            )}
            {retrieved && (
                <div className="activate-app__enroll">
                    <Spinner />
                </div>
            )}
            <DialogFooter>
                <Button variant="outline" onClick={onClose}>
                    {I18n.t('app.enroll.close')}
                </Button>
            </DialogFooter>
        </>
    );
};
