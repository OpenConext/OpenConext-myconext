import type { ReactNode } from 'react';

import './PageHeader.scss';

type PageHeaderProps = {
    icon: ReactNode;
    title: string;
    lead: string;
    color?: 'green' | 'purple' | 'blue' | 'turquoise';
};

export const PageHeader = ({
    icon,
    title,
    lead,
    color = 'green',
}: PageHeaderProps) => (
    <header className={`page-header page-header--${color}`}>
        <div className="page-header__heading">
            <span className="page-header__icon">{icon}</span>
            <h1 className="page-header__title">{title}</h1>
        </div>
        <p className="page-header__lead">{lead}</p>
    </header>
);
