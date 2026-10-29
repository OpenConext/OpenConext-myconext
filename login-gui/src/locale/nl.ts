import type { AppTranslation } from './translationType';

const nl: AppTranslation = {
    globalVariables: {
        productName: 'myConext',
    },
    code: 'NL',
    languages: {
        language: 'Taal',
        en: 'Engels',
        nl: 'Nederlands',
    },
    login: {
        title: 'Inloggen met {{productName}}',
        email: 'E-mailadres',
        accountNotFound:
            'We konden geen {{productName}}-account vinden met dit e-mailadres. Probeer een ander adres.',
        descriptionWithService: 'om door te gaan naar {{serviceName}}',
        description: 'om door te gaan',
        loading: 'Laden...',
        submit: 'Ga door',
        noAccount: 'Heb je nog geen {{productName}}?',
        createAccount: 'Maak een {{productName}} aan',
    },
    request: {
        title: 'Maak een {{productName}} aan',
        email: 'E-mailadres',
        givenName: 'Voornaam',
        familyName: 'Achternaam',
        submit: 'Ga door',
        agree: 'Ik ga akkoord met <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/gebruiksvoorwaarden/">de voorwaarden</a> en ik heb <a target="_blank" rel="noopener noreferrer" href="https://eduid.nl/privacyverklaring/">de privacyverklaring</a> gelezen.',
        hasAccount: 'Heb je al een {{productName}}?',
        login: 'Inloggen',
    },
    requestSuccess: {
        title: 'Je {{productName}} is aangemaakt',
        info: 'We hebben je ook een bevestigingsmail gestuurd.',
        next: 'Volgende',
    },
    codeScreen: {
        title: 'Log in met {{productName}}',
        description: 'Vul de code in die je hebt ontvangen op {{email}}',
        inputLabel: 'Code',
        submit: 'Ga door',
        noEmail: 'Heb je de e-mail niet ontvangen? Controleer je spam, of',
        resend: 'stuur een nieuwe code',
        noAccess: 'Kun je niet bij je e-mail?',
        otherMethod: 'Gebruik een andere inlogmethode',
    },
    password: {
        title: 'Log in met {{productName}}',
        descriptionWithService: 'om door te gaan naar {{serviceName}}',
        description: 'om door te gaan',
        label: 'Wachtwoord',
        submit: 'Ga door',
        incorrect: 'Wachtwoord is onjuist',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
        otherMethod: 'Gebruik een andere inlogmethode',
    },
    webAuthn: {
        title: 'Log in met {{productName}}',
        descriptionWithService: 'om door te gaan naar {{serviceName}}',
        description: 'om door te gaan',
        explanation:
            'Je apparaat opent een nieuw venster. Volg de instructies daar om in te loggen.',
        submit: 'Inloggen met een passkey',
        error: 'Je kunt de passkey op dit moment niet gebruiken',
        otherMethod: 'Gebruik een andere inlogmethode',
    },
    options: {
        title: 'Log in met {{productName}}',
        description: 'Kies hoe je wilt inloggen',
        loading: 'Laden...',
        error: 'Inlogopties laden mislukt. Probeer het opnieuw.',
        useApp: 'Gebruik de {{productName}} app',
        useWebAuthn: 'Gebruik je beveiligingssleutel',
        useCode: 'Ontvang een eenmalige code per e-mail',
        usePassword: 'Gebruik een wachtwoord',
    },
};

export default nl;
