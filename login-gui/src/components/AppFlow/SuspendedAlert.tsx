import I18n from '../../locale/I18n';
import { ErrorAlert } from '../ErrorAlert/ErrorAlert';

type SuspendedAlertProps = {
    minutes: number | null;
};

export const SuspendedAlert = ({ minutes }: SuspendedAlertProps) => {
    if (minutes === null) {
        return null;
    }

    return (
        <ErrorAlert>
            {I18n.t('useApp.suspended')}{' '}
            {minutes > 0
                ? I18n.t('useApp.suspendedWait', {
                      minutes,
                      unit: I18n.t(
                          minutes === 1 ? 'useApp.minute' : 'useApp.minutes',
                      ),
                  })
                : I18n.t('useApp.suspendedRetry')}
        </ErrorAlert>
    );
};
