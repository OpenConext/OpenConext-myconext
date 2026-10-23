import { useState } from 'react';

import { toast } from 'sonner';

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
    FieldError,
    FieldGroup,
    FieldLabel,
    Input,
    Item,
    ItemContent,
    ItemDescription,
    ItemMedia,
    ItemTitle,
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
const NUMBER = /^\d+$/;

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

type FormErrors = Partial<
    Record<'firstName' | 'lastName' | 'day' | 'month' | 'year' | 'date', string>
>;

const validate = (values: {
    firstName: string;
    lastName: string;
    day: string;
    month: string;
    year: string;
}): FormErrors => {
    const errors: FormErrors = {};
    const t = (key: string) => I18n.t(`identity.serviceDesk.${key}`);
    const day = values.day.trim();
    const month = values.month.trim();
    const year = values.year.trim();
    const currentYear = new Date().getFullYear();
    if (values.lastName.trim() === '') {
        errors.lastName = t('errorLastName');
    }
    if (values.firstName.trim() === '') {
        errors.firstName = t('errorFirstName');
    }
    if (!NUMBER.test(day) || Number(day) < 1 || Number(day) > 31) {
        errors.day = t('errorDay');
    }
    if (!NUMBER.test(month) || Number(month) < 1 || Number(month) > 12) {
        errors.month = t('errorMonth');
    }
    if (
        !/^\d{4}$/.test(year) ||
        Number(year) >= currentYear ||
        Number(year) < currentYear - YEARS_BACK
    ) {
        errors.year = t('errorYear');
    }
    if (!errors.day && !errors.month && !errors.year) {
        if (Number(day) > daysInMonth(Number(month) - 1, Number(year))) {
            errors.date = t('errorDate');
        }
    }
    return errors;
};

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
    const [month, setMonth] = useState(
        initialDate.month ? `${MONTHS.indexOf(initialDate.month) + 1}` : '',
    );
    const [year, setYear] = useState(initialDate.year);
    const [submitted, setSubmitted] = useState(false);

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
            toast.success(I18n.t('toast.controlCodeDeleted'));
            onClose();
        },
    });

    const errors = submitted
        ? validate({ firstName, lastName, day, month, year })
        : {};
    const dateError = errors.day ?? errors.month ?? errors.year ?? errors.date;

    const submit = () => {
        setSubmitted(true);
        const found = validate({ firstName, lastName, day, month, year });
        if (Object.keys(found).length > 0) {
            return;
        }
        create({
            firstName: firstName.trim(),
            lastName: lastName.trim(),
            // the backend expects `<day> <MON> <year>`
            dayOfBirth: `${Number(day)} ${MONTHS[Number(month) - 1]} ${year}`,
        });
    };

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
                    <Field data-invalid={!!errors.lastName}>
                        <FieldLabel htmlFor="controlLastName">
                            {I18n.t('identity.serviceDesk.lastName')}
                        </FieldLabel>
                        <Input
                            id="controlLastName"
                            value={lastName}
                            autoComplete="off"
                            spellCheck={false}
                            aria-invalid={!!errors.lastName}
                            onChange={(e) => setLastName(e.target.value)}
                        />
                        {errors.lastName && (
                            <FieldError>{errors.lastName}</FieldError>
                        )}
                    </Field>
                    <Field data-invalid={!!errors.firstName}>
                        <FieldLabel htmlFor="controlFirstName">
                            {I18n.t('identity.serviceDesk.firstName')}
                        </FieldLabel>
                        <Input
                            id="controlFirstName"
                            value={firstName}
                            autoComplete="off"
                            spellCheck={false}
                            aria-invalid={!!errors.firstName}
                            onChange={(e) => setFirstName(e.target.value)}
                        />
                        {errors.firstName && (
                            <FieldError>{errors.firstName}</FieldError>
                        )}
                    </Field>
                    <div className="service-desk__date">
                        <Field data-invalid={!!errors.day || !!errors.date}>
                            <FieldLabel htmlFor="controlDay">
                                {I18n.t('identity.serviceDesk.day')}
                            </FieldLabel>
                            <Input
                                id="controlDay"
                                value={day}
                                inputMode="numeric"
                                maxLength={2}
                                autoComplete="off"
                                placeholder="DD"
                                aria-invalid={!!errors.day || !!errors.date}
                                onChange={(e) => setDay(e.target.value)}
                            />
                        </Field>
                        <Field data-invalid={!!errors.month || !!errors.date}>
                            <FieldLabel htmlFor="controlMonth">
                                {I18n.t('identity.serviceDesk.month')}
                            </FieldLabel>
                            <Input
                                id="controlMonth"
                                value={month}
                                inputMode="numeric"
                                maxLength={2}
                                autoComplete="off"
                                placeholder="MM"
                                aria-invalid={!!errors.month || !!errors.date}
                                onChange={(e) => setMonth(e.target.value)}
                            />
                        </Field>
                        <Field data-invalid={!!errors.year || !!errors.date}>
                            <FieldLabel htmlFor="controlYear">
                                {I18n.t('identity.serviceDesk.year')}
                            </FieldLabel>
                            <Input
                                id="controlYear"
                                value={year}
                                inputMode="numeric"
                                maxLength={4}
                                autoComplete="off"
                                placeholder="YYYY"
                                aria-invalid={!!errors.year || !!errors.date}
                                onChange={(e) => setYear(e.target.value)}
                            />
                        </Field>
                    </div>
                    {dateError && <FieldError>{dateError}</FieldError>}
                </FieldGroup>
                <Button disabled={creating} onClick={submit}>
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
                    <div className="service-desk__actions">
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
                        <Button variant="outline" onClick={onClose}>
                            {I18n.t('identity.serviceDesk.proceed')}
                        </Button>
                    </div>
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
