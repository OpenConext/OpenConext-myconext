import { request } from './request';

export const PERSONAL_DATA_URL = '/myconext/api/sp/personal';

export const deleteAccount = () =>
    request<{ status: number }>('/myconext/api/sp/delete', {
        method: 'DELETE',
    });

export const personalDataFileName = (date = new Date()) => {
    const pad = (n: number) => `${n}`.padStart(2, '0');
    return `eduid_export_${pad(date.getDate())}${pad(date.getMonth() + 1)}${date.getFullYear()}.json`;
};
