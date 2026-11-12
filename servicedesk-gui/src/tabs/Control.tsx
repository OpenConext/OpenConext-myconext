import { useEffect, useRef, useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';

import { OtpInput, sanitizeHtml } from '@myconext/shared-gui';
import { Button, Switch } from '@surfnet/curve-react';

import { convertUserControlCode, validateDate } from '../api';
import CalendarIcon from '../icons/calendar-alt.svg';
import I18n from '../locale/I18n';
import { useAppStore } from '../stores/AppStore';
import { isEmpty } from '../utils/Utils';
import './Control.scss';

const DOCUMENT_ID_LENGTH = 6;

const dateFormatOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
};

const confirmationItems = ['photo', 'valid', 'lastName', 'firstName'];

const leadingZero = (num: number) => (num < 10 ? `0${num}` : num.toString());

type ControlProps = { proceed: () => void };

const Control = ({ proceed }: ControlProps) => {
    const controlCode = useAppStore((state) => state.controlCode);
    const [validDayOfBirth, setValidDayOfBirth] = useState(true);
    const [loading, setLoading] = useState(false);
    const [confirmations, setConfirmations] = useState<boolean[]>(
        Array(5).fill(false),
    );
    const [otp, setOtp] = useState('');
    const documentId = otp.length === DOCUMENT_ID_LENGTH ? otp : '';
    const [error, setError] = useState<Record<string, string>>({});
    const [birthDay, setBirthDay] = useState<Date | null>(null);

    const inputRef = useRef<DatePicker>(null);
    const toggle = () => inputRef.current?.setOpen(true);

    // only the date of birth the person arrived with is validated, not the one picked later
    const [initialDayOfBirth] = useState(controlCode.dayOfBirth);
    useEffect(() => {
        const load = async () =>
            setValidDayOfBirth(await validateDate(initialDayOfBirth));
        void load();
    }, [initialDayOfBirth]);

    const confirm = (index: number, value: boolean) => {
        const newConfirmations = [...confirmations];
        newConfirmations[index] = value;
        setConfirmations(newConfirmations);
    };

    const formatDayOfBirth = () => {
        const locale = I18n.locale === 'nl' ? 'nl-NL' : 'en-EN';
        return {
            dayOfBirth: birthDay?.toLocaleDateString(locale, dateFormatOptions),
        };
    };

    const convertDayOfBirth = (newDate: Date | null) => {
        setBirthDay(newDate);
        if (!newDate) {
            return;
        }
        //See AttributeMapper dateFormat yyyy-MM-dd
        const formattedDate = `${newDate.getFullYear()}-${leadingZero(newDate.getMonth() + 1)}-${leadingZero(newDate.getDate())}`;
        useAppStore.setState(() => ({
            controlCode: { ...controlCode, dayOfBirth: formattedDate },
        }));
    };

    const doConvertUserControlCode = async () => {
        const { firstName, lastName, dayOfBirth, code, userUid } = controlCode;
        setLoading(true);
        try {
            await convertUserControlCode(
                firstName ?? '',
                lastName ?? '',
                dayOfBirth ?? '',
                code ?? '',
                documentId,
                userUid ?? '',
            );
            setError({});
            setLoading(false);
            proceed();
        } catch (e) {
            setLoading(false);
            setError(e instanceof Response ? await e.json() : { error: '' });
            window.scrollTo({ top: 0, left: 0, behavior: 'smooth' });
        }
    };

    return (
        <div className="control">
            {!isEmpty(error) && (
                <div className="error">
                    <p
                        className="inner-html"
                        dangerouslySetInnerHTML={{
                            __html: sanitizeHtml(
                                I18n.t('control.error', error),
                            ),
                        }}
                    />
                </div>
            )}

            <section className="control__section">
                <div className="control__intro">
                    <h2>{I18n.t('control.info')}</h2>
                    <p>{I18n.t('control.validDocuments')}</p>
                </div>
                <div className="control__checks">
                    {confirmationItems.map((name, index) => (
                        <label key={name} className="control__check">
                            <Switch
                                checked={confirmations[index]}
                                onCheckedChange={(val) => confirm(index, val)}
                            />
                            <span
                                dangerouslySetInnerHTML={{
                                    __html: sanitizeHtml(
                                        I18n.t(
                                            `control.validations.${name}`,
                                            controlCode,
                                        ),
                                    ),
                                }}
                            />
                        </label>
                    ))}
                    <div className="control__check control__check--column">
                        <label className="control__check-row">
                            <Switch
                                checked={confirmations[4]}
                                disabled={!validDayOfBirth && birthDay === null}
                                onCheckedChange={(val) => confirm(4, val)}
                            />
                            <span
                                dangerouslySetInnerHTML={{
                                    __html: sanitizeHtml(
                                        I18n.t(
                                            'control.validations.dayOfBirth',
                                            !validDayOfBirth &&
                                                birthDay !== null
                                                ? formatDayOfBirth()
                                                : controlCode,
                                        ),
                                    ),
                                }}
                            />
                        </label>
                        {!validDayOfBirth && (
                            <div
                                className={`birthday ${birthDay === null ? 'invalid' : 'valid'}`}
                            >
                                <p>
                                    {I18n.t(
                                        `control.${birthDay === null ? 'invalidDate' : 'validDate'}`,
                                    )}
                                </p>
                                <DatePicker
                                    ref={inputRef}
                                    preventOpenOnFocus
                                    onChange={convertDayOfBirth}
                                    showWeekNumbers
                                    selected={birthDay}
                                    showYearDropdown={true}
                                    showMonthDropdown={true}
                                    dropdownMode="select"
                                    weekLabel="Week"
                                    todayButton={null}
                                />
                                <div className="calendar" onClick={toggle}>
                                    <CalendarIcon />
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </section>

            <section className="control__section">
                <h2
                    className="control__document"
                    dangerouslySetInnerHTML={{
                        __html: sanitizeHtml(I18n.t('control.idDocument')),
                    }}
                />
                <OtpInput
                    length={DOCUMENT_ID_LENGTH}
                    pattern="^[a-zA-Z0-9]*$"
                    autoFocus={false}
                    align="start"
                    ariaLabel={I18n.t('control.idDocument')}
                    value={otp}
                    onChange={setOtp}
                    onComplete={setOtp}
                />
            </section>

            <Button
                className="control__confirm"
                disabled={
                    isEmpty(documentId) ||
                    confirmations.some((val) => !val) ||
                    loading ||
                    !isEmpty(error) ||
                    (!validDayOfBirth && isEmpty(birthDay))
                }
                onClick={() => void doConvertUserControlCode()}
            >
                {I18n.t('control.proceed')}
            </Button>
        </div>
    );
};

export default Control;
