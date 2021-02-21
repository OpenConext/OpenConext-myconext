import I18n from "i18n-js";

I18n.translations.nl = {
    login: {
        requestEduId: "Geen eduID?",
        requestEduId2: "Vraag het aan!",
        loginEduId: "Login",
        whatis: "Wat is eduID?",
        header: "Login met eduID",
        headerSubTitle: "om door te gaan naar ",
        header2: "Vraag een eduID aan",
        trust: "Vertrouw deze computer",
        loginOptions: "Andere inlog manieren",
        loginOptionsToolTip: "We bieden drie manieren om in te loggen:</br><ol>" +
            "<li>Je kunt een magische link ontvangen op je e-mailadres.</li>" +
            "<li>Je kunt een wachtwoord gebruiken. Dit dien je eerst in Mijn eduID in te stellen.</li>" +
            "<li>Je kunt een beveiligingssleutel gebruiken. Dit dien je eerst in Mijn eduID in te stellen.</li>" +
            "</ol>",
        email: "Je e-mailadres",
        emailPlaceholder: "bijv. naam@gmail.com",
        passwordPlaceholder: "Wachtwoord",
        familyName: "Achternaam",
        givenName: "Voornaam",
        familyNamePlaceholder: "bijv. Berners-Lee",
        givenNamePlaceholder: "bijv. Tim",
        sendMagicLink: "Mail een magische link",
        loginWebAuthn: "Login in met een beveiligingssleutel",
        usePassword: "type een wachtwoord",
        usePasswordNoWebAuthn: "Type een wachtwoord",
        useMagicLink: "Gebruik magische link",
        useMagicLinkNoWebAuthn: "Gebruik magische link.",
        useWebAuth: "Login in met een beveligingssleutel",
        useOr: " of ",
        requestEduIdButton: "Vraag een eduID aan",
        rememberMe: "Ingelogd blijven",
        password: "Je wachtwoord",
        passwordForgotten: "Wachtwoord vergeten of liever een magische link? ",
        passwordForgottenLink: "Ontvang een e-mail om direct in te loggen.",
        login: "Login",
        create: "Aanmaken",
        newTo: "Voor het eerst bij eduID?",
        createAccount: "Maak een account aan.",
        useExistingAccount: "Gebruik een bestaand account",
        invalidEmail: "Ongeldig e-mailadres",
        requiredAttribute: "{{attr}} is verplicht",
        emailInUse1: "Dit e-mailadres is al in gebruik.",
        emailInUse2: "Probeer een andere, of ",
        emailInUse3: " login met dit eduID account.",
        emailNotFound1: "We konden geen eduID vinden met deze mail.",
        emailNotFound2: "Probeer een andere, of ",
        emailNotFound3: "vraag een nieuw eduID account aan.",
        emailOrPasswordIncorrect: "E-mailadres of wachtwoord is niet juist",
        institutionDomainNameWarning: "Het lijkt erop dat je een instellings e-mailadres hebt ingevoerd. Houd er rekening mee dat wanneer je niet meer studeert of werkt bij die instelling, je geen toegang meer hebt tot dat e-mail adres.",
        institutionDomainNameWarning2: "We raden je aan om je persoonlijke e-mailadres te gebruiken voor eduID.",
        allowedDomainNamesError: "Domeinnaam {{domain}} niet toegestaan.",
        allowedDomainNamesError2: "eduID is beperkt om alleen te worden gebruikt door toegestane domeinen.",
        passwordDisclaimer: "Je wachtwoord moet minimaal 15 karakters lang zijn, of minimaal 8 als het een hoofdletter en een getal bevat.",
        alreadyGuestAccount: "Heb je al een eduID?",
        usePasswordLink: "Gebruik toch een wachtwoord",
        useWebAuthnLink: "Of gebruik een beveiligingssleutel",
        agreeWithTerms: "<span>Ik ga akkoord met <a href='https://eduid.nl/voorwaarden/' target='_blank'>de voorwaarden.</a> En ik begrijp <a href='https://eduid.nl/privacyverklaring/' target='_blank'>de privacyverklaring</a>.</span>"
    },
    magicLink: {
        header: "Open je mailbox!",
        info: "Om in te loggen, klik op de link in de e-mail die we hebben verstuurd naar <strong>{{email}}</strong>.",
        awaiting: "Wachten tot je op de link klikt...",
        openGMail: "Open Gmail.com",
        openOutlook: "Open Outlook.com",
        spam: "Kan je de e-mail niet vinden? Kijk in je spam.",
        loggedIn: "Inloggen geslaagd!",
        loggedInInfo: "Je kan dit tabblad / venster sluiten.",
        timeOutReached: "Timeout!",
        timeOutReachedInfo: "Je magische link is verlopen. Ga terug naar de dienst waar je heen wou en probeer het opnieuw.",
        resend: "E-mail nog steeds niet gevonden?",
        resendLink: " Stuur de e-mail opnieuw.",
        mailResend: "Check je inbox. We hebben je de e-mail met de magische link opnieuw verzonden.",
        loggedInDifferentDevice: "Verificatiecode vereist",
        loggedInDifferentDeviceInInfo: "Je hebt de magische link gebruikt in een andere browser dan van waaruit je de magische link hebt aangevraagd. Als extra veiligheidsmaatregel moet je een verificatiecode invoeren.",
        loggedInDifferentDeviceInInfo2: "We hebben je een extra e-mail gestuurd met de verificatiecode.",
        verify: "Verifïeer"
    },
    confirm: {
        header: "Gelukt!",
        thanks: "Je eduID is geactiveerd. Ga door naar je bestemming."
    },
    confirmStepup: {
        header: "Gelukt!",
        proceed: "Ga naar {{name}}",
        conditionMet: "Je hebt aan alle voorwaarden voldaan."
    },
    stepup: {
        header: "Nog één ding!",
        info: "Om door te gaan naar <strong> {{name}} </strong>, moet je nog aan de volgende voorwaarde(n) voldoen.",
        link: "Verifieer dit via SURFconext"
    },
    footer: {
        privacy: "Privacy",
        terms: "Voorwaarden",
        help: "Help",
        poweredBy: "Powered by"
    },
    success: {
        title: "Inloggen bijna geslaagd!",
        info: "Ga terug naar het scherm waar je de magische link hebt aangevraagd en volg de instructies daar op.<br/><br/>Je kunt dit tabblad / venster sluiten."
    },
    expired: {
        title: "Verlopen magische link",
        info: "De magische link die je hebt gebruikt, is verlopen of al een keer gebruikt",
        back: "Ga naar eduid.nl"
    },
    notFound: {
        title: "Oeps...",
        title2: "Er is iets fout gegaan (404)."
    },
    webAuthn: {
        info: "Voeg een beveligingssleutel toe",
        browserPrompt: "Klik op de onderstaande knop om een beveligingssleutel toe te voegen aan je eduID-account. Volg daarbij de instructies van je browser op.",
        start: "Start"
    },
    webAuthnTest: {
        info: "Test een beveligingssleutel",
        browserPrompt: "Klik op de onderstaande knop om een beveligingssleutel te testen. Volg daarbij de instructies van je browser op.",
        start: "Test"
    },
    migration: {
        header: "Migreer naar <br/>eduID",
        info1: "SURF zal het gebruik van Onegini geleidelijk uitfaseren. Om toegang te behouden, moet je je Onegini-account migreren naar eduID.",
        info2: "Je hoeft slechts op de knop te klikken en je één keer aan te melden met je bestaande Onegini account. We maken vervolgens een nieuw eduID aan en sturen je na voltooiing een e-mail.",
        link: "Start migratie"
    },
    affiliationMissing: {
        header: "Account is gekoppeld, maar...",
        info: "Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de juiste attributen teruggegeven",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}",
        proceedLink: "Doorgaan",
        retryLink: "Opnieuw proberen"
    },
    eppnAlreadyLinked: {
        header: "Account niet gekoppeld!",
        info: "Je eduID kon niet worden gekoppeld, omdat het vertrouwde account waarmee je bent ingelogd, al aan een ander eduID-account is gekoppeld.",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}",
        proceedLink: "Doorgaan",
        retryLink: "Opnieuw proberen"
    },
    validNameMissing: {
        header: "Account is gekoppeld, maar...",
        info: "Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de juiste attributen teruggegeven",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}",
        proceedLink: "Doorgaan",
        retryLink: "Opnieuw proberen"
    },
    stepUpExplanation: {
        linked_institution: "Je eduID-account moet gekoppeld zijn aan een vertrouwde instelling.",
        validate_names: "Je voornaam en achternaam moeten worden geverifieerd door een vertrouwde instelling.",
        affiliation_student: "Je moet aantonen dat je onderwijs volgt door je eduID-account te koppelen aan een vertrouwde instelling."
    },
    stepUpVerification: {
        linked_institution: "Je eduID-account is gekoppeld aan een vertrouwde instelling.",
        validate_names: "Je voornaam en achternaam zijn geverifieerd door een vertrouwde instelling.",
        affiliation_student: "Je hebt aangetoont dat je onderwijs volgt doordat je eduID-account is gekoppeld aan een vertrouwde instelling."
    }

};
