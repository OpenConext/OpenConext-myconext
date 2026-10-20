import { request } from './request';

export type ActivityEvent =
    | 'ACCOUNT_CREATED'
    | 'NAME_UPDATED'
    | 'LANGUAGE_CHANGED'
    | 'EMAIL_CHANGED'
    | 'PASSWORD_ADDED'
    | 'PASSWORD_UPDATED'
    | 'PASSWORD_DELETED'
    | 'PASSKEY_ADDED'
    | 'PASSKEY_RENAMED'
    | 'PASSKEY_DELETED'
    | 'APP_ACTIVATED'
    | 'APP_DEACTIVATED'
    | 'RECOVERY_METHOD_CHANGED'
    | 'INSTITUTION_LINKED'
    | 'INSTITUTION_REMOVED'
    | 'PREFERRED_INSTITUTION_CHANGED'
    | 'IDENTITY_VERIFIED'
    | 'IDENTITY_REMOVED'
    | 'CONTROL_CODE_CREATED'
    | 'CONTROL_CODE_DELETED'
    | 'SERVICE_REMOVED'
    | 'TOKENS_REMOVED';

export type Activity = {
    userId: string;
    event: ActivityEvent;
    // epoch milliseconds
    dateTime: number | string;
    description: string;
};

export const fetchActivities = () =>
    request<Activity[]>('/myconext/api/sp/activities');
