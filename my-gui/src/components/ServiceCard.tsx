import { useState } from 'react';

import { AppWindowIcon, CaretDownIcon } from '@phosphor-icons/react';
import { Button, Separator } from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import { formatDate, formatRelative } from '../utils/format';
import type { Service } from '../utils/services';
import './ServiceCard.scss';

type ServiceCardProps = {
    service: Service;
    // reference time for the relative login date
    now: number;
    onDelete: () => void;
    onRevoke: () => void;
};

export const ServiceCard = ({
    service,
    now,
    onDelete,
    onRevoke,
}: ServiceCardProps) => {
    const [open, setOpen] = useState(false);
    const { token } = service;

    return (
        <div className="service-card">
            <button
                type="button"
                className="service-card__header"
                aria-expanded={open}
                onClick={() => setOpen(!open)}
            >
                {service.logoUrl ? (
                    <img
                        className="service-card__logo"
                        src={service.logoUrl}
                        alt=""
                    />
                ) : (
                    <span className="service-card__logo service-card__logo--placeholder">
                        <AppWindowIcon />
                    </span>
                )}
                <span className="service-card__title">
                    <span className="service-card__name">{service.name}</span>
                    {service.lastLogin && (
                        <span className="service-card__login">
                            {I18n.t('activity.services.loggedIn', {
                                time: formatRelative(service.lastLogin, now),
                            })}
                        </span>
                    )}
                </span>
                <CaretDownIcon
                    className="service-card__caret"
                    data-open={open ? '' : undefined}
                />
            </button>
            {open && (
                <div className="service-card__details">
                    {token && (
                        <>
                            <div className="service-card__section">
                                {service.scopeDescriptions.map((text) => (
                                    <p key={text}>{text}</p>
                                ))}
                                <dl className="service-card__list">
                                    <dt>
                                        {I18n.t(
                                            'activity.services.accessGiven',
                                        )}
                                    </dt>
                                    <dd>{formatDate(token.createdAt)}</dd>
                                    <dt>
                                        {I18n.t(
                                            'activity.services.accessExpires',
                                        )}
                                    </dt>
                                    <dd>{formatDate(token.expiresIn)}</dd>
                                </dl>
                            </div>
                            <Separator />
                        </>
                    )}
                    <div className="service-card__section">
                        <dl className="service-card__eduid">
                            <dt>{I18n.t('activity.services.uniqueEduId')}</dt>
                            <dd>{service.eduId}</dd>
                        </dl>
                        <dl className="service-card__list">
                            <dt>{I18n.t('activity.services.firstLogin')}</dt>
                            <dd>{formatDate(service.createdAt)}</dd>
                            {service.lastLogin && (
                                <>
                                    <dt>
                                        {I18n.t('activity.services.lastLogin')}
                                    </dt>
                                    <dd>{formatDate(service.lastLogin)}</dd>
                                </>
                            )}
                            {service.homeUrl && (
                                <>
                                    <dt>
                                        {I18n.t('activity.services.homePage')}
                                    </dt>
                                    <dd>
                                        <a
                                            href={service.homeUrl}
                                            target="_blank"
                                            rel="noreferrer"
                                        >
                                            {service.homeUrl}
                                        </a>
                                    </dd>
                                </>
                            )}
                        </dl>
                    </div>
                    <Separator />
                    <div className="service-card__actions">
                        <Button
                            variant="ghost"
                            className="service-card__action"
                            onClick={onDelete}
                        >
                            {I18n.t('activity.services.deleteLogin')}
                        </Button>
                        {token && (
                            <Button
                                variant="ghost"
                                className="service-card__action"
                                onClick={onRevoke}
                            >
                                {I18n.t('activity.services.revoke')}
                            </Button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};
