import { useState } from 'react';
import type { FormEvent } from 'react';

import { toast } from 'sonner';

import {
    Button,
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Field,
    FieldError,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { updateName } from '../api';
import type { User } from '../api';
import I18n from '../locale/I18n';
import './EditNameDialog.scss';

type EditChosenNameDialogProps = {
    user: User;
    onClose: () => void;
};

// mounted only while open, so the input always starts from the current value
export const EditChosenNameDialog = ({
    user,
    onClose,
}: EditChosenNameDialogProps) => {
    const queryClient = useQueryClient();
    const [chosenName, setChosenName] = useState(user.chosenName);

    const { mutate, isPending, isError } = useMutation({
        mutationFn: updateName,
        onSuccess: (updated) => {
            queryClient.setQueryData(['me'], updated);
            toast.success(I18n.t('toast.nameUpdated'));
            onClose();
        },
    });

    const trimmed = chosenName.trim();
    const canSave = trimmed !== '' && trimmed !== user.chosenName;

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (canSave) {
            mutate({ chosenName: trimmed });
        }
    };

    return (
        <Dialog open onOpenChange={(open) => !open && onClose()}>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>
                            {I18n.t('identity.editChosenName.title')}
                        </DialogTitle>
                    </DialogHeader>
                    <Field className="edit-name-dialog__fields">
                        <FieldLabel htmlFor="chosenName">
                            {I18n.t('identity.chosenName')}
                        </FieldLabel>
                        <Input
                            id="chosenName"
                            data-testid="edit-chosen-name-input"
                            value={chosenName}
                            autoComplete="nickname"
                            spellCheck={false}
                            onChange={(e) => setChosenName(e.target.value)}
                        />
                        {isError && (
                            <FieldError>{I18n.t('common.error')}</FieldError>
                        )}
                    </Field>
                    <DialogFooter>
                        <Button
                            type="button"
                            variant="outline"
                            onClick={onClose}
                        >
                            {I18n.t('identity.editName.cancel')}
                        </Button>
                        <Button
                            type="submit"
                            data-testid="edit-chosen-name-save"
                            disabled={!canSave || isPending}
                        >
                            {I18n.t('identity.editName.save')}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
