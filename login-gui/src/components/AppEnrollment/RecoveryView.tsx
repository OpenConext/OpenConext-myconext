import { DeviceMobileIcon, PrinterIcon } from '@phosphor-icons/react';

import I18n from '../../locale/I18n';
import { ItemList } from '../ItemList/ItemList';
import { Spinner } from '../Loading/Spinner';
import { LoginContainer } from '../LoginContainer/LoginContainer';

interface RecoveryViewProps {
    onPhone: () => void;
    onBackupCode: () => void;
    isSubmitting?: boolean;
}

export const RecoveryView = ({
    onPhone,
    onBackupCode,
    isSubmitting = false,
}: RecoveryViewProps) => (
    <LoginContainer
        title={I18n.t('recovery.title')}
        description={I18n.t('recovery.description')}
        headerAlign="start"
    >
        {isSubmitting ? (
            <Spinner />
        ) : (
            <ItemList
                items={[
                    {
                        key: 'phone',
                        icon: DeviceMobileIcon,
                        label: I18n.t('recovery.phoneOption'),
                        onClick: onPhone,
                    },
                    {
                        key: 'backup-code',
                        icon: PrinterIcon,
                        label: I18n.t('recovery.backupOption'),
                        onClick: onBackupCode,
                    },
                ]}
            />
        )}
    </LoginContainer>
);
