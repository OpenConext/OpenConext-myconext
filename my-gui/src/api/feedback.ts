import { request } from './request';

export type FeedbackPayload = {
    message: string;
    url: string;
    includeScreenshot: boolean;
    screenshotBase64?: string;
    screenshotContentType?: string;
};

export const sendFeedback = (payload: FeedbackPayload) =>
    request<{ status: number }>('/myconext/api/sp/feedback', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
