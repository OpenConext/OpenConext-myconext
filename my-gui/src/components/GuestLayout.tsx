import type { ReactNode } from 'react';

import eduidLogo from '../assets/eduid-logo.svg';
import I18n from '../locale/I18n';
import './AuthLayout.scss';
import './GuestLayout.scss';
import { PageFooter } from './PageFooter';

type GuestLayoutProps = {
    children: ReactNode;
};

// the shell of the pages that need no login
export const GuestLayout = ({ children }: GuestLayoutProps) => (
    <div className="auth-layout">
        <header className="guest-layout__header">
            <img
                src={eduidLogo}
                alt={I18n.t('logo.eduid')}
                width={72}
                height={36}
            />
        </header>
        <main className="auth-layout__main">{children}</main>
        <PageFooter />
    </div>
);
