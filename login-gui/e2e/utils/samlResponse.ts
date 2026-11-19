import type { Request } from '@playwright/test';

interface SamlResponse {
    authnContextClassRef: string | undefined;
    statusCodes: string[];
}

/**
 * Reads the SAMLResponse the browser posted to the ACS URL (unsigned XML in base64, not encrypted).
 */
export const readSamlResponse = (acsRequest: Request): SamlResponse => {
    const encoded = new URLSearchParams(acsRequest.postData() ?? '').get(
        'SAMLResponse',
    );
    if (!encoded) {
        throw new Error('The ACS request has no SAMLResponse');
    }
    const xml = Buffer.from(encoded, 'base64').toString('utf8');
    return {
        authnContextClassRef: xml.match(
            /<(?:\w+:)?AuthnContextClassRef[^>]*>([^<]+)</,
        )?.[1],
        statusCodes: [
            ...xml.matchAll(/<(?:\w+:)?StatusCode Value="([^"]+)"/g),
        ].map((match) => match[1]),
    };
};
