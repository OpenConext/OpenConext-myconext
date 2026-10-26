import { toast } from 'sonner';

import { LinkBreakIcon } from '@phosphor-icons/react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { deleteServiceAndTokens, deleteTokens } from '../api';
import I18n from '../locale/I18n';
import { setUser } from '../store/me';
import type { Service } from '../utils/services';
import { ConfirmDeleteDialog } from './ConfirmDeleteDialog';

type DeleteServiceDialogProps = {
    service: Service;
    // delete the login details, or only revoke the access
    kind: 'delete' | 'revoke';
    onClose: () => void;
};

// mounted only while open
export const DeleteServiceDialog = ({
    service,
    kind,
    onClose,
}: DeleteServiceDialogProps) => {
    const queryClient = useQueryClient();
    const key = kind === 'delete' ? 'deleteDialog' : 'revokeDialog';
    const { mutate, isPending, isError } = useMutation({
        mutationFn: () =>
            kind === 'delete'
                ? deleteServiceAndTokens(service.entityId, service.tokens)
                : deleteTokens(service.tokens),
        onSuccess: async (updated) => {
            setUser(updated);
            await queryClient.invalidateQueries({ queryKey: ['tokens'] });
            toast.success(
                I18n.t(
                    kind === 'delete'
                        ? 'toast.serviceDeleted'
                        : 'toast.accessRevoked',
                    { name: service.name },
                ),
            );
            onClose();
        },
    });

    // revoking access is not confirmed by the eduID app
    return (
        <ConfirmDeleteDialog
            icon={<LinkBreakIcon />}
            title={I18n.t(`activity.services.${key}.title`)}
            question={I18n.t(`activity.services.${key}.question`, {
                name: service.name,
            })}
            info={I18n.t(`activity.services.${key}.info`)}
            cancelLabel={I18n.t('activity.services.cancel')}
            actionLabel={I18n.t(`activity.services.${key}.action`)}
            isPending={isPending}
            isError={isError}
            askSecondFactor={kind === 'delete'}
            onConfirm={mutate}
            onClose={onClose}
        />
    );
};
