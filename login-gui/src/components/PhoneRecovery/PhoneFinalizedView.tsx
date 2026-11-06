import { Button } from '@surfnet/curve-react';

import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';

interface PhoneFinalizedViewProps {
    onNext: () => void;
}

export const PhoneFinalizedView = ({ onNext }: PhoneFinalizedViewProps) => (
    <LoginContainer
        title={I18n.t('phoneVerification.finalizedTitle')}
        description={I18n.t('phoneVerification.finalizedDescription')}
        headerAlign="start"
    >
        <Button type="button" onClick={onNext}>
            {I18n.t('phoneVerification.finalizedNext')}
        </Button>
    </LoginContainer>
);
