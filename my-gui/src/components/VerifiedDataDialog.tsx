import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from '@surfnet/curve-react';

import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { accountKey } from '../utils/verification';
import { InstitutionCard } from './InstitutionCard';
import './VerifiedDataDialog.scss';

type VerifiedDataDialogProps = {
    accounts: LinkedAccount[];
    // only the card of this account starts opened
    openKey: string;
    onDelete: (account: LinkedAccount) => void;
    onClose: () => void;
};

// mounted only while open
export const VerifiedDataDialog = ({
    accounts,
    openKey,
    onDelete,
    onClose,
}: VerifiedDataDialogProps) => (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
        <DialogContent className="verified-data" initialFocus={false}>
            <DialogHeader>
                <DialogTitle className="verified-data__title">
                    {I18n.t('identity.verifiedData.title')}
                </DialogTitle>
            </DialogHeader>
            <div className="verified-data__list">
                {accounts.map((account) => (
                    <InstitutionCard
                        key={accountKey(account)}
                        account={account}
                        bordered
                        defaultOpen={accountKey(account) === openKey}
                        onDelete={() => onDelete(account)}
                    />
                ))}
            </div>
        </DialogContent>
    </Dialog>
);
