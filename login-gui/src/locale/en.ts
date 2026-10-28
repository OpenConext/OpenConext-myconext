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
    login: {
        title: 'Log in with {{productName}}',
        email: 'Email address',
        accountNotFound:
            'We couldn’t find a {{productName}} account with this email address. Try another one.',
        descriptionWithService: 'to continue to {{serviceName}}',
        description: 'to continue',
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
};

export default en;
