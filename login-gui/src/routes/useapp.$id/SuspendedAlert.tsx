import { WarningDiamondIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';

type SuspendedAlertProps = {
    minutes: number | null;
};

export const SuspendedAlert = ({ minutes }: SuspendedAlertProps) => {
    if (minutes === null) {
        return null;
    }

    return (
        <Alert variant="warning">
            <WarningDiamondIcon />
            <AlertDescription>
                {I18n.t('useApp.suspended')}{' '}
                {minutes > 0
                    ? I18n.t('useApp.suspendedWait', {
                          minutes,
                          unit: I18n.t(
                              minutes === 1
                                  ? 'useApp.minute'
                                  : 'useApp.minutes',
                          ),
                      })
                    : I18n.t('useApp.suspendedRetry')}
            </AlertDescription>
        </Alert>
    );
};
