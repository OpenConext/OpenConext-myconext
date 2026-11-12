import { useEffect } from 'react';

import { toast } from 'sonner';

import {
    Alert,
    AlertAction,
    AlertDescription,
    Button,
} from '@surfnet/curve-react';

import { useAppStore } from '../stores/AppStore';
import { isEmpty } from '../utils/Utils';
import './Flash.scss';

export const Flash = () => {
    const flash = useAppStore((state) => state.flash);
    const clearFlash = useAppStore((state) => state.clearFlash);

    const isAlert =
        !isEmpty(flash) && (flash.type === 'error' || flash.type === 'warning');
    const isToast = !isEmpty(flash) && !isAlert && !isEmpty(flash.msg);

    useEffect(() => {
        if (isToast) {
            toast.success(flash.msg);
        }
    }, [flash, isToast]);

    if (isAlert) {
        return (
            <Alert
                className="flash-alert"
                variant={flash.type === 'warning' ? 'warning' : 'danger'}
            >
                <AlertDescription>{flash.msg}</AlertDescription>
                <AlertAction>
                    {flash.action && (
                        <Button
                            variant="outline"
                            size="sm"
                            onClick={flash.action}
                        >
                            {flash.actionLabel}
                        </Button>
                    )}
                    <Button variant="ghost" size="sm" onClick={clearFlash}>
                        ×
                    </Button>
                </AlertAction>
            </Alert>
        );
    }
    return null;
};
