import { PrinterIcon } from '@phosphor-icons/react';

import I18n from '../../locale/I18n';
import { ItemList } from '../ItemList/ItemList';
import { LoginContainer } from '../LoginContainer/LoginContainer';

interface RecoveryViewProps {
    onBackupCode: () => void;
}

export const RecoveryView = ({ onBackupCode }: RecoveryViewProps) => (
    <LoginContainer
        title={I18n.t('recovery.title')}
        description={I18n.t('recovery.description')}
        headerAlign="start"
    >
        <ItemList
            items={[
                {
                    key: 'backup-code',
                    icon: PrinterIcon,
                    label: I18n.t('recovery.backupOption'),
                    onClick: onBackupCode,
                },
            ]}
        />
    </LoginContainer>
);
