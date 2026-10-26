import { useCallback, useRef, useState } from 'react';
import { useLocation } from 'react-router';

import DOMPurify from 'dompurify';
import html2canvas from 'html2canvas-pro';
import { toast } from 'sonner';

import {
    Button,
    Checkbox,
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    Label,
    Textarea,
} from '@surfnet/curve-react';
import { useMutation, useQuery } from '@tanstack/react-query';

import { fetchConfig, fetchMe, sendFeedback } from '../api';
import type { FeedbackPayload } from '../api';
import I18n from '../locale/I18n';
import './UserFeedbackWidget.scss';

const MAX_SCREENSHOT_BYTES = 5 * 1024 * 1024;

const sanitizeHtml = (html: string) =>
    DOMPurify.sanitize(html, { ADD_ATTR: ['target', 'rel'] });

export const UserFeedbackWidget = () => {
    const location = useLocation();
    const { data: user } = useQuery({ queryKey: ['me'], queryFn: fetchMe });
    const { data: config } = useQuery({
        queryKey: ['config'],
        queryFn: fetchConfig,
    });
    const [open, setOpen] = useState(false);
    const [message, setMessage] = useState('');
    const [includeScreenshot, setIncludeScreenshot] = useState(true);
    const [previewScreenshot, setPreviewScreenshot] = useState<string | null>(
        null,
    );
    const inputRef = useRef<HTMLTextAreaElement>(null);

    const closeModal = () => {
        setOpen(false);
        setMessage('');
        setIncludeScreenshot(true);
        setPreviewScreenshot(null);
        document.body.classList.remove('feedback-open');
    };

    const { mutate, isPending } = useMutation({
        mutationFn: sendFeedback,
        onSuccess: () => {
            closeModal();
            toast.success(I18n.t('feedback.flash'));
        },
    });

    const captureScreenshot = useCallback(async () => {
        window.scrollTo({ top: 0, behavior: 'instant' });
        document.body.classList.add('feedback-capture');
        try {
            const canvas = await html2canvas(document.body, {
                backgroundColor: null,
                useCORS: true,
                scale: 1,
                windowWidth: document.body.clientWidth,
                windowHeight: document.body.scrollHeight,
            });
            return canvas.toDataURL('image/png');
        } finally {
            document.body.classList.remove('feedback-capture');
        }
    }, []);

    const handleOpen = useCallback(async () => {
        // capture the page before the feedback dialog is in the DOM, so any
        // other open dialog ends up in the screenshot
        const dataUrl = await captureScreenshot();
        document.body.classList.add('feedback-open');
        setOpen(true);
        setPreviewScreenshot(dataUrl);
        setTimeout(() => inputRef.current?.focus(), 25);
    }, [captureScreenshot]);

    const handleSubmit = () => {
        if (!message.trim()) {
            return;
        }
        const payload: FeedbackPayload = {
            message,
            url: `${window.location.origin}${location.pathname}${location.search}${location.hash}`,
            includeScreenshot,
        };
        if (includeScreenshot && previewScreenshot) {
            const base64 = previewScreenshot.split(',')[1] || '';
            const estimatedBytes = Math.ceil((base64.length * 3) / 4);
            if (estimatedBytes > MAX_SCREENSHOT_BYTES) {
                toast.error(I18n.t('feedback.tooLarge'));
            } else if (base64.length > 0) {
                payload.screenshotBase64 = base64;
                payload.screenshotContentType = 'image/png';
            }
        }
        mutate(payload);
    };

    const renderMailPreview = () => {
        const dateStr = new Date().toLocaleDateString(I18n.locale, {
            weekday: 'short',
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });

        return (
            <div className="mail-preview">
                <div className="mail-preview__toolbar">
                    <div className="mail-preview__dots">
                        <span className="dot red" />
                        <span className="dot yellow" />
                        <span className="dot green" />
                    </div>
                    <span className="mail-preview__toolbar-title">
                        {I18n.t('feedback.preview.subjectLineInfo')}
                    </span>
                </div>
                <div className="mail-preview__header">
                    <div className="mail-preview__field">
                        <span className="lefty">
                            {I18n.t('feedback.preview.to')}:
                        </span>
                        <span className="value">{config?.supportEmail}</span>
                    </div>
                    <div className="mail-preview__field">
                        <span className="lefty">
                            {I18n.t('feedback.preview.from')}:
                        </span>
                        <span className="value">
                            {config?.feedbackFromEmail}
                        </span>
                    </div>
                    <div className="mail-preview__field">
                        <span className="lefty">
                            {I18n.t('feedback.preview.subject')}:
                        </span>
                        <span className="value">
                            {I18n.t('feedback.preview.subjectLine')}
                        </span>
                    </div>
                    <div className="mail-preview__field">
                        <span className="lefty">
                            {I18n.t('feedback.preview.date')}:
                        </span>
                        <span className="value">{dateStr}</span>
                    </div>
                </div>
                <div className="mail-preview__body">
                    <p className="greeting">
                        {I18n.t('feedback.preview.greeting')}
                    </p>
                    <p className="intro">
                        {I18n.t('feedback.preview.providedFeedback', {
                            name: user?.chosenName || '',
                        })}
                    </p>
                    <div className="mail-preview__quote">
                        <p className={message.trim() ? '' : 'placeholder'}>
                            {message.trim() ||
                                I18n.t('feedback.preview.messagePlaceholder')}
                        </p>
                    </div>
                    {includeScreenshot && previewScreenshot && (
                        <div className="mail-preview__screenshot">
                            <img
                                src={previewScreenshot}
                                alt={I18n.t('feedback.preview.screenshotLabel')}
                            />
                        </div>
                    )}
                    {includeScreenshot && !previewScreenshot && (
                        <div className="mail-preview__screenshot-loading" />
                    )}
                    <p className="follow-up">
                        {I18n.t('feedback.preview.followUp', {
                            email: user?.email || '',
                        })}
                    </p>
                </div>
            </div>
        );
    };

    return (
        <>
            {open ? (
                <Dialog open onOpenChange={(next) => !next && closeModal()}>
                    <DialogContent className="feedback-preview-dialog">
                        <DialogHeader>
                            <DialogTitle>
                                {I18n.t('feedback.title')}
                            </DialogTitle>
                        </DialogHeader>
                        <div className="user-feedback-widget__layout">
                            <div className="user-feedback-widget__form">
                                <div className="user-feedback-widget__modal">
                                    <p>{I18n.t('feedback.info')}</p>
                                    <Textarea
                                        name="feedback"
                                        id="feedback"
                                        value={message}
                                        rows={10}
                                        ref={inputRef}
                                        onChange={(e) =>
                                            setMessage(e.target.value)
                                        }
                                    />
                                    <Label className="user-feedback-widget__options">
                                        <Checkbox
                                            checked={includeScreenshot}
                                            onCheckedChange={() =>
                                                setIncludeScreenshot(
                                                    !includeScreenshot,
                                                )
                                            }
                                        />
                                        {I18n.t('feedback.includeScreenshot')}
                                    </Label>
                                    <section className="disclaimer">
                                        <span
                                            dangerouslySetInnerHTML={{
                                                __html: sanitizeHtml(
                                                    I18n.t(
                                                        'feedback.disclaimer',
                                                    ),
                                                ),
                                            }}
                                        />
                                    </section>
                                    <section className="help">
                                        <h3 className="title">
                                            {I18n.t('feedback.help')}
                                        </h3>
                                        <span
                                            dangerouslySetInnerHTML={{
                                                __html: sanitizeHtml(
                                                    I18n.t(
                                                        'feedback.helpInfo',
                                                        {
                                                            email:
                                                                config?.supportEmail ??
                                                                '',
                                                        },
                                                    ),
                                                ),
                                            }}
                                        />
                                    </section>
                                </div>
                            </div>
                            {renderMailPreview()}
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={closeModal}>
                                {I18n.t('feedback.cancel')}
                            </Button>
                            <Button
                                disabled={isPending || !message.trim()}
                                onClick={handleSubmit}
                            >
                                {I18n.t('feedback.submit')}
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            ) : (
                <div className="user-feedback-widget">
                    <button
                        className="user-feedback-widget__trigger"
                        onClick={handleOpen}
                        type="button"
                    >
                        {I18n.t('feedback.widgetLabel')}
                    </button>
                </div>
            )}
        </>
    );
};
