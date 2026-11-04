import { BuildingOfficeIcon } from '@phosphor-icons/react';
import { Alert, AlertDescription, Button } from '@surfnet/curve-react';

import { LoginContainer } from '../../components';
import I18n from '../../locale/I18n.ts';
import type { SupportedExplanation } from './ValidateNamesMode.ts';

interface ValidateNamesExplanationProps {
    explanation: SupportedExplanation;
    serviceName: string;
    onContinue: () => void;
}

export const ValidateNamesExplanation = ({
    explanation,
    serviceName,
    onContinue,
}: ValidateNamesExplanationProps) => (
    <LoginContainer
        title={I18n.t('stepup.title')}
        description={I18n.t(`stepup.${explanation}.description`, {
            serviceName,
        })}
        headerAlign="start"
        footer={
            <p>
                {I18n.t(`stepup.${explanation}.footer`, {
                    serviceName,
                })}
            </p>
        }
    >
        <Alert>
            <BuildingOfficeIcon aria-hidden />
            <AlertDescription>
                {I18n.t(`stepup.${explanation}.alert`)}
            </AlertDescription>
        </Alert>
        <Button type="button" onClick={onContinue} autoFocus>
            {I18n.t('stepup.submit')}
        </Button>
    </LoginContainer>
);
