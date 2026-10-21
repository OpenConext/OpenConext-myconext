import I18n from '../locale/I18n';

export const formatDateTime = (date: number | string): string =>
    new Intl.DateTimeFormat(I18n.locale, {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(date));

export const formatDate = (date: number | string): string =>
    new Intl.DateTimeFormat(I18n.locale, {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
    }).format(new Date(date));

// dates of birth are stored as midnight UTC
export const formatDateOfBirth = (date: number | string): string => {
    const d = new Date(date);
    const pad = (n: number) => `${n}`.padStart(2, '0');
    return `${pad(d.getUTCDate())}-${pad(d.getUTCMonth() + 1)}-${d.getUTCFullYear()}`;
};

const RELATIVE_UNITS: [Intl.RelativeTimeFormatUnit, number][] = [
    ['year', 365 * 24 * 60 * 60 * 1000],
    ['month', 30 * 24 * 60 * 60 * 1000],
    ['day', 24 * 60 * 60 * 1000],
    ['hour', 60 * 60 * 1000],
    ['minute', 60 * 1000],
];

// e.g. `1 day ago`, in the largest fitting unit
export const formatRelative = (date: number | string, now: number): string => {
    const diff = new Date(date).getTime() - now;
    const [unit, size] =
        RELATIVE_UNITS.find(([, s]) => Math.abs(diff) >= s) ??
        RELATIVE_UNITS[RELATIVE_UNITS.length - 1];
    return new Intl.RelativeTimeFormat(I18n.locale, {
        numeric: 'auto',
    }).format(Math.round(diff / size), unit);
};
