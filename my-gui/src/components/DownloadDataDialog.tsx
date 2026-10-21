import {
    Button,
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@surfnet/curve-react';

import { PERSONAL_DATA_URL, personalDataFileName } from '../api';
import I18n from '../locale/I18n';

type DownloadDataDialogProps = {
    onClose: () => void;
};

// mounted only while open
export const DownloadDataDialog = ({ onClose }: DownloadDataDialogProps) => (
    <Dialog open onOpenChange={(open) => !open && onClose()}>
        <DialogContent>
            <DialogHeader>
                <DialogTitle>{I18n.t('account.download.title')}</DialogTitle>
                <DialogDescription>
                    {I18n.t('account.download.info')}
                </DialogDescription>
            </DialogHeader>
            <DialogFooter>
                <Button variant="outline" onClick={onClose}>
                    {I18n.t('account.download.cancel')}
                </Button>
                <Button
                    render={
                        <a
                            href={PERSONAL_DATA_URL}
                            download={personalDataFileName()}
                        />
                    }
                    onClick={onClose}
                >
                    {I18n.t('account.download.action')}
                </Button>
            </DialogFooter>
        </DialogContent>
    </Dialog>
);
