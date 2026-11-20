import { ErrorAlert, LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';

export default function RateLimited() {
    return (
        <LoginContainer title={I18n.t('rateLimited.title')}>
            <ErrorAlert>{I18n.t('rateLimited.info')}</ErrorAlert>
        </LoginContainer>
    );
}
