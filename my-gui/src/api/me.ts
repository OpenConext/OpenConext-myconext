import type { Activity } from './activities';
import { request } from './request';

export type User = {
    id: string;
    email: string;
    chosenName: string;
    givenName: string;
    familyName: string;
    preferredLanguage?: string;
    loginOptions?: string[];
    linkedAccounts?: unknown[];
    externalLinkedAccounts?: unknown[];
    // the most recent activities, all activities are available in fetchActivities
    recentActivities?: Activity[];
    [key: string]: unknown;
};

export const fetchMe = () => request<User>('/myconext/api/sp/me');
