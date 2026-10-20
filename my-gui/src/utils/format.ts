import I18n from '../locale/I18n';

export const formatDateTime = (date: number | string): string =>
    new Intl.DateTimeFormat(I18n.locale, {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(date));
