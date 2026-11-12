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
