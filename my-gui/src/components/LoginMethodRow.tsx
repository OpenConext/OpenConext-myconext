import { useState } from 'react';
import type { ReactNode } from 'react';
import { Link } from 'react-router';

import { CaretDownIcon } from '@phosphor-icons/react';
import { Button } from '@surfnet/curve-react';

import I18n from '../locale/I18n';
import './LoginMethodRow.scss';

type LoginMethodRowProps = {
    icon: ReactNode;
    title: string;
    description: string;
    // a link to change the method
    changeTo?: string;
    // alternative to `changeTo`, for methods changed in a dialog
    onChange?: () => void;
    // expandable details, shown instead of the change link
    details?: ReactNode;
};

export const LoginMethodRow = ({
    icon,
    title,
    description,
    changeTo,
    onChange,
    details,
}: LoginMethodRowProps) => {
    const [open, setOpen] = useState(false);

    const content = (
        <>
            <span className="login-method__media">{icon}</span>
            <span className="login-method__text">
                <span className="login-method__title">{title}</span>
                <span className="login-method__description">{description}</span>
            </span>
        </>
    );

    return (
        <div className="login-method">
            {details ? (
                <button
                    type="button"
                    className="login-method__main login-method__main--toggle"
                    aria-expanded={open}
                    onClick={() => setOpen(!open)}
                >
                    {content}
                    <CaretDownIcon
                        className="login-method__caret"
                        data-open={open ? '' : undefined}
                    />
                </button>
            ) : (
                <div className="login-method__main">
                    {content}
                    {(changeTo || onChange) && (
                        <Button
                            variant="ghost"
                            className="login-method__change"
                            render={
                                changeTo ? <Link to={changeTo} /> : undefined
                            }
                            onClick={onChange}
                        >
                            {I18n.t('security.change')}
                        </Button>
                    )}
                </div>
            )}
            {open && details}
        </div>
    );
};
