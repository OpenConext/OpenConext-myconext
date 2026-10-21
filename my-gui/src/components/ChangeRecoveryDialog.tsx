import { useState } from 'react';

import { RecoveryDialog } from './RecoveryDialog';
import { SecondFactorDialog } from './SecondFactorDialog';

type ChangeRecoveryDialogProps = {
    onClose: () => void;
};

// the server only accepts a new recovery method right after the app confirmed
// the user, so the confirmation comes first
export const ChangeRecoveryDialog = ({
    onClose,
}: ChangeRecoveryDialogProps) => {
    const [confirmed, setConfirmed] = useState(false);

    return confirmed ? (
        <RecoveryDialog change onClose={onClose} />
    ) : (
        <SecondFactorDialog
            onClose={onClose}
            onConfirmed={() => setConfirmed(true)}
        />
    );
};
