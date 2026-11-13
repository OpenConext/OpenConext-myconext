import { SealCheckIcon } from '@phosphor-icons/react';
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

import I18n from '../locale/I18n';
import './BlockUserDialog.scss';

type UnblockUserDialogProps = {
    name: string;
    institution?: string;
    isPending: boolean;
    isError: boolean;
    onConfirm: () => void;
    onClose: () => void;
};

// mounted only while open
export const UnblockUserDialog = ({
    name,
    institution,
    isPending,
    isError,
    onConfirm,
    onClose,
}: UnblockUserDialogProps) => {
    const params = { name, institution: institution ?? '' };
    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent className="block-user">
                <AlertDialogHeader className="block-user__header">
                    <AlertDialogMedia className="block-user__media block-user__media--positive">
                        <SealCheckIcon />
                    </AlertDialogMedia>
                    <div className="block-user__text">
                        <AlertDialogTitle>
                            {I18n.t('unblockUser.title')}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t('unblockUser.question', params)}
                        </AlertDialogDescription>
                        <ul>
                            {['institution', 'block', 'notice'].map((key) => (
                                <li key={key}>
                                    {I18n.t(`unblockUser.info.${key}`, params)}
                                </li>
                            ))}
                        </ul>
                        {isError && (
                            <FieldError>
                                {I18n.t('unblockUser.error')}
                            </FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('unblockUser.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction disabled={isPending} onClick={onConfirm}>
                        {I18n.t('unblockUser.confirm')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
