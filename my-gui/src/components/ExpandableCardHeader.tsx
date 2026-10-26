import type { ReactNode } from 'react';

import { CaretDownIcon } from '@phosphor-icons/react';

import './ExpandableCardHeader.scss';

type ExpandableCardHeaderProps = {
    logoUrl?: string;
    // shown when there is no logo
    placeholderIcon: ReactNode;
    name: ReactNode;
    subtitle?: ReactNode;
    open: boolean;
    onToggle: () => void;
    testId?: string;
};

export const ExpandableCardHeader = ({
    logoUrl,
    placeholderIcon,
    name,
    subtitle,
    open,
    onToggle,
    testId,
}: ExpandableCardHeaderProps) => (
    <button
        type="button"
        className="expandable-card-header"
        data-testid={testId}
        aria-expanded={open}
        onClick={onToggle}
    >
        {logoUrl ? (
            <img
                className="expandable-card-header__logo"
                src={logoUrl}
                alt=""
            />
        ) : (
            <span className="expandable-card-header__logo expandable-card-header__logo--placeholder">
                {placeholderIcon}
            </span>
        )}
        <span className="expandable-card-header__title">
            <span className="expandable-card-header__name">{name}</span>
            {subtitle && (
                <span className="expandable-card-header__subtitle">
                    {subtitle}
                </span>
            )}
        </span>
        <CaretDownIcon
            className="expandable-card-header__caret"
            data-open={open ? '' : undefined}
        />
    </button>
);
