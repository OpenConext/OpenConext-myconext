import { useState } from 'react';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
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
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { fetchMe, removeLinkedAccount } from '../api';
import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { hasSecondFactor } from '../utils/secondFactor';
import './DeleteInstitutionDialog.scss';
import { SecondFactorDialog } from './SecondFactorDialog';

type DeleteInstitutionDialogProps = {
    account: LinkedAccount;
    onClose: () => void;
};

// mounted only while open
export const DeleteInstitutionDialog = ({
    account,
    onClose,
}: DeleteInstitutionDialogProps) => {
    const queryClient = useQueryClient();
    const [verifying, setVerifying] = useState(false);
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { mutate, isPending, isError } = useMutation({
        mutationFn: () => removeLinkedAccount(account),
        onSuccess: (updated) => {
            queryClient.setQueryData(['me'], updated);
            onClose();
        },
    });

    // the eduID app confirms first when the user has it
    if (verifying) {
        return (
            <SecondFactorDialog
                onClose={onClose}
                onConfirmed={() => {
                    setVerifying(false);
                    mutate();
                }}
            />
        );
    }

    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent className="delete-institution">
                <AlertDialogHeader className="delete-institution__header">
                    <AlertDialogMedia className="delete-institution__media">
                        <BuildingOfficeIcon />
                    </AlertDialogMedia>
                    <div className="delete-institution__text">
                        <AlertDialogTitle>
                            {I18n.t('institutions.delete.title')}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t('institutions.delete.question')}
                        </AlertDialogDescription>
                        <p>{I18n.t('institutions.delete.info')}</p>
                        {isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('institutions.delete.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        disabled={isPending}
                        onClick={() =>
                            me && hasSecondFactor(me)
                                ? setVerifying(true)
                                : mutate()
                        }
                    >
                        {I18n.t('institutions.delete.action')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
