import { Link } from 'react-router';

import { PencilSimpleIcon, ShieldCheckIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import './IdentityRow.scss';

type IdentityRowProps = {
    label: string;
    value: string;
    // shown in green below the value when the value is verified
    verifiedText?: string;
    // the details of a verified value, opened with View
    viewTo?: string;
    // makes the value editable
    onEdit?: () => void;
};

export const IdentityRow = ({
    label,
    value,
    verifiedText,
    viewTo,
    onEdit,
}: IdentityRowProps) => {
    return (
        <div className="identity-row">
            <div className="identity-row__main">
                <div className="identity-row__content">
                    <span className="identity-row__label">{label}</span>
                    <span className="identity-row__value">{value}</span>
                </div>
                {onEdit && (
                    <Button
                        variant="ghost"
                        className="identity-row__action"
                        onClick={onEdit}
                    >
                        <PencilSimpleIcon />
                        {I18n.t('identity.edit')}
                    </Button>
                )}
                {viewTo && (
                    <Button
                        variant="ghost"
                        className="identity-row__action"
                        render={<Link to={viewTo} />}
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
