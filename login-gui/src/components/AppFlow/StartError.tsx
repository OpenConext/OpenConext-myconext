import I18n from '../../locale/I18n';
import { ErrorAlert } from '../ErrorAlert/ErrorAlert';

export const StartError = () => (
    <ErrorAlert>{I18n.t('useApp.startError')}</ErrorAlert>
);
