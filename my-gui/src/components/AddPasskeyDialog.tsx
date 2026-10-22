import { useState } from 'react';
import type { FormEvent } from 'react';

import {
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
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchConfig, startWebAuthnFlow } from '../api';
import I18n from '../locale/I18n';
import './PasskeyDialog.scss';

type AddPasskeyDialogProps = {
    onClose: () => void;
};

// mounted only while open
export const AddPasskeyDialog = ({ onClose }: AddPasskeyDialogProps) => {
    const [name, setName] = useState('');
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });

    const { mutate, isPending, isSuccess, isError } = useMutation({
        mutationFn: startWebAuthnFlow,
        // the browser leaves for the passkey registration, which redirects back
        onSuccess: ({ token }) =>
            window.location.assign(
                `${config?.accountWebAuthUrl}?token=${token}&name=${encodeURIComponent(name.trim())}`,
            ),
    });

    const valid = name.trim() !== '';
    const busy = isPending || isSuccess;

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (valid && !busy) {
            mutate();
        }
    };

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>
                            {I18n.t('security.passkey.flow.addTitle')}
                        </DialogTitle>
                        <DialogDescription>
                            {I18n.t('security.passkey.flow.addInfo')}
                        </DialogDescription>
                    </DialogHeader>
                    <FieldGroup className="passkey-dialog__fields">
                        <Field>
                            <FieldLabel htmlFor="passkeyName">
                                {I18n.t('security.passkey.flow.name')}
                            </FieldLabel>
                            <Input
                                id="passkeyName"
                                data-testid="add-passkey-name"
                                autoFocus
                                spellCheck={false}
                                placeholder={I18n.t(
                                    'security.passkey.flow.namePlaceholder',
                                )}
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                        </Field>
                        {isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </FieldGroup>
                    <DialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={onClose}
                        >
                            {I18n.t('security.passkey.flow.cancel')}
                        </Button>
                        <Button
                            type="submit"
                            data-testid="add-passkey-start"
                            disabled={!valid || busy}
                        >
                            {busy && <Spinner />}
                            {I18n.t('security.passkey.flow.start')}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
