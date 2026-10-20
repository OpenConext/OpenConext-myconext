import type { BadgeCounts } from './hooks/useBadgeCounts';

export type NavigationItem = {
    to: string;
    labelKey: string;
    badge?: keyof BadgeCounts;
};

export const navigationItems: NavigationItem[] = [
    { to: '/', labelKey: 'navigation.home' },
    { to: '/identity', labelKey: 'navigation.identity', badge: 'identity' },
    { to: '/institutions', labelKey: 'navigation.institutions' },
    { to: '/security', labelKey: 'navigation.security', badge: 'security' },
    { to: '/activity', labelKey: 'navigation.activity' },
];
