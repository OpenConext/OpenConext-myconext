import { useState } from 'react';

import { BuildingOfficeIcon, CaretDownIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import type { LinkedAccount } from '../api';
import I18n from '../locale/I18n';
import { formatDate } from '../utils/format';
import {
    accountFamilyName,
    accountGivenName,
    institutionName,
    roleNames,
} from '../utils/verification';
import './InstitutionCard.scss';

type InstitutionCardProps = {
    account: LinkedAccount;
    // without it the details are read-only
    onDelete?: () => void;
    // a standalone card, for example in a dialog, instead of a list row
    bordered?: boolean;
};

export const InstitutionCard = ({
    account,
    onDelete,
    bordered,
}: InstitutionCardProps) => {
    const [open, setOpen] = useState(false);
    const roles = roleNames(account);

    return (
        <div
            className={`institution-card${bordered ? ' institution-card--bordered' : ''}`}
        >
            <button
                type="button"
                className="institution-card__header"
                aria-expanded={open}
                onClick={() => setOpen(!open)}
            >
                {account.logoUrl ? (
                    <img
                        className="institution-card__logo"
                        src={account.logoUrl}
                        alt=""
                    />
                ) : (
                    <span className="institution-card__logo institution-card__logo--placeholder">
                        <BuildingOfficeIcon />
                    </span>
                )}
                <span className="institution-card__title">
                    <span className="institution-card__name">
                        {institutionName(account)}
                    </span>
                    {roles && (
                        <span className="institution-card__role">
                            {I18n.t('institutions.role', { roles })}
                        </span>
                    )}
                </span>
                <CaretDownIcon
                    className="institution-card__caret"
                    data-open={open ? '' : undefined}
                />
            </button>
            {open && (
                <>
                    <dl className="institution-card__details">
                        <dt>{I18n.t('institutions.details.firstName')}</dt>
                        <dd>{accountGivenName(account)}</dd>
                        <dt>{I18n.t('institutions.details.lastName')}</dt>
                        <dd>{accountFamilyName(account)}</dd>
                        <dt>{I18n.t('institutions.details.receivedOn')}</dt>
                        <dd>{formatDate(account.createdAt)}</dd>
                        <dt>{I18n.t('institutions.details.validUntil')}</dt>
                        <dd>{formatDate(account.expiresAt)}</dd>
                    </dl>
                    {onDelete && (
                        <Button
                            variant="ghost"
                            className="institution-card__delete"
                            onClick={onDelete}
                        >
                            {I18n.t('institutions.delete.button')}
                        </Button>
                    )}
                </>
            )}
        </div>
    );
};
