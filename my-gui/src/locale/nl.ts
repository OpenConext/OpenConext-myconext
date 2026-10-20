import type { AppTranslation } from './translationType';

const nl: AppTranslation = {
    globalVariables: {
        productName: 'eduID',
    },
    code: 'NL',
    languages: {
        language: 'Taal',
        en: 'Engels',
        nl: 'Nederlands',
        pt: 'Portugees',
    },
    common: {
        loading: 'Laden...',
        error: 'Er is iets misgegaan',
    },
    logo: {
        eduid: 'eduID',
        surf: 'SURF',
    },
    navigation: {
        home: 'Home',
        identity: 'Identiteit',
        institutions: 'Instellingen',
        security: 'Beveiliging',
        activity: 'Activiteit',
    },
    userMenu: {
        account: 'Account',
        darkMode: 'Donkere modus',
        lightMode: 'Lichte modus',
        logout: 'Uitloggen',
    },
    footer: {
        privacy: 'Privacy & voorwaarden',
        privacyUrl: 'https://eduid.nl/privacy/',
        about: 'Over eduID',
        aboutUrl: 'https://eduid.nl',
    },
    home: {
        title: 'Welkom {{name}}',
        lead: 'Hier bekijk en wijzig je je gegevens, bepaal je hoe je inlogt en beheer je aan welke instellingen en diensten je eduID gekoppeld is.',
        intro: {
            title: 'Wat is eduID?',
            description:
                'eduID is jouw persoonlijke account waarmee je inlogt bij onderwijs- en onderzoeksinstellingen in Nederland, of je nu student, medewerker of professional bent. Je eduID blijft van jou, ook als je van instelling of baan verandert.',
            dismiss: 'Begrepen, verbergen',
        },
        cards: {
            identity: {
                title: 'Identiteit',
                unverified: 'Nog niet bevestigd',
                verified: 'Bevestigd',
            },
            institutions: {
                title: 'Instellingen',
                none: 'Nog geen koppeling',
                count: {
                    one: '{{count}} koppeling',
                    other: '{{count}} koppelingen',
                },
            },
            security: {
                title: 'Beveiliging',
                none: 'Geen inlogmethode ingesteld',
                count: {
                    one: 'Eén inlogmethode ingesteld',
                    other: '{{count}} inlogmethodes ingesteld',
                },
            },
            activity: {
                title: 'Activiteit',
            },
        },
        activity: {
            empty: 'Nog geen activiteit',
            title: 'Recente activiteit',
            viewAll: 'Bekijk alle activiteit',
        },
    },
    activity: {
        all: 'Alle activiteit',
        events: {
            ACCOUNT_CREATED: 'Account aangemaakt',
            NAME_UPDATED: 'Naam gewijzigd',
            LANGUAGE_CHANGED: 'Taal gewijzigd',
            EMAIL_CHANGED: 'E-mailadres gewijzigd',
            PASSWORD_ADDED: 'Wachtwoord toegevoegd',
            PASSWORD_UPDATED: 'Wachtwoord gewijzigd',
            PASSWORD_DELETED: 'Wachtwoord verwijderd',
            PASSKEY_ADDED: 'Passkey toegevoegd',
            PASSKEY_RENAMED: 'Passkey hernoemd',
            PASSKEY_DELETED: 'Passkey verwijderd',
            APP_ACTIVATED: 'eduID-app geactiveerd',
            APP_DEACTIVATED: 'eduID-app gedeactiveerd',
            RECOVERY_METHOD_CHANGED: 'Herstelmethode gewijzigd',
            INSTITUTION_LINKED: 'Instelling gekoppeld',
            INSTITUTION_REMOVED: 'Instelling verwijderd',
            PREFERRED_INSTITUTION_CHANGED: 'Voorkeursinstelling gewijzigd',
            IDENTITY_VERIFIED: 'Identiteit bevestigd',
            IDENTITY_REMOVED: 'Bevestigde identiteit verwijderd',
            CONTROL_CODE_CREATED:
                'Controlecode voor de servicedesk aangevraagd',
            CONTROL_CODE_DELETED: 'Controlecode voor de servicedesk verwijderd',
            SERVICE_REMOVED: 'Inloggegevens van een dienst verwijderd',
            TOKENS_REMOVED: 'Toegang van een dienst ingetrokken',
        },
    },
    pages: {
        identity: 'Identiteit',
        identityEditEmail: 'E-mailadres wijzigen',
        identityEppnAlreadyLinked: 'Instelling is al gekoppeld',
        identityAttributeMissing: 'Attribuut ontbreekt',
        identitySubjectAlreadyLinked: 'Account is al gekoppeld',
        identityExternalAccountLinkedError: 'Koppelen van het account mislukt',
        institutions: 'Instellingen',
        security: 'Beveiliging',
        securityCredential: 'Passkey',
        securityWebauthn: 'Passkey toevoegen',
        securityPassword: 'Wachtwoord',
        securityApp: 'eduID-app',
        activity: 'Activiteit',
        account: 'Account',
        accountDelete: 'Account verwijderen',
        updateEmail: 'E-mailadres bevestigen',
        installApp: 'De eduID-app installeren',
        createFromInstitution: 'eduID aanmaken via instelling',
        createFromInstitutionStep: 'eduID aanmaken via instelling',
    },
    landing: {
        logout: 'Je bent uitgelogd',
        delete: 'Je account is verwijderd',
        ratelimit: 'Te veel pogingen',
        none: 'Pagina niet gevonden',
    },
    notFound: {
        title: '404 - Pagina niet gevonden',
    },
};

export default nl;
