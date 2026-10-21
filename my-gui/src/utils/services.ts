import type { OidcToken, User } from '../api';
import I18n from '../locale/I18n';

export type Service = {
    entityId: string;
    name: string;
    eduId: string;
    logoUrl?: string;
    homeUrl?: string;
    createdAt: number;
    lastLogin?: number;
    // all tokens of the service provider, revoked together
    tokens: OidcToken[];
    // the tokens that grant access to something the user can read about
    token?: OidcToken;
    scopeDescriptions: string[];
};

const describe = (scope: { descriptions: Record<string, string> }) =>
    scope.descriptions[I18n.locale] || scope.descriptions.en;

// the old eduID had multiple services per entity id, the name is localized
const serviceName = (service: { name: string; nameNl?: string }): string =>
    I18n.locale === 'nl' ? service.nameNl || service.name : service.name;

export const buildServices = (user: User, allTokens: OidcToken[]): Service[] =>
    Object.entries(user.eduIdPerServiceProvider ?? {})
        .flatMap(([entityId, provider]) => {
            const tokens = allTokens.filter(
                (token) => token.clientId === entityId,
            );
            const visible = tokens.filter((token) =>
                token.scopes?.some(
                    (scope) => scope.name !== 'openid' && describe(scope),
                ),
            );
            const token =
                visible.find((t) => t.type === 'REFRESH') ?? visible[0];
            const seen = new Set<string>();
            const scopeDescriptions = tokens
                .flatMap((t) => t.scopes ?? [])
                .filter(
                    (scope) =>
                        scope.name !== 'openid' &&
                        describe(scope) &&
                        !seen.has(scope.name) &&
                        seen.add(scope.name),
                )
                .map(describe);

            return provider.services.map((service) => ({
                entityId,
                name: serviceName(service),
                eduId: provider.value,
                logoUrl: service.logoUrl,
                homeUrl: service.homeUrl,
                createdAt: service.createdAt,
                lastLogin: service.lastLogin,
                tokens,
                token,
                scopeDescriptions,
            }));
        })
        .sort(
            (a, b) =>
                (b.lastLogin ?? b.createdAt) - (a.lastLogin ?? a.createdAt),
        );
