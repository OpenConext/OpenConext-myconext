import { toast } from 'sonner';

import { BuildingOfficeIcon } from '@phosphor-icons/react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { removeLinkedAccount } from '../api';
import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { ConfirmDeleteDialog } from './ConfirmDeleteDialog';

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
    const { mutate, isPending, isError } = useMutation({
        mutationFn: () => removeLinkedAccount(account),
        onSuccess: (updated) => {
            queryClient.setQueryData(['me'], updated);
            toast.success(I18n.t('toast.institutionRemoved'));
            onClose();
        },
    });

    return (
        <ConfirmDeleteDialog
            icon={<BuildingOfficeIcon />}
            title={I18n.t('institutions.delete.title')}
            question={I18n.t('institutions.delete.question')}
            info={I18n.t('institutions.delete.info')}
            cancelLabel={I18n.t('institutions.delete.cancel')}
            actionLabel={I18n.t('institutions.delete.action')}
            actionTestId="delete-institution-confirm"
            isPending={isPending}
            isError={isError}
            onConfirm={mutate}
            onClose={onClose}
        />
    );
};
