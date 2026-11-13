import { createApiFetch } from '@myconext/shared-gui';

import I18n from '../locale/I18n';
import type { Config, ControlCode, User } from '../stores/AppStore';

const apiFetch = createApiFetch({
    getLocale: () => I18n.locale,
    reloadOnSessionLoss: true,
});

//Users
export const me = () => apiFetch<User>('/myconext/api/servicedesk/me');

export const configuration = () => apiFetch<Config>('/config');

export const logout = () => apiFetch('/myconext/api/servicedesk/logout');

//Service Desk
export const getUserControlCode = (code: string) =>
    apiFetch<ControlCode>(`/myconext/api/servicedesk/user/${code}`);

export const validateDate = (dayOfBirth?: string) =>
    apiFetch<boolean>(
        `/myconext/api/servicedesk/validate?dayOfBirth=${encodeURIComponent(dayOfBirth ?? '')}`,
    );

export const convertUserControlCode = (
    firstName: string,
    lastName: string,
    dayOfBirth: string,
    code: string,
    documentId: string,
    userUid: string,
) =>
    apiFetch('/myconext/api/servicedesk/approve', {
        method: 'PUT',
        body: { firstName, lastName, dayOfBirth, code, documentId, userUid },
        keepOnUnauthorized: true,
    });

export type ServiceDeskUser = {
    id: string;
    uid: string;
    name: string;
    email: string;
    blocked: boolean;
    mfaMethods: string[];
    affiliations: string[];
    roles: string[];
};

export type ServiceDeskUserSearchResult = {
    users: ServiceDeskUser[];
    total: number;
    page: number;
    pageSize: number;
};

export type UserSearchParams = {
    query?: string;
    mfa?: string[];
    blocked?: boolean;
    sort?: 'name' | 'email';
    direction?: 'asc' | 'desc';
    page?: number;
    size?: number;
};

export const searchUsers = ({ mfa = [], ...params }: UserSearchParams) => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== '') {
            searchParams.set(key, String(value));
        }
    });
    mfa.forEach((method) => searchParams.append('mfa', method));
    return apiFetch<ServiceDeskUserSearchResult>(
        `/myconext/api/servicedesk/users?${searchParams.toString()}`,
    );
};

export type ActivityEntry = {
    userId: string;
    event: string;
    dateTime: number;
    context: Record<string, string>;
};

export type ServiceDeskUserDetail = {
    id: string;
    uid: string;
    eduPersonPrincipalName: string;
    email: string;
    givenName?: string;
    familyName?: string;
    chosenName?: string;
    dateOfBirth?: number;
    namesVerified: boolean;
    blocked: boolean;
    mfaMethods: string[];
    affiliations: string[];
    verifiedBy: string[];
    activities: ActivityEntry[];
    // the names of the linked account at the institution of the service desk member
    institutionGivenName?: string;
    institutionFamilyName?: string;
};

export const getUserDetail = (id: string) =>
    apiFetch<ServiceDeskUserDetail>(
        `/myconext/api/servicedesk/users/${encodeURIComponent(id)}`,
    );

export const blockUser = (id: string) =>
    apiFetch(
        `/myconext/api/servicedesk/users/${encodeURIComponent(id)}/block`,
        {
            method: 'PUT',
        },
    );

export type ServiceUsage = {
    name: string;
    users: number;
};

export type ServiceDeskOverview = {
    // the month (yyyy-MM) of the first entry in added
    firstMonth: string | null;
    // the accounts without a creation date
    baseline: number;
    // the new accounts per month, from firstMonth until the current month
    added: number[];
    services: ServiceUsage[];
};

export const getOverview = () =>
    apiFetch<ServiceDeskOverview>('/myconext/api/servicedesk/overview');

export const unblockUser = (id: string) =>
    apiFetch(
        `/myconext/api/servicedesk/users/${encodeURIComponent(id)}/unblock`,
        {
            method: 'PUT',
        },
    );

export const resetMfa = (id: string) =>
    apiFetch(
        `/myconext/api/servicedesk/users/${encodeURIComponent(id)}/reset-mfa`,
        {
            method: 'PUT',
        },
    );
