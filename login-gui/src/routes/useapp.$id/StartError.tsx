import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';

export const StartError = () => (
    <Alert variant="warning">
        <WarningDiamondIcon />
        <AlertDescription>{I18n.t('useApp.startError')}</AlertDescription>
    </Alert>
);
