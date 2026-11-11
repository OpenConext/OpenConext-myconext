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
    landing: {
        title: 'Waar is eduID voor?',
        description: 'Jouw ID voor een leven lang leren',
        services:
            'Log in bij een groeiend aantal diensten voor onderwijs en onderzoek, als student, medewerker of professional.',
        career: 'Je eduID blijft met je meegaan tijdens je hele loopbaan, van je studie tot werk en cursussen later in je leven.',
        control:
            'Je hebt altijd volledig inzicht in en controle over je persoonlijke gegevens.',
        imageAlt: 'Telefoon',
        more: 'Meer over eduID',
    },
    login: {
        title: 'Inloggen met {{productName}}',
        email: 'E-mailadres',
        accountNotFound:
            'We konden geen {{productName}}-account vinden met dit e-mailadres. Probeer een ander adres.',
        descriptionWithService: 'om door te gaan naar {{serviceName}}',
        description: 'om door te gaan',
        useOtherAccount: 'Gebruik een ander account',
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
    success: {
        title: 'Inloggen is bijna klaar!',
        info: [
            'Ga terug naar het scherm waar je de link hebt aangevraagd en volg de instructies daar op.',
            'Je kunt dit tabblad / venster sluiten.',
        ],
    },
    remember: {
        title: 'Ingelogd blijven?',
        info: 'Je hoeft de volgende keer niet opnieuw in te loggen. Doe dit alleen op een apparaat dat je niet met anderen deelt.',
        yes: 'Ja, ingelogd blijven',
        no: 'Nee, niet nodig',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
    },
    expired: {
        title: 'Je code is niet geldig',
        info: 'De code die je hebt gebruikt, is verlopen of al een keer gebruikt.',
        back: 'Ga naar eduid.nl',
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
    addWebAuthn: {
        title: 'Voeg een beveiligingssleutel toe',
        explanation:
            'Druk op de knop hieronder om een beveiligingssleutel toe te voegen aan je eduID. Volg daarbij de instructies van je browser op.',
        submit: 'Start',
        error: 'De beveiligingssleutel kon niet worden toegevoegd. Probeer het opnieuw.',
    },
    useApp: {
        title: 'Controleer je {{productName}} app',
        description:
            'We hebben een pushmelding naar je app gestuurd om te controleren dat jij het bent die inlogt.',
        scanTitle: 'Scan de QR-code met je {{productName}} app',
        loading: 'Laden...',
        qrAlt: 'QR-code om in te loggen met de {{productName}} app',
        openApp: 'Open de app op dit apparaat',
        noNotification: 'Geen melding?',
        createQr: 'Maak een QR-code',
        offline: 'Toont je telefoon een code?',
        enterCode: 'Code invoeren',
        codeLabel: 'Code uit je app',
        codeIncorrect: 'De code is niet juist.',
        suspended: 'De controle van je app is mislukt.',
        suspendedWait:
            'Je moet {{minutes}} {{unit}} wachten voordat je het opnieuw kunt proberen.',
        suspendedRetry: 'Je kunt het opnieuw proberen.',
        minute: 'minuut',
        minutes: 'minuten',
        startError: 'Er is iets misgegaan. Probeer het opnieuw.',
        timeOutTitle: 'Sessie verlopen',
        timeOutInfo: 'Je sessie is verlopen.',
        timeOutRetry: 'Probeer het opnieuw',
        noAccess: 'Geen toegang tot de app?',
        reRegister: 'Registreer de app opnieuw',
        reRegisterUrl: 'https://eduid.nl/help',
        or: 'of',
        otherMethod: 'gebruik een andere inlogmethode',
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
    stepup: {
        title: 'Je bent er bijna',
        submit: 'Ga door',
        proveIdentity: {
            title: 'Bewijs wie je bent',
            description:
                'Heb je een account bij een onderwijs- of onderzoeksinstelling?',
            institution: 'Gebruik je instellingsaccount',
            alternatives:
                'Geen instellingsaccount? Kies dan een van de volgende methoden:',
            bank: 'Gebruik je bank',
            european: 'Gebruik een Europees ID',
            footerQuestion:
                'Heb je geen Europees ID of bank die iDIN ondersteunt?',
            serviceDesk: 'Bevestig bij een servicedesk',
            back: 'Terug',
            bankTitle: 'Gebruik je bank',
            bankDescription:
                'Met iDIN controleren we via de bank wie je bent. Je maakt geen geld over.',
            bankMoreUrl: 'https://www.idin.nl/',
            bankMore: 'Meer over iDIN',
            bankMissing: 'Staat je bank er niet tussen?',
            otherMethod: 'Kies een andere methode',
        },
        validate_names: {
            description:
                'Om door te gaan naar {{serviceName}} moeten je voornaam en achternaam zijn geverifieerd door een vertrouwde instelling.',
            alert: 'Je voornaam en achternaam moeten worden geverifieerd door een vertrouwde instelling.',
            footer: 'Zonder geverifieerde naam heb je geen toegang tot {{serviceName}}.',
        },
        linked_institution: {
            description:
                'Om door te gaan naar {{serviceName}} moet je eduID gekoppeld zijn aan een instellingsaccount.',
            alert: 'Koppel je eduID aan een instelling',
            footer: 'Zonder instellingsaccount heb je geen toegang tot {{serviceName}}.',
        },
        affiliation_student: {
            description:
                'Om door te gaan naar {{serviceName}} moet je aantonen dat je onderwijs volgt.',
            alert: 'Je moet aantonen dat je student bent. Dit doe je door je eduID te koppelen aan een officiële instelling.',
            footer: 'Zonder bewijs dat je onderwijs volgt heb je geen toegang tot {{serviceName}}.',
        },
        validate_names_external: {
            description:
                'Om door te gaan naar {{serviceName}} moet je identiteit zijn geverifieerd door een vertrouwde partij.',
            alert: 'Je identiteit moet worden geverifieerd door een externe vertrouwde instelling.',
            footer: 'Zonder geverifieerde identiteit heb je geen toegang tot {{serviceName}}.',
        },
        any_link: {
            description:
                'Om door te gaan naar {{serviceName}} moet je eduID gekoppeld zijn aan een vertrouwde partij.',
            alert: 'Je eduID-account moet gekoppeld zijn aan een vertrouwde partij.',
            footer: 'Zonder gekoppeld account heb je geen toegang tot {{serviceName}}.',
        },
    },
    attributeMissing: {
        title: 'Account niet gekoppeld!',
        info: [
            'Je eduID kon niet worden gekoppeld. Het vertrouwde account waarmee je zojuist bent ingelogd, heeft geen EPPN vrijgegeven.',
            'Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{serviceName}}.',
        ],
    },
    linkOutcome: {
        retry: 'Opnieuw proberen',
        proceed: 'Doorgaan',
    },
    affiliationMissing: {
        title: 'Account is gekoppeld, maar…',
        info: [
            'Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de vereiste persoonsgegevens teruggegeven.',
            'Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{serviceName}}.',
        ],
    },
    validNameMissing: {
        title: 'Account is gekoppeld, maar…',
        info: [
            'Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de vereiste persoonsgegevens teruggegeven.',
            'Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{serviceName}}.',
        ],
    },
    eppnAlreadyLinked: {
        title: 'Account niet gekoppeld!',
        info: [
            'Je eduID kon niet worden gekoppeld. Het vertrouwde account waarmee je zojuist bent ingelogd, is al aan een ander eduID-account gekoppeld: {{email}}.',
            'Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{serviceName}}.',
        ],
    },
    subjectAlreadyLinked: {
        title: 'Account niet geverifieerd!',
        info: [
            'Je eduID kon niet worden geverifieerd. Het externe account waarmee je zojuist bent ingelogd, is al aan een ander eduID-account gekoppeld: {{email}}.',
            'Je kan nogmaals proberen je account te verifiëren of doorgaan naar {{serviceName}}.',
        ],
    },
    iapAssuranceMissing: {
        title: 'Account is gekoppeld, maar…',
        levelHigh: 'Hoog',
        levelMedium: 'Midden',
        info: [
            'Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet het vereiste betrouwbaarheidsniveau {{level}} teruggegeven.',
            'Je kan proberen je identiteit via een andere vertrouwde partij te verifiëren of doorgaan naar {{serviceName}}.',
        ],
    },
    confirmStepup: {
        title: 'Gelukt!',
        conditionMet: 'Je hebt aan alle voorwaarden voldaan.',
        submit: 'Ga naar {{serviceName}}',
        explanation: {
            linked_institution:
                'Je eduID is gekoppeld aan een officiële instelling.',
            validate_names:
                'Je voornaam en achternaam zijn bevestigd door een officiële instelling.',
            validate_names_external:
                'Je voornaam en achternaam moeten worden geverifieerd door een externe vertrouwde instelling.',
            affiliation_student:
                'Je hebt aangetoond dat je student bent, doordat je eduID is gekoppeld aan een officiële instelling.',
            profile_mfa:
                'Je bent succesvol ingelogd met de eduID app met multi-factor authenticatie.',
            any_link:
                'Je eduID-account is gekoppeld aan een vertrouwde partij.',
        },
    },
    appRequired: {
        title: 'Je bent er bijna',
        description:
            'Om door te gaan naar {{serviceName}} moet je de eduID-app gebruiken.',
        alertTitle: 'Veilig inloggen met een extra stap',
        alertDescription:
            'Met de eduID-app log je extra veilig in, zonder wachtwoord. Het installeren is zo gebeurd.',
        refuse: 'Ik weiger',
        download: 'Download de eduID-app',
        warningTitle: 'Download eduID-app',
        warning:
            'Doorgaan zonder de eduID-app is niet toegestaan. Je kunt dan niet inloggen op {{serviceName}}.',
        confirmRefuse: 'Ik weiger echt',
    },
    getApp: {
        title: 'Activeer je eduID-app',
        description:
            'Download de eduID-app (van SURF) in een van de app-stores:',
        google: 'https://play.google.com/store/apps/details?id=nl.eduid',
        googleLabel: 'Google Play',
        apple: 'https://apps.apple.com/us/app/eduid/id1600756434',
        appleLabel: 'App Store',
        qr: 'QR-code om de eduID-app te installeren',
        after: 'Heb je de app al geïnstalleerd? Ga dan door.',
        back: 'Terug',
        next: 'Ga door',
    },
    enrollApp: {
        scanTitle: 'Voltooi de installatie in de eduID-app',
        scanDescription: 'Scan deze QR-code met je eduID-app.',
        qrAlt: 'QR-code om de eduID-app te koppelen',
        openApp: 'Open de app op dit apparaat',
        finishTitle: 'Voltooi de installatie in de eduID-app',
        finishDescription:
            'Volg de instructies in de app om de installatie te voltooien.',
        timeOutTitle: 'Sessie verlopen',
        timeOutDescription: 'Je sessie is verlopen.',
        existingTitle: 'Installatie niet afgerond',
        existingDescription:
            'Je bent al begonnen met het koppelen van de eduID-app, maar de app is nog niet actief. Ga door om een herstelmethode in te stellen en af te ronden.',
        existingResume: 'Installatie afronden',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
        retry: 'Probeer opnieuw',
    },
    recovery: {
        title: 'Bijna klaar',
        description:
            'Voeg een herstelmethode toe. Als je de eduID-app niet meer kunt gebruiken, bijvoorbeeld bij verlies van je mobiele telefoon, kun je met een herstelmethode de eduID-app opnieuw registreren.',
        phoneOption: 'Hersteltelefoonnummer toevoegen',
        backupOption: 'Herstelcode aanvragen',
        backupTitle: 'Bewaar de code op een veilige plek',
        backupDescription:
            'Deze code is nu actief, maar je kunt op elk moment een nieuwe genereren.',
        copy: 'Kopieer de code',
        copied: 'Gekopieerd',
        next: 'Ik heb de code bewaard. Doorgaan',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
        retry: 'Probeer opnieuw',
    },
    phoneVerification: {
        title: 'Voeg een hersteltelefoonnummer toe',
        description:
            'Je telefoonnummer wordt alleen gebruikt voor beveiliging, bijvoorbeeld om weer toegang te krijgen tot je account als je de app kwijtraakt.',
        back: 'Terug',
        label: 'Telefoonnummer',
        hint: 'We sturen je een code om je nummer te verifiëren.',
        placeholder: '+31 612345678',
        submit: 'Verifieer dit telefoonnummer',
        incorrect: 'Telefoonnummer is onjuist',
        rateLimited:
            'Te veel verzoeken om een sms te versturen. Je bent beperkt, probeer het later opnieuw.',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
        finalizedTitle: 'Bestaande herstelmethode',
        finalizedDescription:
            'Je kunt geen telefoonnummer registreren, omdat je al een herstelcode als herstelmethode hebt.',
        finalizedNext: 'Doorgaan',
    },
    phoneConfirmation: {
        title: 'Controleer je telefoon',
        description:
            'Voer de zescijferige code in die we naar {{phoneNumber}} hebben gestuurd.',
        inputLabel: 'Verificatiecode',
        submit: 'Doorgaan',
        incorrect: 'De code is onjuist',
        error: 'Er is iets misgegaan. Probeer het opnieuw.',
        maxAttempts:
            'Maximaal aantal pogingen bereikt. Verstuur de code opnieuw of',
        changeNumber: 'gebruik een ander telefoonnummer',
        noCode: 'Geen code ontvangen?',
        resend: 'Code opnieuw versturen',
        resent: 'We hebben je een nieuwe code gestuurd.',
    },
};

export default nl;
