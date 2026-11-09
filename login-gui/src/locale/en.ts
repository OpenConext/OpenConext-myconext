import type { AppTranslation } from './translationType';

const en: AppTranslation = {
    globalVariables: {
        productName: 'myConext',
    },
    code: 'EN',
    languages: {
        language: 'Language',
        en: 'English',
        nl: 'Dutch',
    },
    landing: {
        title: 'What is eduID for?',
        description: 'Your ID for lifelong learning',
        services:
            "Log in to a growing number of education and research services, whether you're a student, staff member, or professional.",
        career: 'Your eduID stays with you throughout your entire career, from your studies to work and courses later in life.',
        control:
            'You always have full insight into and control over your personal data.',
        imageAlt: 'Phone',
        more: 'Learn more about eduID',
    },
    login: {
        title: 'Log in with {{productName}}',
        email: 'Email address',
        accountNotFound:
            'We couldn’t find a {{productName}} account with this email address. Try another one.',
        descriptionWithService: 'to continue to {{serviceName}}',
        description: 'to continue',
        useOtherAccount: 'Use another account',
        loading: 'Loading...',
        submit: 'Continue',
        noAccount: "Don't have a {{productName}} yet?",
        createAccount: 'Create a {{productName}}',
    },
    request: {
        title: 'Create a {{productName}}',
        email: 'Email address',
        givenName: 'First name',
        familyName: 'Last name',
        submit: 'Continue',
        agree: 'I agree with <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/terms-of-use/">the terms of service</a> and I\'ve read <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/privacy">the privacy policy</a>.',
        hasAccount: 'Already have a {{productName}}?',
        login: 'Log in',
    },
    requestSuccess: {
        title: 'Your {{productName}} has been created',
        info: 'We have also sent you a confirmation email.',
        next: 'Next',
    },
    remember: {
        title: 'Stay signed in?',
        info: "You won't need to log in again next time. Only do this on a device you don't share with others.",
        yes: 'Yes, stay signed in',
        no: 'No, not needed',
        error: 'Something went wrong. Please try again.',
    },
    expired: {
        title: 'Your code is invalid',
        info: 'The code you used is either expired or has already been used.',
        back: 'Go to eduid.nl',
    },
    codeScreen: {
        title: 'Log in with {{productName}}',
        description: 'Enter the code sent to {{email}}',
        inputLabel: 'Code',
        submit: 'Continue',
        noEmail: "Didn't receive the email? Check your spam folder, or",
        resend: 'send a new code',
        noAccess: "Can't access your mail?",
        otherMethod: 'Use another login method',
    },
    password: {
        title: 'Log in with {{productName}}',
        descriptionWithService: 'to continue to {{serviceName}}',
        description: 'to continue',
        label: 'Password',
        submit: 'Continue',
        incorrect: 'Password is incorrect',
        error: 'Something went wrong. Please try again.',
        otherMethod: 'Use another login method',
    },
    webAuthn: {
        title: 'Log in with {{productName}}',
        descriptionWithService: 'to continue to {{serviceName}}',
        description: 'to continue',
        explanation:
            'Your device will open a new window. Follow the instructions there to log in.',
        submit: 'Log in with a passkey',
        error: "You can't use the passkey at this moment",
        otherMethod: 'Use another login method',
    },
    addWebAuthn: {
        title: 'Add security key',
        explanation:
            'Press the button below to add a security key to your eduID. Please follow the instructions given by your browser.',
        submit: 'Start',
        error: "The security key couldn't be added. Please try again.",
    },
    useApp: {
        title: 'Check your {{productName}} app',
        description:
            "We've sent a push notification to your app to check that it's you logging in.",
        scanTitle: 'Scan the QR code with your {{productName}} app',
        loading: 'Loading...',
        qrAlt: 'QR code to log in with the {{productName}} app',
        openApp: 'Open the app on this device',
        noNotification: 'No notification?',
        createQr: 'Create a QR code',
        offline: 'Does your phone show a code?',
        enterCode: 'Enter code',
        codeLabel: 'Code from your app',
        codeIncorrect: 'The code is invalid.',
        suspended: 'The verification from your app failed.',
        suspendedWait:
            "You'll have to wait {{minutes}} {{unit}} before you can try again.",
        suspendedRetry: 'You can try again.',
        minute: 'minute',
        minutes: 'minutes',
        startError: 'Something went wrong. Please try again.',
        timeOutTitle: 'Session timed out',
        timeOutInfo: 'Your session timed out.',
        timeOutRetry: 'Try again',
        noAccess: "Can't access the app?",
        reRegister: 'Re-register the app',
        reRegisterUrl: 'https://eduid.nl/help',
        or: 'or',
        otherMethod: 'use another login method',
    },
    options: {
        title: 'Log in with {{productName}}',
        description: 'Choose how you want to log in',
        loading: 'Loading...',
        error: 'Failed to load login options. Please try again.',
        useApp: 'Use the {{productName}} app',
        useWebAuthn: 'Use your security key',
        useCode: 'Get a one-time code by email',
        usePassword: 'Use a password',
    },
    stepup: {
        title: 'Almost there',
        submit: 'Continue',
        proveIdentity: {
            title: 'Prove who you are',
            description:
                'Do you have an account at an educational or research institution?',
            institution: 'Use your institution account',
            alternatives:
                'No institution account? Choose one of the following methods:',
            bank: 'Use your bank',
            european: 'Use a European ID',
            footerQuestion:
                "Don't have a European ID or a bank that supports iDIN?",
            serviceDesk: 'Confirm at a service desk',
            back: 'Back',
            bankTitle: 'Use your bank',
            bankDescription:
                'With iDIN we verify who you are through your bank. You do not transfer any money.',
            bankMoreUrl: 'https://www.idin.nl/en/',
            bankMore: 'More about iDIN',
            bankMissing: 'Is your bank not listed?',
            otherMethod: 'Choose another method',
        },
        validate_names: {
            description:
                'To continue to {{serviceName}}, your first name and last name must be verified by a trusted party.',
            alert: 'Your first name and last name must be verified by a trusted party.',
            footer: "Without verified names you can't access {{serviceName}}.",
        },
        linked_institution: {
            description:
                'To continue to {{serviceName}}, your eduID must be linked to an institution account.',
            alert: 'Connect your eduID to your institution',
            footer: "Without an institution account you can't access {{serviceName}}.",
        },
        affiliation_student: {
            description:
                'To continue to {{serviceName}}, you must prove that you are following education.',
            alert: 'You must prove that you are following education by connecting your eduID account to an official institution.',
            footer: "Without proof of enrolment in education you can't access {{serviceName}}.",
        },
        validate_names_external: {
            description:
                'To continue to {{serviceName}}, your identity must be verified by a trusted party.',
            alert: 'Your identity must be verified by a trusted party.',
            footer: "Without a verified identity you can't access {{serviceName}}.",
        },
        any_link: {
            description:
                'To continue to {{serviceName}}, your eduID must be linked to a trusted party.',
            alert: 'Your eduID account must be linked to a trusted party.',
            footer: "Without a linked account you can't access {{serviceName}}.",
        },
    },
    attributeMissing: {
        title: 'Account not linked!',
        info: [
            'Your eduID could not be linked. The trusted account with which you just logged in did not provide an EPPN.',
            'You can try to link to another institution or proceed to {{serviceName}}.',
        ],
        retry: 'Retry',
        proceed: 'Proceed',
    },
    confirmStepup: {
        title: 'Thanks!',
        conditionMet: 'All conditions are met.',
        submit: 'Go to {{serviceName}}',
        explanation: {
            linked_institution: 'Your eduID is connected to an official party.',
            validate_names:
                'Your first name and last name are verified by an official party.',
            validate_names_external:
                'Your identity is verified by a trusted party.',
            affiliation_student:
                'You have proven that you are following education by connecting your eduID account to an official party.',
            profile_mfa:
                'You have successfully logged in using the eduID app with multi-factor authentication.',
            any_link: 'Your eduID account is linked to a trusted party.',
        },
    },
    appRequired: {
        title: 'Almost there',
        description:
            'To continue to {{serviceName}} you must use the eduID app.',
        alertTitle: 'Secure login with an extra step',
        alertDescription:
            'With the eduID app, you log in extra securely and without a password. Installation is quick and easy.',
        refuse: 'I refuse',
        download: 'Download the eduID app',
        warningTitle: 'Get the eduID app',
        warning:
            'Login without the eduID app is not allowed. You will not be able to log in to {{serviceName}}.',
        confirmRefuse: 'I still refuse',
    },
    getApp: {
        title: 'Activate your eduID app',
        description:
            'Download the eduID app (from SURF) in one of the app stores:',
        google: 'https://play.google.com/store/apps/details?id=nl.eduid',
        googleLabel: 'Google Play',
        apple: 'https://apps.apple.com/us/app/eduid/id1600756434',
        appleLabel: 'App Store',
        qr: 'QR code to install the eduID app',
        after: 'Did you already install the app? Please continue.',
        back: 'Back',
        next: 'Continue',
    },
    enrollApp: {
        scanTitle: 'Finish setup in the eduID app',
        scanDescription: 'Scan this QR code with your eduID app.',
        qrAlt: 'QR code to link the eduID app',
        openApp: 'Open the app on this device',
        finishTitle: 'Finish setup in the eduID app',
        finishDescription:
            'Follow the instructions in the app to finish the setup.',
        timeOutTitle: 'Session timeout',
        timeOutDescription: 'Your session timed out.',
        existingTitle: 'Setup not finished',
        existingDescription:
            'You already started linking the eduID app, but it is not active yet. Continue to set up a recovery method and finish.',
        existingResume: 'Continue setup',
        error: 'Something went wrong. Please try again.',
        retry: 'Try again',
    },
    recovery: {
        title: 'Almost ready',
        description:
            'Add a recovery method. If you can no longer use the eduID app, for example if you lose your mobile phone, you can use a recovery method to register the eduID app again.',
        phoneOption: 'Add a recovery phone number',
        backupOption: 'Request a recovery code',
        backupTitle: 'Keep the code in a safe place',
        backupDescription:
            'This code is currently active, but you can generate a new one at any time.',
        copy: 'Copy the code',
        copied: 'Copied',
        next: 'I saved the code. Continue',
        error: 'Something went wrong. Please try again.',
        retry: 'Try again',
    },
    phoneVerification: {
        title: 'Add a recovery phone number',
        description:
            'Your phone number is only used for security, for example to help you back into your account if you lose your app.',
        back: 'Back',
        label: 'Phone number',
        hint: 'We will text you a code to verify your number.',
        placeholder: '+31 612345678',
        submit: 'Verify this phone number',
        incorrect: 'Phone number is incorrect',
        rateLimited:
            'Too many requests to send an SMS. You are rate limited, please try again later.',
        error: 'Something went wrong. Please try again.',
        finalizedTitle: 'Existing recovery method',
        finalizedDescription:
            'You can not register a phone number, because you already have a recovery code as recovery method.',
        finalizedNext: 'Continue',
    },
    phoneConfirmation: {
        title: 'Check your phone',
        description:
            'Enter the six-digit code we sent to {{phoneNumber}} to continue.',
        inputLabel: 'Verification code',
        submit: 'Continue',
        incorrect: 'The code is incorrect',
        error: 'Something went wrong. Please try again.',
        maxAttempts: 'Maximum attempts reached. Resend the code or',
        changeNumber: 'use a different phone number',
        noCode: 'Did not receive a code?',
        resend: 'Resend code',
        resent: 'We sent you a new code.',
    },
};

export default en;
