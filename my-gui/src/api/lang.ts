import { apiFetch } from './apiFetch';

export const updateLanguage = (language: string) =>
    apiFetch<null>('/myconext/api/sp/lang', {
        method: 'PUT',
        body: { language },
    });
