import type { ActivityEntry } from '../api';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';

export type ActivityType = 'account' | 'security' | 'institution' | 'service';

const TYPES: Record<string, ActivityType> = {
    ACCOUNT_CREATED: 'account',
    NAME_UPDATED: 'account',
    LANGUAGE_CHANGED: 'account',
    EMAIL_CHANGED: 'account',
    PASSWORD_ADDED: 'security',
    PASSWORD_UPDATED: 'security',
    PASSWORD_DELETED: 'security',
    PASSKEY_ADDED: 'security',
    PASSKEY_RENAMED: 'security',
    PASSKEY_DELETED: 'security',
    APP_ACTIVATED: 'security',
    APP_DEACTIVATED: 'security',
    RECOVERY_METHOD_CHANGED: 'security',
    INSTITUTION_LINKED: 'institution',
    INSTITUTION_REMOVED: 'institution',
    PREFERRED_INSTITUTION_CHANGED: 'institution',
    IDENTITY_VERIFIED: 'institution',
    IDENTITY_REMOVED: 'institution',
    CONTROL_CODE_CREATED: 'institution',
    CONTROL_CODE_DELETED: 'institution',
    SERVICE_REMOVED: 'service',
    TOKENS_REMOVED: 'service',
    BLOCKED_ACCOUNT: 'account',
    DEBLOCKED_ACCOUNT: 'account',
    MFA_RESET: 'security',
    SERVICE_FIRST_LOGIN: 'service',
    SERVICE_LAST_LOGIN: 'service',
};

export const ACTIVITY_TYPES: ActivityType[] = [
    'account',
    'security',
    'institution',
    'service',
];

export const activityType = (event: string): ActivityType =>
    TYPES[event] ?? 'account';

// the context holds language-neutral values, the placeholders of the event translation
const localized = (
    context: Record<string, string>,
    key: 'institution' | 'service',
): string => {
    const value =
        (I18n.locale === 'nl' && context[`${key}Nl`]) || context[key] || '';
    return key === 'institution'
        ? I18n.t(`issuers.${value}`, { defaultValue: value })
        : value;
};

export const activityText = ({ event, context }: ActivityEntry): string => {
    const { variant, language, ...rest } = context ?? {};
    const params = {
        email: '',
        name: '',
        ...rest,
        // the actor of the activity, e.g. the service desk member that blocked the user
        you:
            context?.actor && context.actor === useAppStore.getState().user.id
                ? I18n.t('activity.you')
                : '',
        institution: localized(context ?? {}, 'institution'),
        service: localized(context ?? {}, 'service'),
        language: language ? I18n.t(`languages.${language}`) : '',
    };
    const base = `activity.events.${event}`;
    // events with variants hold an object, without a variant use its default
    const text = I18n.t(variant ? `${base}.${variant}` : base, params);
    return typeof text === 'string' ? text : I18n.t(`${base}.default`, params);
};
