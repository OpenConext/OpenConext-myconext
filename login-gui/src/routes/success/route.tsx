import { LoginContainer } from '../../components';
import I18n, { tArray } from '../../locale/I18n.ts';

export default function Success() {
    return (
        <LoginContainer title={I18n.t('success.title')}>
            {tArray('success.info', (info, index) => (
                <p key={index}>{info}</p>
            ))}
        </LoginContainer>
    );
}
