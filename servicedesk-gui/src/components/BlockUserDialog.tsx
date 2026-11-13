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
} from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import './BlockUserDialog.scss';

type BlockUserDialogProps = {
    name: string;
    institution?: string;
    isPending: boolean;
    isError: boolean;
    onConfirm: () => void;
    onClose: () => void;
};

// mounted only while open
export const BlockUserDialog = ({
    name,
    institution,
    isPending,
    isError,
    onConfirm,
    onClose,
}: BlockUserDialogProps) => {
    const params = { name, institution: institution ?? '' };
    return (
        <AlertDialog open onOpenChange={(open) => !open && onClose()}>
            <AlertDialogContent className="block-user">
                <AlertDialogHeader className="block-user__header">
                    <AlertDialogMedia className="block-user__media">
                        <ProhibitIcon />
                    </AlertDialogMedia>
                    <div className="block-user__text">
                        <AlertDialogTitle>
                            {I18n.t('blockUser.title')}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {I18n.t('blockUser.question', params)}
                        </AlertDialogDescription>
                        <ul>
                            {[
                                'institution',
                                'active',
                                'indefinite',
                                'notice',
                            ].map((key) => (
                                <li key={key}>
                                    {I18n.t(`blockUser.info.${key}`, params)}
                                </li>
                            ))}
                        </ul>
                        {isError && (
                            <FieldError>{I18n.t('blockUser.error')}</FieldError>
                        )}
                    </div>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel>
                        {I18n.t('blockUser.cancel')}
                    </AlertDialogCancel>
                    <AlertDialogAction
                        variant="destructive"
                        disabled={isPending}
                        onClick={onConfirm}
                    >
                        {I18n.t('blockUser.confirm')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};
