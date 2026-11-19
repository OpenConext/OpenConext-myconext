import { createHmac } from 'node:crypto';

const OCRA_SUITE = 'OCRA-1:HOTP-SHA1-6:QH10-S064';
const DIGITS = 6;

/**
 * Port of `tiqr.org.secure.OCRA.generateOCRA(secret, challenge, sessionKey)` (RFC 6287), the answer the eduID app
 * gives to an authentication. `secret` and `sessionKey` are hex strings, `challenge` is the QH10 challenge.
 */
export const generateOcra = (
    secret: string,
    challenge: string,
    sessionKey: string,
): string => {
    const message = Buffer.concat([
        Buffer.from(OCRA_SUITE, 'utf8'),
        Buffer.from([0]),
        Buffer.from(challenge.padEnd(256, '0'), 'hex'),
        Buffer.from(sessionKey.padStart(128, '0'), 'hex'),
    ]);
    const hash = createHmac('sha1', Buffer.from(secret, 'hex'))
        .update(message)
        .digest();

    const offset = hash[hash.length - 1] & 0x0f;
    const binary = hash.readUInt32BE(offset) & 0x7fffffff;
    return (binary % 10 ** DIGITS).toString().padStart(DIGITS, '0');
};
