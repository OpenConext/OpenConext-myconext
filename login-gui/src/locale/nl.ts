import type { AppTranslation } from './translationType';

const nl: AppTranslation = {
    globalVariables: {
        productName: 'OpenConext Access',
    },
    code: 'NL',
    languages: {
        language: 'Taal',
        en: 'Engels',
        nl: 'Nederlands',
    },
    login: {
        title: 'Inloggen met myConext',
        email: 'E-mailadres',
        accountNotFound:
            'We konden geen eduID vinden met dit e-mailadres. Probeer een ander adres.',
        descriptionWithService: 'om door te gaan naar {{serviceName}}',
        description: 'om door te gaan',
        loading: 'Laden...',
        submit: 'Ga door',
        noAccount: 'Heb je nog geen eduID?',
        createAccount: 'Maak een eduID aan',
    },
    codeScreen: {
        title: 'Log in met eduID',
        description: 'Vul de code in die je hebt ontvangen op {{email}}',
        inputLabel: 'Code',
        submit: 'Ga door',
        noEmail: 'Heb je de e-mail niet ontvangen? Controleer je spam, of',
        resend: 'stuur een nieuwe code',
        noAccess: 'Kun je niet bij je e-mail?',
        otherMethod: 'Gebruik een andere inlogmethode',
    },
};

export default nl;
