import type { ReactNode } from 'react';

import './PageHeader.scss';

type PageHeaderProps = {
    children: ReactNode;
    subtitle?: ReactNode;
    action?: ReactNode;
    // use the full content width, for overviews
    wide?: boolean;
};

export const PageHeader = ({
    children,
    subtitle,
    action,
    wide,
}: PageHeaderProps) => (
    <div className={`page-header ${wide ? 'page-header--wide' : ''}`}>
        <div className="page-header__text">
            <h1>{children}</h1>
            {subtitle && <p className="page-header__subtitle">{subtitle}</p>}
        </div>
        {action}
    </div>
);
