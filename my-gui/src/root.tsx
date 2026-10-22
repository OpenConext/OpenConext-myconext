import { useEffect, useState } from 'react';
import { Links, Meta, Outlet, Scripts, ScrollRestoration } from 'react-router';

import { Button, Toaster } from '@surfnet/curve-react';
import '@surfnet/curve-react/styles.css';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

import './index.scss';
import I18n from './locale/I18n';
import { useAppStore } from './store/store';

export function Layout({ children }: { children: React.ReactNode }) {
    return (
        <html lang={I18n.locale}>
            <head>
                <meta charSet="utf-8" />
                <meta
                    name="viewport"
                    content="width=device-width, initial-scale=1"
                />
                <link rel="icon" type="image/png" href="/favicon.ico" />
                <link rel="stylesheet" href="/myconext/api/disclaimer" />
                <title>{I18n.t('globalVariables.productName')}</title>
                <Meta />
                <Links />
            </head>
            <body>
                {children}
                <ScrollRestoration />
                <Scripts />
            </body>
        </html>
    );
}

export default function Root() {
    // Global TanStack Query Client initialization
    const [queryClient] = useState(() => new QueryClient());
    const dark = useAppStore((state) => state.dark);

    useEffect(() => {
        document.documentElement.classList.toggle('dark', dark);
        document.documentElement.lang = I18n.locale;
    }, [dark]);

    return (
        <QueryClientProvider client={queryClient}>
            <Outlet />
            <Toaster position="bottom-right" offset={35} mobileOffset={35} />
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    );
}

// a render error would otherwise show the framework's default page
export function ErrorBoundary() {
    return (
        <div className="error-boundary">
            <h1>{I18n.t('errorBoundary.title')}</h1>
            <p>{I18n.t('errorBoundary.info')}</p>
            <Button onClick={() => window.location.reload()}>
                {I18n.t('errorBoundary.reload')}
            </Button>
        </div>
    );
}
