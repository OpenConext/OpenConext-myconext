import type { SubmitEvent } from 'react';

import { WarningDiamondIcon } from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    Button,
    Field,
    FieldDescription,
    FieldLabel,
    Input,
} from '@surfnet/curve-react';

import type { SendError } from '../../api';
import I18n from '../../locale/I18n';
import { LoginContainer } from '../LoginContainer/LoginContainer';
import styles from './PhoneRecovery.module.scss';

interface PhoneVerificationViewProps {
    phoneNumber: string;
    onPhoneNumberChange: (value: string) => void;
    onSubmit: (event: SubmitEvent<HTMLFormElement>) => void;
    onBack: () => void;
    isPending: boolean;
    invalid: boolean;
    sendError: SendError | null;
}

export const PhoneVerificationView = ({
    phoneNumber,
    onPhoneNumberChange,
    onSubmit,
    onBack,
    isPending,
    invalid,
    sendError,
}: PhoneVerificationViewProps) => (
    <LoginContainer
        title={I18n.t('phoneVerification.title')}
        description={I18n.t('phoneVerification.description')}
        headerAlign="start"
        onBack={onBack}
        backLabel={I18n.t('phoneVerification.back')}
    >
        <form className={styles.form} onSubmit={onSubmit}>
            <Field>
                <FieldLabel htmlFor="phone-number">
                    {I18n.t('phoneVerification.label')}
                </FieldLabel>
                <Input
                    id="phone-number"
                    type="tel"
                    autoComplete="tel"
                    inputMode="tel"
                    autoFocus
                    placeholder={I18n.t('phoneVerification.placeholder')}
                    aria-invalid={invalid || undefined}
                    value={phoneNumber}
                    onChange={(e) => onPhoneNumberChange(e.target.value)}
                />
                <FieldDescription>
                    {I18n.t('phoneVerification.hint')}
                </FieldDescription>
            </Field>
            {invalid && (
                <Alert variant="warning">
                    <WarningDiamondIcon />
                    <AlertDescription>
                        {I18n.t('phoneVerification.incorrect')}
                    </AlertDescription>
                </Alert>
            )}
            {sendError !== null && (
                <Alert variant="warning">
                    <WarningDiamondIcon />
                    <AlertDescription>
                        {I18n.t(
                            sendError === 'rateLimited'
                                ? 'phoneVerification.rateLimited'
                                : 'phoneVerification.error',
                        )}
                    </AlertDescription>
                </Alert>
            )}
            <Button
                type="submit"
                disabled={
                    isPending ||
                    phoneNumber === '' ||
                    sendError === 'rateLimited'
                }
            >
                {I18n.t('phoneVerification.submit')}
            </Button>
        </form>
    </LoginContainer>
);
