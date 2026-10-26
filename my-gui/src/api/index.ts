export * from './account.ts';
export * from './activities.ts';
export * from './config.ts';
export * from './createFromInstitution.ts';
export * from './email.ts';
export * from './feedback.ts';
export * from './identity.ts';
export * from './lang.ts';
export * from './logout.ts';
export * from './me.ts';
export * from './passkey.ts';
export * from './password.ts';
export * from './services.ts';
export * from './tiqr.ts';

// `force` makes the server add prompt=login to the OIDC authorization request
export function redirectToLogin(
    loginUrl: string,
    redirectTo?: string,
    force = false,
): void {
    const url = new URL(loginUrl);
    url.searchParams.set('redirect_path', redirectTo || '/');
    url.searchParams.set('registration_id', 'my_conext');
    // tells the server to redirect back to my-gui instead of myconext-gui
    url.searchParams.set('app', 'my_gui');
    if (force) {
        url.searchParams.set('force', 'true');
    }
    window.location.href = url.toString();
}
