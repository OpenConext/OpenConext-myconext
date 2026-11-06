export type PhoneNavigationState = { phoneNumber?: string } | null;

const PHONE_NUMBER_PATTERN = /^\+?[0-9]{7,15}$/;

// The server expects the number without separators
export const normalizePhoneNumber = (value: string): string =>
    value.replaceAll(/[\s-]/g, '');

export const isValidPhoneNumber = (value: string): boolean =>
    PHONE_NUMBER_PATTERN.test(normalizePhoneNumber(value));
