import { PencilSimpleIcon, ShieldCheckIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import './IdentityRow.scss';

type IdentityRowProps = {
    // prefix of the data-testid attributes of the row and its controls
    testId?: string;
    label: string;
    value: string;
    // shown in green below the value when the value is verified
    verifiedText?: string;
    // shows the details of a verified value, opened with View
    onView?: () => void;
    // makes the value editable
    onEdit?: () => void;
};

export const IdentityRow = ({
    testId,
    label,
    value,
    verifiedText,
    onView,
    onEdit,
}: IdentityRowProps) => {
    return (
        <div className="identity-row" data-testid={testId}>
            <div className="identity-row__main">
                <div className="identity-row__content">
                    <span className="identity-row__label">{label}</span>
                    <span className="identity-row__value">{value}</span>
                </div>
                {onEdit && (
                    <Button
                        variant="ghost"
                        className="identity-row__action"
                        data-testid={testId && `${testId}-edit`}
                        onClick={onEdit}
                    >
                        <PencilSimpleIcon />
                        {I18n.t('identity.edit')}
                    </Button>
                )}
                {onView && (
                    <Button
                        variant="ghost"
                        className="identity-row__action"
                        onClick={onView}
                    >
                        {I18n.t('identity.view')}
                    </Button>
                )}
            </div>
            {verifiedText && (
                <p className="identity-row__verified">
                    <ShieldCheckIcon />
                    {verifiedText}
                </p>
            )}
        </div>
    );
};
