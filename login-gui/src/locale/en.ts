import type { AppTranslation } from './translationType';

const en: AppTranslation = {
    globalVariables: {
        productName: 'OpenConext Access',
    },
    code: 'EN',
    languages: {
        language: 'Language',
        en: 'English',
        nl: 'Dutch',
    },
    login: {
        title: 'Log in with myConext',
        email: 'Email address',
        accountNotFound:
            'We couldn’t find an eduID with this email address. Try another one.',
        descriptionWithService: 'to continue to {{serviceName}}',
        description: 'to continue',
        loading: 'Loading...',
        submit: 'Continue',
        noAccount: "Don't have an eduID yet?",
        createAccount: 'Create an eduID',
    },
    request: {
        title: 'Create an eduID',
        email: 'Email address',
        givenName: 'First name',
        familyName: 'Last name',
        submit: 'Continue',
        agree: 'I agree with <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/terms-of-use/">the terms of service</a> and I\'ve read <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/privacy">the privacy policy</a>.',
        hasAccount: 'Already have an eduID?',
        login: 'Log in',
    },
    codeScreen: {
        title: 'Log in with eduID',
        description: 'Enter the code sent to {{email}}',
        inputLabel: 'Code',
        submit: 'Continue',
        noEmail: "Didn't receive the email? Check your spam folder, or",
        resend: 'send a new code',
        noAccess: "Can't access your mail?",
        otherMethod: 'Use another login method',
    },
};

export default en;
