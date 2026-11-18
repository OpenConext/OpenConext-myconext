import { randomUUID } from 'node:crypto';
import { deflateRawSync } from 'node:zlib';

import { type APIRequestContext, type BrowserContext } from '@playwright/test';

// Mirrors myconext-server/src/test/resources/authn_request.xml. The issuer must be listed in `sp_entity_id`.
const buildAuthnRequest = (): string => `<?xml version="1.0" encoding="UTF-8"?>
<samlp:AuthnRequest
        AssertionConsumerServiceURL="https://engine.test.surfconext.nl/authentication/sp/consume-assertion"
        Destination="https://mujina-idp.test.surfconext.nl/SingleSignOnService"
        ID="${randomUUID()}" IssueInstant="${new Date().toISOString()}"
        ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Version="2.0"
        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
        xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol">
    <saml:Issuer>https://engine.test.surfconext.nl/authentication/sp/metadata</saml:Issuer>
    <samlp:NameIDPolicy AllowCreate="true" Format="urn:mace:dir:attribute-def:eduPersonPrincipalName"/>
</samlp:AuthnRequest>`;

/**
 * Calls the backend SSO endpoint directly (HTTP-Redirect binding, unsigned) and returns the
 * SamlAuthenticationRequest id from the Location header (`<gui>/login/<id>` or `<gui>/request/<id>`).
 */
export const createAuthenticationRequestId = async (
    request: APIRequestContext,
    baseURL = 'http://localhost:8081',
): Promise<string> => {
    const samlRequest = deflateRawSync(
        Buffer.from(buildAuthnRequest()),
    ).toString('base64');
    const response = await request.get(`${baseURL}/saml/guest-idp/SSO`, {
        params: { SAMLRequest: samlRequest },
        maxRedirects: 0,
    });
    const location = response.headers()['location'];
    const match = location?.match(/\/(?:login|request)\/([0-9a-f-]{36})/);
    if (response.status() !== 302 || !match) {
        throw new Error(
            `Unexpected SSO response ${response.status()}, location: ${location}`,
        );
    }
    return match[1];
};

// The server sets this cookie when the browser itself starts the SSO. Without it a password login is
// treated as a login on a different device and ends on /success instead of continuing the flow.
export const addBrowserSessionCookie = async (
    context: BrowserContext,
    baseURL = 'http://localhost:8081',
): Promise<void> => {
    await context.addCookies([
        { name: 'BROWSER_SESSION', value: 'true', url: baseURL },
    ]);
};
