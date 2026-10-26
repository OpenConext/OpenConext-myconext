import { useState } from 'react';
import type { FormEvent } from 'react';

import { toast } from 'sonner';

import {
    Alert,
    AlertDescription,
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Field,
    FieldError,
    FieldGroup,
    FieldLabel,
    Input,
    Spinner,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import {
    deleteCredential,
    fetchTestWebAuthnUrl,
    updateCredential,
} from '../api';
import type { PublicKeyCredential, User } from '../api';
import I18n from '../locale/I18n';
import { setUser } from '../store/me';
import { formatDateTime } from '../utils/format';
import { signalPasskeyDeleted } from '../utils/passkey';
import './PasskeyDialog.scss';

type EditPasskeyDialogProps = {
    credential: PublicKeyCredential;
    // result of a passkey test the server redirected back from
    testResult?: 'success' | 'failed';
    onClose: () => void;
};

// mounted only while open
export const EditPasskeyDialog = ({
    credential,
    testResult,
    onClose,
}: EditPasskeyDialogProps) => {
    const [name, setName] = useState(credential.name);
    const [confirmingDelete, setConfirmingDelete] = useState(false);

    const onSuccess = (updated: User, message: string) => {
        setUser(updated);
        toast.success(I18n.t(message));
        onClose();
    };

    const update = useMutation({
        mutationFn: updateCredential,
        onSuccess: (updated) => onSuccess(updated, 'toast.passkeyUpdated'),
    });
    const remove = useMutation({
        mutationFn: async (deleted: PublicKeyCredential) => {
            const updated = await deleteCredential(deleted);
            return {
                updated,
                signalled: await signalPasskeyDeleted(deleted.identifier),
            };
        },
        onSuccess: ({ updated, signalled }) => {
            setUser(updated);
            toast.success(
                I18n.t('toast.passkeyDeleted'),
                signalled
                    ? undefined
                    : { description: I18n.t('toast.passkeyDeletedManual') },
            );
            onClose();
        },
    });
    const test = useMutation({
        mutationFn: fetchTestWebAuthnUrl,
        // the browser leaves for the passkey login, which redirects back
        onSuccess: ({ url }) => window.location.assign(url),
    });

    const valid = name.trim() !== '';
    const busy = update.isPending || remove.isPending || test.isPending;
    const failed = update.isError || remove.isError || test.isError;

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (valid && !busy) {
            update.mutate({ ...credential, name: name.trim() });
        }
    };

    if (confirmingDelete) {
        return (
            <Dialog open onOpenChange={(open) => !open && onClose()}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>
                            {I18n.t('security.passkey.flow.deleteTitle')}
                        </DialogTitle>
                        <DialogDescription>
                            {I18n.t('security.passkey.flow.deleteInfo', {
                                name: credential.name,
                            })}
                        </DialogDescription>
                    </DialogHeader>
                    {failed && (
                        <FieldError>{I18n.t('common.error')}</FieldError>
                    )}
                    <DialogFooter>
                        <Button
                            variant="outline"
                            onClick={() => setConfirmingDelete(false)}
                        >
                            {I18n.t('security.passkey.flow.back')}
                        </Button>
                        <Button
                            variant="destructive"
                            data-testid="edit-passkey-delete-confirm"
                            disabled={busy}
                            onClick={() => remove.mutate(credential)}
                        >
                            {I18n.t('security.passkey.flow.deleteAction')}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        );
    }

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>
                            {I18n.t('security.passkey.flow.editTitle')}
                        </DialogTitle>
                        <DialogDescription>
                            {I18n.t('security.passkey.flow.added', {
                                date: formatDateTime(credential.createdAt),
                            })}
                        </DialogDescription>
                    </DialogHeader>
                    {testResult && (
                        <Alert
                            variant={
                                testResult === 'success' ? 'success' : 'warning'
                            }
                        >
                            <AlertDescription>
                                {I18n.t(
                                    testResult === 'success'
                                        ? 'security.passkey.flow.testSuccess'
                                        : 'security.passkey.flow.testFailed',
                                )}
                            </AlertDescription>
                        </Alert>
                    )}
                    <FieldGroup className="passkey-dialog__fields">
                        <Field>
                            <FieldLabel htmlFor="credentialName">
                                {I18n.t('security.passkey.flow.name')}
                            </FieldLabel>
                            <Input
                                id="credentialName"
                                data-testid="edit-passkey-name"
                                spellCheck={false}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                        </Field>
                        {failed && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </FieldGroup>
                    <DialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            className="passkey-dialog__delete"
                            data-testid="edit-passkey-delete"
                            onClick={() => setConfirmingDelete(true)}
                        >
                            {I18n.t('security.passkey.flow.delete')}
                        </Button>
                        <Button
                            type="button"
                            variant="outline"
                            disabled={busy}
                            onClick={() => test.mutate()}
                        >
                            {test.isPending && <Spinner />}
                            {I18n.t('security.passkey.flow.test')}
                        </Button>
                        <Button
                            type="submit"
                            data-testid="edit-passkey-update"
                            disabled={!valid || busy}
                        >
                            {I18n.t('security.passkey.flow.update')}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
