export * from './activities.ts';
export * from './config.ts';
export * from './lang.ts';
export * from './logout.ts';
export * from './me.ts';

export function redirectToLogin(loginUrl: string, redirectTo?: string): void {
    const url = new URL(loginUrl);
    url.searchParams.set('redirect_path', redirectTo || '/');
    url.searchParams.set('registration_id', 'my_conext');
    window.location.href = url.toString();
}
