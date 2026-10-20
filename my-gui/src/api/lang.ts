import { putJson } from './request';

export const updateLanguage = (language: string) =>
    putJson<null>('/myconext/api/sp/lang', { language });
