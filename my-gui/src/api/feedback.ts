import { apiFetch } from './apiFetch';

export type FeedbackPayload = {
    message: string;
    url: string;
    includeScreenshot: boolean;
    screenshotBase64?: string;
    screenshotContentType?: string;
};

export const sendFeedback = (payload: FeedbackPayload) =>
    apiFetch<{ status: number }>('/myconext/api/sp/feedback', {
        method: 'POST',
        body: payload,
    });
