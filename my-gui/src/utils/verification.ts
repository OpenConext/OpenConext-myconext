import type { LinkedAccount, User } from '../api';
import I18n from '../locale/I18n';

const hasNames = (account: LinkedAccount) =>
    Boolean(account.givenName && account.familyName);

const DAY_IN_MILLIS = 24 * 60 * 60 * 1000;

/**
 * When the account stops being valid. Institution accounts without names
 * only live for `nonValidatedDays` after they were received.
 */
export const accountExpiresAt = (
    account: LinkedAccount,
    nonValidatedDays?: number,
): number =>
    !account.external && !hasNames(account) && nonValidatedDays
        ? account.createdAt + nonValidatedDays * DAY_IN_MILLIS
        : account.expiresAt;

export const isExpired = (account: LinkedAccount, nonValidatedDays?: number) =>
    accountExpiresAt(account, nonValidatedDays) < Date.now();

// Studielink connections stay until the connection itself has ended
export const canDelete = (account: LinkedAccount): boolean =>
    account.idpScoping !== 'studielink' || account.connectionActive === false;

export const isValid = (account: LinkedAccount) =>
    account.expiresAt > Date.now();

/**
 * The account the verified names come from: a valid institution account
 * with names wins over an external (iDIN / eIDAS) account.
 */
export const preferredAccount = (user: User): LinkedAccount | undefined =>
    (user.linkedAccounts ?? []).find(
        (account) => account.preferred && hasNames(account) && isValid(account),
    ) ??
    (user.externalLinkedAccounts ?? []).find(
        (account) => account.preferred && isValid(account),
    );

export const isVerified = (user: User): boolean =>
    [...(user.linkedAccounts ?? []), ...(user.externalLinkedAccounts ?? [])]
        .filter((account) => hasNames(account) || account.external)
        .some(isValid);

export const institutionName = (account: LinkedAccount): string => {
    if (account.external) {
        return I18n.t(
            `identity.issuers.${(account.issuer?.name ?? '').toLowerCase()}`,
        );
    }
    const [primary, secondary] =
        I18n.locale === 'nl'
            ? [account.displayNameNl, account.displayNameEn]
            : [account.displayNameEn, account.displayNameNl];
    return primary || secondary || account.schacHomeOrganization || '';
};

const affiliationsOf = (account: LinkedAccount): string[] =>
    Array.from(
        new Set(
            (account.eduPersonAffiliations ?? []).map((affiliation) =>
                affiliation.slice(0, affiliation.indexOf('@')),
            ),
        ),
    ).filter(Boolean);

// the roles of the user at the institution, for example `student`
export const roleNames = (account: LinkedAccount): string =>
    account.external
        ? ''
        : affiliationsOf(account)
              .map((role) =>
                  I18n.t(`institutions.roles.${role}`, { defaultValue: role }),
              )
              .join(', ');

export const accountGivenName = (account: LinkedAccount) =>
    (account.external ? account.firstName : account.givenName) ?? '';

export const accountFamilyName = (account: LinkedAccount) =>
    (account.external ? account.legalLastName : account.familyName) ?? '';
