export * from './config.ts';
export * from './logout.ts';

export function redirectToLogin(loginUrl: string, redirectTo?: string): void {
    const url = new URL(loginUrl);
    url.searchParams.set('redirect_path', redirectTo || '/');
    url.searchParams.set('registration_id', 'my_conext');
    window.location.href = url.toString();
}
