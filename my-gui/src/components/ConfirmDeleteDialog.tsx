import { useState } from 'react';
import type { ReactNode } from 'react';

import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogMedia,
    AlertDialogTitle,
    FieldError,
} from '@surfnet/curve-react';
import { useQuery } from '@tanstack/react-query';

import { fetchMe } from '../api';
import I18n from '../locale/I18n';
import { hasSecondFactor } from '../utils/secondFactor';
import './ConfirmDeleteDialog.scss';
import { SecondFactorDialog } from './SecondFactorDialog';

type ConfirmDeleteDialogProps = {
    icon: ReactNode;
    title: string;
    question: string;
    info: string;
    cancelLabel: string;
    actionLabel: string;
    actionTestId?: string;
    isPending: boolean;
    isError: boolean;
    // the eduID app confirms first when the user has it
    askSecondFactor?: boolean;
    onConfirm: () => void;
    onClose: () => void;
};

// mounted only while open
export const ConfirmDeleteDialog = ({
    icon,
    title,
    question,
    info,
    cancelLabel,
    actionLabel,
    actionTestId,
    isPending,
    isError,
    askSecondFactor = true,
    onConfirm,
    onClose,
}: ConfirmDeleteDialogProps) => {
    const [verifying, setVerifying] = useState(false);
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });

    if (verifying) {
        return (
            <SecondFactorDialog
                onClose={onClose}
                onConfirmed={() => {
                    setVerifying(false);
                    onConfirm();
                }}
            />
        );
    }

    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent className="confirm-delete">
                <AlertDialogHeader className="confirm-delete__header">
                    <AlertDialogMedia className="confirm-delete__media">
                        {icon}
                    </AlertDialogMedia>
                    <div className="confirm-delete__text">
                        <AlertDialogTitle>{title}</AlertDialogTitle>
                        <AlertDialogDescription>
                            {question}
                        </AlertDialogDescription>
                        <p>{info}</p>
                        {isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>{cancelLabel}</AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        data-testid={actionTestId}
                        disabled={isPending}
                        onClick={() =>
                            askSecondFactor && me && hasSecondFactor(me)
                                ? setVerifying(true)
                                : onConfirm()
                        }
                    >
                        {actionLabel}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
