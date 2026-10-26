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
    FieldGroup,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';
import { useMutation } from '@tanstack/react-query';

import { updateName } from '../api';
import type { User } from '../api';
import I18n from '../locale/I18n';
import { setUser } from '../store/me';
import './EditNameDialog.scss';

type EditNameDialogProps = {
    user: User;
    open: boolean;
    onOpenChange: (open: boolean) => void;
};

export const EditNameDialog = ({
    user,
    open,
    onOpenChange,
}: EditNameDialogProps) => {
    const [givenName, setGivenName] = useState(user.givenName);
    const [familyName, setFamilyName] = useState(user.familyName);

    const { mutate, isPending, isError, reset } = useMutation({
        mutationFn: updateName,
        onSuccess: (updated) => {
            setUser(updated);
            toast.success(I18n.t('toast.nameUpdated'));
            onOpenChange(false);
        },
    });

    const trimmed = {
        chosenName: user.chosenName,
        givenName: givenName.trim(),
        familyName: familyName.trim(),
    };
    const changed =
        trimmed.givenName !== user.givenName ||
        trimmed.familyName !== user.familyName;
    const valid = trimmed.givenName !== '' && trimmed.familyName !== '';

    const handleOpenChange = (next: boolean) => {
        if (next) {
            setGivenName(user.givenName);
            setFamilyName(user.familyName);
            reset();
        }
        onOpenChange(next);
    };

    const handleSubmit = (event: FormEvent) => {
        event.preventDefault();
        if (valid && changed) {
            mutate(trimmed);
        }
    };

    return (
        <Dialog open={open} onOpenChange={handleOpenChange}>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>
                            {I18n.t('identity.editName.title')}
                        </DialogTitle>
                    </DialogHeader>
                    <FieldGroup className="edit-name-dialog__fields">
                        <Field>
                            <FieldLabel htmlFor="givenName">
                                {I18n.t('identity.editName.givenName')}
                            </FieldLabel>
                            <Input
                                id="givenName"
                                value={givenName}
                                autoComplete="given-name"
                                spellCheck={false}
                                onChange={(e) => setGivenName(e.target.value)}
                            />
                        </Field>
                        <Field>
                            <FieldLabel htmlFor="familyName">
                                {I18n.t('identity.editName.familyName')}
                            </FieldLabel>
                            <Input
                                id="familyName"
                                value={familyName}
                                autoComplete="family-name"
                                spellCheck={false}
                                onChange={(e) => setFamilyName(e.target.value)}
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
                            onClick={() => handleOpenChange(false)}
                        >
                            {I18n.t('identity.editName.cancel')}
                        </Button>
                        <Button
                            type="submit"
                            disabled={!valid || !changed || isPending}
                        >
                            {I18n.t('identity.editName.save')}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
};
