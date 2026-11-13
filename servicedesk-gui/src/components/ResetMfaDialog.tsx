import { useState } from 'react';

import { ProhibitIcon } from '@phosphor-icons/react';
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
    Switch,
} from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import './BlockUserDialog.scss';
import './ResetMfaDialog.scss';

type ResetMfaDialogProps = {
    name: string;
    givenName?: string;
    familyName?: string;
    isPending: boolean;
    isError: boolean;
    onConfirm: () => void;
    onClose: () => void;
};

// mounted only while open
export const ResetMfaDialog = ({
    name,
    givenName,
    familyName,
    isPending,
    isError,
    onConfirm,
    onClose,
}: ResetMfaDialogProps) => {
    // the date of birth is not yet part of the check
    const checks = [
        I18n.t('resetMfa.familyName', { value: familyName ?? '' }),
        I18n.t('resetMfa.givenName', { value: givenName ?? '' }),
    ];
    const [checked, setChecked] = useState<boolean[]>(checks.map(() => false));
    const allChecked = checked.every(Boolean);

    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent className="block-user">
                <AlertDialogHeader className="block-user__header">
                    <AlertDialogMedia className="block-user__media">
                        <ProhibitIcon />
                    </AlertDialogMedia>
                    <div className="block-user__text">
                        <AlertDialogTitle>
                            {I18n.t('resetMfa.title')}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t('resetMfa.explanation', { name })}
                        </AlertDialogDescription>
                        <p className="reset-mfa__check">
                            {I18n.t('resetMfa.check', { name })}
                        </p>
                        <div className="reset-mfa__checks">
                            {checks.map((label, index) => (
                                <label key={label} className="reset-mfa__item">
                                    <Switch
                                        checked={checked[index]}
                                        onCheckedChange={(value) =>
                                            setChecked((current) =>
                                                current.map((item, i) =>
                                                    i === index ? value : item,
                                                ),
                                            )
                                        }
                                    />
                                    <span>{label}</span>
                                </label>
                            ))}
                        </div>
                        {isError && (
                            <FieldError>{I18n.t('resetMfa.error')}</FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('resetMfa.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        disabled={!allChecked || isPending}
                        onClick={onConfirm}
                    >
                        {I18n.t('resetMfa.confirm')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
