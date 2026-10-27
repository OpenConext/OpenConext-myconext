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
