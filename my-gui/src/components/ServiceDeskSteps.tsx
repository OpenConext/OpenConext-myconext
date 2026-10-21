import { useState } from 'react';

import {
    ArrowLeftIcon,
    ArrowSquareOutIcon,
    IdentificationCardIcon,
    UserIcon,
} from '@phosphor-icons/react';
import {
    Alert,
    AlertDescription,
    AlertTitle,
    Button,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    Field,
    FieldGroup,
    FieldLabel,
    Input,
    Item,
    ItemContent,
    ItemDescription,
    ItemMedia,
    ItemTitle,
    NativeSelect,
    NativeSelectOption,
} from '@surfnet/curve-react';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { createControlCode, deleteControlCode } from '../api';
import type { ControlCode, User } from '../api';
import I18n from '../locale/I18n';
import './ServiceDeskSteps.scss';

type Step = 'form' | 'code';

type ServiceDeskStepsProps = {
    user: User;
    onBack: () => void;
    onClose: () => void;
};

// the backend stores the date of birth as `<day> <MON> <year>`
const MONTHS = [
    'JAN',
    'FEB',
    'MAR',
    'APR',
    'MAY',
    'JUN',
    'JUL',
    'AUG',
    'SEP',
    'OCT',
    'NOV',
    'DEC',
];
const CODE_VALIDITY_DAYS = 14;
const DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
const YEARS_BACK = 100;

const daysInMonth = (monthIndex: number, year: number | undefined) =>
    // without a year Feb gets the leap day, the year can still narrow it down
    new Date(year ?? 2000, monthIndex + 1, 0).getDate();

const daysLeft = (controlCode: ControlCode) =>
    Math.max(
        0,
        Math.ceil(
            (controlCode.createdAt +
                CODE_VALIDITY_DAYS * DAY_IN_MILLIS -
                Date.now()) /
                DAY_IN_MILLIS,
        ),
    );

const formatDayOfBirth = (dayOfBirth: string) => {
    const { day, month, year } = parseDayOfBirth(dayOfBirth);
    const monthIndex = MONTHS.indexOf(month);
    return monthIndex < 0
        ? dayOfBirth
        : `${day.padStart(2, '0')}-${`${monthIndex + 1}`.padStart(2, '0')}-${year}`;
};

const monthLabel = (monthIndex: number) =>
    new Intl.DateTimeFormat(I18n.locale, { month: 'long' }).format(
        new Date(2000, monthIndex, 1),
    );

const parseDayOfBirth = (dayOfBirth: string | undefined) => {
    const [day = '', month = '', year = ''] = (dayOfBirth ?? '').split(' ');
    return { day, month, year };
};

export const ServiceDeskSteps = ({
    user,
    onBack,
    onClose,
}: ServiceDeskStepsProps) => {
    const queryClient = useQueryClient();
    const controlCode = user.controlCode ?? undefined;
    const [step, setStep] = useState<Step>(controlCode ? 'code' : 'form');
    const [lastName, setLastName] = useState(controlCode?.lastName ?? '');
    const [firstName, setFirstName] = useState(controlCode?.firstName ?? '');
    const initialDate = parseDayOfBirth(controlCode?.dayOfBirth);
    const [day, setDay] = useState(initialDate.day);
    const [month, setMonth] = useState(initialDate.month);
    const [year, setYear] = useState(initialDate.year);

    const currentYear = new Date().getFullYear();
    const years = Array.from(
        { length: YEARS_BACK },
        (_, i) => `${currentYear - 1 - i}`,
    );
    const monthIndex = MONTHS.indexOf(month);
    const days =
        monthIndex < 0
            ? 31
            : daysInMonth(monthIndex, year ? Number(year) : undefined);

    const {
        mutate: create,
        isPending: creating,
        isError,
    } = useMutation({
        mutationFn: createControlCode,
        onSuccess: (created) => {
            queryClient.setQueryData(['me'], { ...user, controlCode: created });
            setStep('code');
        },
    });

    const { mutate: remove, isPending: removing } = useMutation({
        mutationFn: deleteControlCode,
        onSuccess: (updated) => {
            queryClient.setQueryData(['me'], updated);
            onClose();
        },
    });

    const changeMonth = (value: string) => {
        setMonth(value);
        // keep the day valid for the new month, e.g. 31 -> FEB
        const index = MONTHS.indexOf(value);
        if (
            index >= 0 &&
            Number(day) > daysInMonth(index, year ? Number(year) : undefined)
        ) {
            setDay('');
        }
    };

    const changeYear = (value: string) => {
        setYear(value);
        if (
            monthIndex >= 0 &&
            Number(day) > daysInMonth(monthIndex, Number(value))
        ) {
            setDay('');
        }
    };

    const formComplete =
        lastName.trim() !== '' &&
        firstName.trim() !== '' &&
        day !== '' &&
        month !== '' &&
        year !== '';

    if (isError) {
        return (
            <>
                <DialogHeader>
                    <DialogTitle className="service-desk__title">
                        {I18n.t('identity.serviceDesk.errorTitle')}
                    </DialogTitle>
                    <DialogDescription className="service-desk__text">
                        {I18n.t('identity.serviceDesk.errorInfo')}{' '}
                        <a href="mailto:help@eduid.nl">help@eduid.nl</a>
                    </DialogDescription>
                </DialogHeader>
                <Button variant="outline" onClick={onClose}>
                    {I18n.t('identity.serviceDesk.back')}
                </Button>
            </>
        );
    }

    if (step === 'form') {
        return (
            <>
                <DialogHeader>
                    <DialogTitle className="service-desk__title">
                        <Button
                            variant="ghost"
                            size="icon-sm"
                            aria-label={I18n.t('identity.verify.bankBack')}
                            onClick={() => onBack()}
                        >
                            <ArrowLeftIcon />
                        </Button>
                        {I18n.t('identity.serviceDesk.formTitle')}
                    </DialogTitle>
                    <DialogDescription className="service-desk__text">
                        {I18n.t('identity.serviceDesk.formInfo')}
                    </DialogDescription>
                </DialogHeader>
                <Alert variant="warning" className="service-desk__alert">
                    <IdentificationCardIcon />
                    <AlertTitle>
                        {I18n.t('identity.serviceDesk.validIdsTitle')}
                    </AlertTitle>
                    <AlertDescription>
                        {I18n.t('identity.serviceDesk.validIds')}
                        <span className="service-desk__small">
                            {I18n.t('identity.serviceDesk.eeaNote')}
                        </span>
                    </AlertDescription>
                </Alert>
                <FieldGroup>
                    <Field>
                        <FieldLabel htmlFor="controlLastName">
                            {I18n.t('identity.serviceDesk.lastName')}
                        </FieldLabel>
                        <Input
                            id="controlLastName"
                            value={lastName}
                            autoComplete="off"
                            spellCheck={false}
                            onChange={(e) => setLastName(e.target.value)}
                        />
                    </Field>
                    <Field>
                        <FieldLabel htmlFor="controlFirstName">
                            {I18n.t('identity.serviceDesk.firstName')}
                        </FieldLabel>
                        <Input
                            id="controlFirstName"
                            value={firstName}
                            autoComplete="off"
                            spellCheck={false}
                            onChange={(e) => setFirstName(e.target.value)}
                        />
                    </Field>
                    <div className="service-desk__date">
                        <Field>
                            <FieldLabel htmlFor="controlDay">
                                {I18n.t('identity.serviceDesk.day')}
                            </FieldLabel>
                            <NativeSelect
                                id="controlDay"
                                value={day}
                                onChange={(e) => setDay(e.target.value)}
                            >
                                <NativeSelectOption value="">
                                    {I18n.t('identity.serviceDesk.select')}
                                </NativeSelectOption>
                                {Array.from({ length: days }, (_, i) => (
                                    <NativeSelectOption
                                        key={i}
                                        value={`${i + 1}`}
                                    >
                                        {i + 1}
                                    </NativeSelectOption>
                                ))}
                            </NativeSelect>
                        </Field>
                        <Field>
                            <FieldLabel htmlFor="controlMonth">
                                {I18n.t('identity.serviceDesk.month')}
                            </FieldLabel>
                            <NativeSelect
                                id="controlMonth"
                                value={month}
                                onChange={(e) => changeMonth(e.target.value)}
                            >
                                <NativeSelectOption value="">
                                    {I18n.t('identity.serviceDesk.select')}
                                </NativeSelectOption>
                                {MONTHS.map((value, i) => (
                                    <NativeSelectOption
                                        key={value}
                                        value={value}
                                    >
                                        {monthLabel(i)}
                                    </NativeSelectOption>
                                ))}
                            </NativeSelect>
                        </Field>
                        <Field>
                            <FieldLabel htmlFor="controlYear">
                                {I18n.t('identity.serviceDesk.year')}
                            </FieldLabel>
                            <NativeSelect
                                id="controlYear"
                                value={year}
                                onChange={(e) => changeYear(e.target.value)}
                            >
                                <NativeSelectOption value="">
                                    {I18n.t('identity.serviceDesk.select')}
                                </NativeSelectOption>
                                {years.map((value) => (
                                    <NativeSelectOption
                                        key={value}
                                        value={value}
                                    >
                                        {value}
                                    </NativeSelectOption>
                                ))}
                            </NativeSelect>
                        </Field>
                    </div>
                </FieldGroup>
                <Button
                    disabled={!formComplete || creating}
                    onClick={() =>
                        create({
                            firstName: firstName.trim(),
                            lastName: lastName.trim(),
                            dayOfBirth: `${day} ${month} ${year}`,
                        })
                    }
                >
                    {I18n.t('identity.serviceDesk.continue')}
                </Button>
            </>
        );
    }

    return (
        <>
            <DialogHeader>
                <DialogTitle className="service-desk__title">
                    {I18n.t('identity.serviceDesk.codeTitle')}
                </DialogTitle>
            </DialogHeader>
            {controlCode && (
                <>
                    <div className="service-desk__code">{controlCode.code}</div>
                    <p className="service-desk__text">
                        {I18n.t('identity.serviceDesk.codeInfo', {
                            count: daysLeft(controlCode),
                        })}
                    </p>
                    <Item variant="outline">
                        <ItemMedia
                            variant="icon"
                            className="service-desk__media"
                        >
                            <UserIcon />
                        </ItemMedia>
                        <ItemContent>
                            <ItemTitle>
                                {controlCode.firstName} {controlCode.lastName}
                            </ItemTitle>
                            <ItemDescription>
                                {formatDayOfBirth(controlCode.dayOfBirth)}
                            </ItemDescription>
                        </ItemContent>
                    </Item>
                    <div className="service-desk__section">
                        <p className="service-desk__strong">
                            {I18n.t('identity.serviceDesk.todo')}
                        </p>
                        <p className="service-desk__text">
                            {I18n.t('identity.serviceDesk.todoDetails')}
                        </p>
                    </div>
                    <Button
                        nativeButton={false}
                        render={
                            <a
                                href={I18n.t(
                                    'identity.serviceDesk.locationsUrl',
                                )}
                                target="_blank"
                                rel="noreferrer"
                            />
                        }
                    >
                        {I18n.t('identity.serviceDesk.locations')}
                        <ArrowSquareOutIcon />
                    </Button>
                    <div className="service-desk__footer">
                        <p>{I18n.t('identity.serviceDesk.rethink')}</p>
                        <button
                            type="button"
                            className="service-desk__link"
                            disabled={removing}
                            onClick={() => remove()}
                        >
                            {I18n.t('identity.serviceDesk.delete')}
                        </button>
                    </div>
                </>
            )}
        </>
    );
};
