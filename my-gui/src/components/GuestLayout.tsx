import type { ReactNode } from 'react';

import { AppLogo } from './AppLogo';
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
            <AppLogo width={72} height={36} />
        </header>
        <main className="auth-layout__main">{children}</main>
        <PageFooter />
    </div>
);
