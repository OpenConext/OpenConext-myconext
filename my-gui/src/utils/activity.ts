import type { Activity, User } from '../api';
import I18n from '../locale/I18n';
import { buildServices } from './services';
import { institutionName } from './verification';

// the context holds language-neutral values, the placeholders of the event translation
// external accounts are recorded with their idpScoping, the other names with their Dutch translation
const localized = (
    context: Record<string, string>,
    key: 'institution' | 'service',
): string => {
    const value =
        (I18n.locale === 'nl' && context[`${key}Nl`]) || context[key] || '';
    return key === 'institution'
        ? I18n.t(`identity.issuers.${value}`, { defaultValue: value })
        : value;
};

export const activityText = ({ event, context }: Activity): string => {
    const { variant, language, ...rest } = context ?? {};
    const params = {
        email: '',
        name: '',
        ...rest,
        institution: localized(context ?? {}, 'institution'),
        service: localized(context ?? {}, 'service'),
        language: language ? I18n.t(`languages.${language}`) : '',
    };
    const base = `activity.events.${event}`;
    // events with variants hold an object, without a variant use its default
    const text = I18n.t(variant ? `${base}.${variant}` : base, params);
    return typeof text === 'string' ? text : I18n.t(`${base}.default`, params);
};

const time = (date: number | string): number => new Date(date).getTime();

// facts the user data already holds, for the time before activities were recorded
const buildHistory = (user: User): Activity[] => {
    const item = (
        event: Activity['event'],
        dateTime: number,
        context: Record<string, string> = {},
    ): Activity => ({ userId: user.id, event, dateTime, context });

    const history: Activity[] = [];
    // created is in seconds
    if (typeof user.created === 'number' && user.created > 0) {
        history.push(item('ACCOUNT_CREATED', user.created * 1000));
    }
    if (user.passwordUpdatedAt) {
        history.push(item('PASSWORD_UPDATED', user.passwordUpdatedAt));
    }
    (user.linkedAccounts ?? []).forEach((account) =>
        history.push(
            item('INSTITUTION_LINKED', account.createdAt, {
                institution: institutionName(account),
            }),
        ),
    );
    (user.externalLinkedAccounts ?? []).forEach((account) =>
        history.push(
            item('IDENTITY_VERIFIED', account.createdAt, {
                institution: institutionName(account),
            }),
        ),
    );
    buildServices(user, []).forEach((service) => {
        history.push(
            item('SERVICE_FIRST_LOGIN', service.createdAt, {
                service: service.name,
            }),
        );
        if (service.lastLogin && service.lastLogin !== service.createdAt) {
            history.push(
                item('SERVICE_LAST_LOGIN', service.lastLogin, {
                    service: service.name,
                }),
            );
        }
    });
    return history.filter((entry) => time(entry.dateTime) > 0);
};

// the recorded activities with the history from before the first recorded one,
// most recent first
export const withHistory = (
    user: User,
    recorded: Activity[],
    limit?: number,
): Activity[] => {
    const oldest = Math.min(...recorded.map((a) => time(a.dateTime)));
    const merged = [
        ...recorded,
        ...buildHistory(user).filter((entry) => time(entry.dateTime) < oldest),
    ].sort((a, b) => time(b.dateTime) - time(a.dateTime));
    return limit ? merged.slice(0, limit) : merged;
};
