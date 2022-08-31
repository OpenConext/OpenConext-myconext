import I18n from "i18n-js";

I18n.translations.nl = {
    login: {
        requestEduId: "Geen eduID?",
        requestEduId2: "Maak het aan!",
        loginEduId: "Login",
        whatis: "Wat is eduID?",
        header: "Login met eduID",
        headerSubTitle: "om door te gaan naar ",
        header2: "Vraag een eduID aan",
        trust: "Vertrouw deze computer",
        loginOptions: "Andere inlogmanieren",
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
        usePassword: "typ een wachtwoord",
        usePasswordNoWebAuthn: "Typ een wachtwoord",
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
        emailNotFound3: "maak een nieuw eduID account aan.",
        emailOrPasswordIncorrect: "E-mailadres of wachtwoord is niet juist",
        institutionDomainNameWarning: "Het lijkt erop dat je een instellings e-mailadres hebt ingevoerd. Houd er rekening mee dat wanneer je niet meer studeert of werkt bij die instelling, je geen toegang meer hebt tot dat e-mail adres.",
        institutionDomainNameWarning2: "We raden je aan om je persoonlijke e-mailadres te gebruiken voor eduID.",
        allowedDomainNamesError: "Domeinnaam {{domain}} niet toegestaan.",
        allowedDomainNamesError2: "eduID is beperkt om alleen te worden gebruikt door toegestane domeinen.",
        passwordDisclaimer: "Je wachtwoord moet minimaal 15 karakters lang zijn, of minimaal 8 als het een hoofdletter en een getal bevat.",
        alreadyGuestAccount: "Heb je al een eduID?",
        usePasswordLink: "Gebruik toch een wachtwoord",
        useWebAuthnLink: "Of gebruik een beveiligingssleutel",
        agreeWithTerms: "<span>Ik ga akkoord met <a tabindex='-1' href='https://eduid.nl/gebruiksvoorwaarden/' target='_blank'>de voorwaarden.</a> En ik begrijp <a tabindex='-1' href='https://eduid.nl/privacyverklaring/' target='_blank'>de privacyverklaring</a>.</span>",
        next: "Volgende",
        useOtherAccount: "Gebruik een andere login",
        noAppAccess: "Heb je de app niet bij de hand?",
        noMailAccess: "Kun je niet bij je email?",
        forgotPassword: "Wachtwoord vergeten?",
        useAnother: "Gebruik een andere",
        optionsLink: "inlogmethode.",
    },
    options: {
        header: "Hoe wil je inloggen?",
        noLogin: "Kun je nog niet inloggen?",
        learn: "Zie hoe je jouw",
        learnLink: "account kunt herstellen",
        useApp: "Gebruik de <strong>eduID app</strong> om in te loggen met je mobiel.",
        useWebAuthn: "Gebruik je <strong>beveiligingssleuten</strong>.",
        useLink: "Ontvang een <strong>magische link</strong> in je inbox.",
        usePassword: "Gebruik <strong>een wachtwoord</strong>.",
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
        verificationCodeError: "Verkeerde verificatiecode.",
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
        info: "Om door te gaan naar <strong>{{name}}</strong>, moet je nog aan de volgende voorwaarde(n) voldoen.",
        link: "Verifieer dit via SURFconext",
        linkExternalValidation: "Verifieer dit via ReadID"
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
    maxAttempt: {
        title: "Maximum aantal pogingen bereikt",
        info: "Je hebt het maximale aantal verificatiepogingen bereikt.",
    },
    notFound: {
        title: "Oeps...",
        title2: "Er is iets fout gegaan (404)."
    },
    webAuthn: {
        info: "Voeg een beveligingssleutel toe",
        browserPrompt: "Klik op de onderstaande knop om een beveligingssleutel toe te voegen aan je eduID-account. Volg daarbij de instructies van je browser op.",
        start: "Start",
        header: "Log in met een beveiligingssleutel",
        explanation: "Er wordt een nieuw venster geopend. Volg de instricties om in te loggen.",
        next: "Log in met een beveiligingssleutel",
        error: "De beveligingssleutel kan niet gebruikt worden."
    },
    useLink: {
        header: "Vraag een magische link aan",
        next: "Email een magische link"
    },
    usePassword: {
        header: "Voer je wachtwoord in",
        passwordIncorrect: "Wachtwoord is onjuist"
    },
    useApp: {
        header: "Controleer je eduID-app",
        info: "We hebben een pushmelding naar je app gestuurd om te verifiëren dat jij het bent die probeert in te loggen.",
        scan: "Scan deze QR-code met je eduID app",
        noNotification: "Geen melding?",
        qrCodeLink: "Maak een QR-code",
        qrCodePostfix: "en scan deze.",
        offline: "Wanneer je apparaat offline is, moet je een invoeren ",
        offlineLink: "eenmalige code.",
        lost: "Je app verloren?",
        lostLink: "Lees op <a href=\"https://eduid.nl/help\" target=\"_blank\">hoe je een nieuwe moet registreren</a>.",
        timeOut: "Sessie timeout",
        timeOutInfoFirst: "Je sessie is verlopen. Klik op deze ",
        timeOutInfoLast: " om het opnieuw te proberen.",
        timeOutInfoLink: "link",
        responseIncorrect: "De code is niet juist."
    },
    enrollApp: {
        header: "Voltooi de installatie in de eduID app"
    },
    recovery: {
        header: "Een APP-herstelmethode instellen",
        info: "Als je geen toegang hebt tot eduID met de app of via e-mail, kun je een herstelmethode gebruiken om in te loggen op je eduID-account.",
        methods: "De volgende methoden zijn beschikbaar.",
        phoneNumber: "Voeg een hersteltelefoonnummer toe.",
        phoneNumberInfo: "Je ontvangt een sms met een code.",
        backupCode: "Vraag een back-upcode aan.",
        backupCodeInfo: "De code kan worden gebruikt om in te loggen.",
        save: "Bewaar de code ergens veilig.",
        active: "Deze code is nu actief, maar je kan op elk moment een nieuwe code aanmaken binnen mijn eduID.",
        copy: "Kopieer de code",
        copied: "Gekopieerd",
        continue: "Mijn code is veilig. Doorgaan",
    },
    phoneVerification: {
        header: "Voeg een herstel telefoonnummer toe",
        info: "Je telefoonnummer wordt gebruikt voor veiligheidsdoeleinden, zoals u helpen om weer toegang te krijgen tot uw account als u uw app ooit kwijtraakt.",
        text: "We sturen je een code om je nummer te verifiëren.",
        verify: "Verifieer dit telefoonnummer",
        placeHolder: "+31 612345678",
        phoneIncorrect: "Verificatie code is onjuist"
    },
    congrats: {
        header: "Succes",
        info: "Je kunt nu de eduID app gebruiken om snel in te loggen bij diensten waarvoor je moet inloggen met je eduID.",
        next: "Verder naar {{name}}"
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
        info: "Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de juiste attributen teruggegeven.",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}.",
        proceedLink: "Doorgaan",
        retryLink: "Opnieuw proberen"
    },
    eppnAlreadyLinked: {
        header: "Account niet gekoppeld!",
        info: "Je eduID kon niet worden gekoppeld. Het vertrouwde account waarmee je zojuist bent ingelogd, is al aan een ander eduID-account gekoppeld: {{email}}.",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}.",
        proceedLink: "Doorgaan",
        retryLink: "Opnieuw proberen"
    },
    validNameMissing: {
        header: "Account is gekoppeld, maar...",
        info: "Je eduID is succesvol gekoppeld, maar de instelling die je hebt gekozen heeft niet de juiste attributen teruggegeven.",
        proceed: "Je kan het nogmaals met een andere instelling proberen of doorgaan naar {{name}}.",
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
    },
    nudgeApp: {
        new: "J eduID is aangemaakt!",
        header: "Wil je de volgende keer sneller en veiliger inloggen?",
        info: "Installeer de <strong>eduID app</strong> en log veiliger in zonder wachtwoord of het openen van je email. Het kost je maar een minuut.",
        no: "Nee bedankt",
        noLink: "/proceed",
        yes: "Installeer nu",
        yesLink: "/eduid-app"
    },
    getApp: {
        header: "Download de eduID app",
        info: "Download en installeer <a href=\"https://eduid.nl/help\" target=\"_blank\">de eduID app</a> (uitgegeven door SURF) op je mobiele apparaat.",
        google: "https://play.google.com/store/apps/details?id=nl.eduid",
        apple: "https://apps.apple.com/",
        after: "Als je de eduID app op je telefoon hebt gedownload, kom dan hier terug en klik op volgende.",
        back: "Terug",
        next: "Volgende"
    },
    sms: {
        header: "Controleer je telefoon",
        info: "Voer de zescijferige code in die we naar je telefoon hebben gestuurd om door te gaan.",
        codeIncorrect: "De code is onjuist",
        sendSMSAgain: "Nieuwe code",
        maxAttemptsPre: "Maximum aantal pogingen bereikt. Klik",
        maxAttemptsPost: "om een ander telefoonnummer te gebruiken of de verstuur een nieuwe code",
        here: " hier "

    },
    rememberMe: {
        yes: "Ja",
        no: "Nee",
        header: "Ingelogd blijven?",
        info: "Blijf ingelogd op dit apparaat zodat je de volgende niet hoeft in te loggen."
    }

};
