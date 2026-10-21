import { useState } from 'react';

import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    Field,
    FieldError,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { deleteAccount, fetchConfig, fetchMe } from '../api';
import I18n from '../locale/I18n';
import { hasSecondFactor } from '../utils/secondFactor';
import './DeleteAccountDialog.scss';
import { SecondFactorDialog } from './SecondFactorDialog';

type DeleteAccountDialogProps = {
    onClose: () => void;
};

// the keyword is typed without quotes or capitals, like in the old app
const sanitize = (input: string) => input.toLowerCase().replace(/["']/g, '');

// mounted only while open, so the confirmation always starts empty
export const DeleteAccountDialog = ({ onClose }: DeleteAccountDialogProps) => {
    const [confirmation, setConfirmation] = useState('');
    const [verifying, setVerifying] = useState(false);
    const { data: me } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const { mutate, isPending, isError } = useMutation({
        mutationFn: deleteAccount,
        // the server clears the session, the logout page shows the goodbye
        onSuccess: () =>
            window.location.assign(
                `${config?.accountBaseUrl ?? ''}/doLogout?param=${encodeURIComponent('delete=true')}`,
            ),
    });

    const confirmed =
        sanitize(confirmation.trim()) ===
        sanitize(I18n.t('account.delete.keyword'));

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
            <AlertDialogContent>
                <AlertDialogHeader>
                    <AlertDialogTitle>
                        {I18n.t('account.delete.title')}
                    </AlertDialogTitle>
                    <AlertDialogDescription>
                        {I18n.t('account.delete.warning')}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <Field className="delete-account__confirm">
                    <FieldLabel htmlFor="deleteConfirmation">
                        {I18n.t('account.delete.confirm', {
                            keyword: I18n.t('account.delete.keyword'),
                        })}
                    </FieldLabel>
                    <Input
                        id="deleteConfirmation"
                        value={confirmation}
                        autoComplete="off"
                        spellCheck={false}
                        onChange={(e) => setConfirmation(e.target.value)}
                    />
                    {isError && (
                        <FieldError>{I18n.t('common.error')}</FieldError>
                    )}
                </Field>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('account.delete.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        disabled={!confirmed || isPending}
                        onClick={() =>
                            me && hasSecondFactor(me)
                                ? setVerifying(true)
                                : mutate()
                        }
                    >
                        {I18n.t('account.delete.action')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
