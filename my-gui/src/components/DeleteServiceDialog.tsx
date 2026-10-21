import { useState } from 'react';

import { LinkBreakIcon } from '@phosphor-icons/react';
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

import { deleteServiceAndTokens, deleteTokens, fetchMe } from '../api';
import I18n from '../locale/I18n';
import { hasSecondFactor } from '../utils/secondFactor';
import type { Service } from '../utils/services';
import './DeleteServiceDialog.scss';
import { SecondFactorDialog } from './SecondFactorDialog';

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
    const [verifying, setVerifying] = useState(false);
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const key = kind === 'delete' ? 'deleteDialog' : 'revokeDialog';
    const { mutate, isPending, isError } = useMutation({
        mutationFn: () =>
            kind === 'delete'
                ? deleteServiceAndTokens(service.entityId, service.tokens)
                : deleteTokens(service.tokens),
        onSuccess: async (updated) => {
            queryClient.setQueryData(['me'], updated);
            await queryClient.invalidateQueries({ queryKey: ['tokens'] });
            onClose();
        },
    });

    // the eduID app confirms first when the user has it, revoking access is
    // not asked for
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
            <AlertDialogContent className="delete-service">
                <AlertDialogHeader className="delete-service__header">
                    <AlertDialogMedia className="delete-service__media">
                        <LinkBreakIcon />
                    </AlertDialogMedia>
                    <div className="delete-service__text">
                        <AlertDialogTitle>
                            {I18n.t(`activity.services.${key}.title`)}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t(`activity.services.${key}.question`, {
                                name: service.name,
                            })}
                        </AlertDialogDescription>
                        <p>{I18n.t(`activity.services.${key}.info`)}</p>
                        {isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('activity.services.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        disabled={isPending}
                        onClick={() =>
                            kind === 'delete' && me && hasSecondFactor(me)
                                ? setVerifying(true)
                                : mutate()
                        }
                    >
                        {I18n.t(`activity.services.${key}.action`)}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
