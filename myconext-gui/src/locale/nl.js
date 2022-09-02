import I18n from "i18n-js";

I18n.translations.nl = {
    sidebar: {
        home: "Home",
        personalInfo: "Persoonlijke info",
        dataActivity: "Data & activiteit",
        security: "Beveiliging",
        account: "Account"
    },
    start: {
        hi: "Hi {{name}}!",
        manage: "Beheer jouw persoonlijke informatie, jouw privacy, en de beveiliging van jouw eduID account."
    },
    header: {
        title: "eduID",
        logout: "Uitloggen"
    },
    landing: {
        logoutTitle: "Je bent uitgelogd",
        logoutStatus: "Om het uitlogproces te voltooien, moet je de browser nu afsluiten.",
        deleteTitle: "Jouw eduID is verwijderd",
        deleteStatus: "Om het verwijderen van je eduID te voltooien, moet je jouw browser nu afsluiten."
    },
    notFound: {
        title: "Oeps...",
        title2: "Er is iets fout gegaan (404)."
    },
    profile: {
        title: "Persoonlijke informatie",
        info: "Wanneer je eduID gebruikt om in te loggen op andere websites, kan jouw persoonlijke informatie worden gedeeld. Sommige websites vereisen dat je persoonlijke gegevens worden gevalideerd door een derde partij.",
        basic: "Basis informatie",
        email: "E-mail",
        name: "Naam",
        validated: "Gevalideerde informatie",
        firstAndLastName: "Voor- en achternaam",
        firstAndLastNameInfo: "Jouw voor- en achternaam zijn nog niet geverifieerd door een derde partij.",
        verify: "Verifïeer",
        student: "Bewijs van studeren",
        studentInfo: "Je hebt nog niet bewezen dat je in Nederland een studie volgt.",
        prove: "Bewijs",
        trusted: "Koppeling met vertrouwde partij",
        trustedInfo: "Je eduID account is nog niet gekoppeld aan een vertrouwde partij.",
        link: "Koppel",
        institution: "Instelling",
        affiliations: "Betrekking(en)",
        expires: "Koppeling verloopt",
        expiresValue: "{{date}}",
        verifiedAt: "Geverifieerd door <strong>{{name}}</strong> op {{date}}",
        proceed: "Doorgaan",
        verifyFirstAndLastName: {
            addInstitution: "Verifieer naam",
            addInstitutionConfirmation: "Als je doorgaat word je gevraagd in te loggen via de onderwijsinstelling die je wilt koppelen. Selecteer eerst welke instelling je wilt koppelen en log daarna in.<br/> <br/>Nadat je succesvol bent ingelogd kom je hier weer terug.",
        },
        verifyStudent: {
            addInstitution: "Bewijs student",
            addInstitutionConfirmation: "Als je doorgaat word je gevraagd in te loggen via de onderwijsinstelling die je wilt koppelen. Selecteer eerst welke instelling je wilt koppelen en log daarna in.<br/> <br/>Nadat je succesvol bent ingelogd kom je hier weer terug.",
        },
        verifyParty: {
            addInstitution: "Koppel instelling",
            addInstitutionConfirmation: "Als je doorgaat word je gevraagd in te loggen via de onderwijsinstelling die je wilt koppelen. Selecteer eerst welke instelling je wilt koppelen en log daarna in.<br/> <br/>Nadat je succesvol bent ingelogd kom je hier weer terug.",
        }
    },
    eppnAlreadyLinked: {
        header: "Koppeling niet gemaakt!",
        info: "Je eduID kon niet worden gekoppeld. Het vertrouwde account waarmee je zojuist bent ingelogd, is al aan een ander eduID-account gekoppeld: {{email}}.",
        retryLink: "Opnieuw proberen"
    },
    edit: {
        title: "Aanpassen profielgegevens",
        info: "Voer je volledige naam in.",
        givenName: "Je voornaam",
        familyName: "Je achternaam",
        update: "Opslaan",
        cancel: "Annuleren",
        updated: "Je profiel is bijgewerkt.",
        back: "/profile"
    },
    email: {
        title: "E-mail",
        info: "Voer je nieuwe e-mailadres in. Er wordt een verificatiemail naar dit adres gestuurd.",
        email: "Je nieuwe e-mail",
        update: "Verstuur",
        cancel: "Annuleer",
        updated: "Een verificatiemail is verzonden naar {{email}}",
        confirmed: "Je e-mail is gewijzigd naar {{email}}",
        back: "/personal",
        emailEquality: "Je nieuwe e-mailadres is gelijk aan je huidige e-mailadres",
        duplicateEmail: "Dit e-mailadres is al in gebruik.",
        outstandingPasswordForgotten: "Uitstaand reset verzoek voor wachtwoord",
        outstandingPasswordForgottenConfirmation: "Je hebt een openstaand wachtwoord vergeten link. Deze link is niet langer geldig als je deze e-mailwijziging bevestigt."
    },
    security: {
        title: "Beveiliging",
        subTitle: "We bieden verschillende methoden om in te loggen met je eduID.",
        secondSubTitle: "Andere loginmethoden",
        usePassword: "Wachtwoord",
        usePublicKey: "Beveiligingssleutel",
        notSet: "Niet ingesteld",
        notSupported: "Niet ondersteund",
        useMagicLink: "Stuur magische link naar",
        rememberMe: "Ingelogd blijven",
        securityKey: "Beveiligingssleutel {{nbr}}",
        test: "Test",
        addSecurityKey: "Beveiligingssleutel toevoegen",
        addSecurityKeyInfo: "Je kunt een beveiligingssleutel toevoegen aan je eduID account waarmee je kunt inloggen. Dit kan bijv. de ingebouwde sensor van je apparaat zijn (TouchID, FaceID) of een los hardwaretoken (YubiKey).",
        settings: "Instellingen voor inloggen",
        rememberMeInfo: "<strong> Dit apparaat wordt momenteel onthouden. Je wordt automatisch ingelogd op eduD </strong>",
        noRememberMeInfo: "Als je inlogt met eduID kun je ervoor kiezen om <strong>ingelogd te blijven</strong>. Dan wordt jouw login op het apparaat dat je op dat moment gebruikt onthouden.",
        forgetMe: "Vergeet dit apparaat",
        tiqr: {
            title: "Wil je de volgende keer sneller en veiliger inloggen?",
            info: "Download de <strong>eduID app</strong> en log veilig in zonder wachtwoord of toegang tot je e-mail.",
            fetch: "Nu installeren",
            deactivate: "Deactiveren",
            app: "eduID app",
            phoneId: "Telefoon ID",
            APNS: "iPhone",
            APNS_DIRECT: "iPhone",
            FCM: "Android",
            GCM: "Android",
            FCM_DIRECT: "Android",
            appCode: "App code",
            lastLogin: "Laatste login",
            activated: "Geactiveerd op",
            dateTimeOn: "om",

        }
    },
    home: {
        home: "Home",
        welcome: "Welkom {{name}}",
        "data-activity": "Data & activiteit",
        personal: "Persoonlijke info",
        security: "Beveiliging",
        account: "Account",
        institutions: "Koppelingen",
        services: "Diensten",
        favorites: "Favorieten",
        settings: "Instellingen",
        links: {
            teams: "Teams",
            teamsHref: "https://teams.{{baseDomain}}",
        }
    },
    account: {
        title: "Je eduID account",
        titleDelete: "Verwijder je eduID account",
        info: "Op deze pagina kun je je account beheren.",
        created: "Aangemaakt op",
        delete: "Verwijder mijn account",
        cancel: "Annuleer",
        deleteInfo: "Ga voorzichtig te werk, want je verliest de unieke eduID ID's die momenteel aan je e-mailadres zijn gekoppeld.",
        data: "Download je data",
        downloadData: "Download",
        downloadDataConfirmation: "De download van je persoonlijke gegevens van je eduID-account bevat alle informatie die we over je  hebben. Het bevat ook technische sleutels en referenties.",
        personalInfo: "Klik op de onderstaande knop om al je persoonlijke gegevens uit je eduID account te downloaden.",
        deleteTitle: "Je eduID account verwijderen",
        info1: "Je kunt je eduID account verwijderen wanneer je maar wilt.",
        info2: "Let op, je verliest de unieke eduID nummers die aan je e-mailadres zijn gekoppeld. Wanneer je je opnieuw registreert voor eduID met hetzelfde e-mailadres, krijg je een nieuwe eduID nummers. Sommige diensten gebruiken deze nummers om je uniek te identificeren, dus voor die diensten word je dan gezien als een nieuwe gebruiker. ",
        info3: "Houd er rekening mee dat het verwijderen van je eduID niet betekent dat alle diensten die je met je eduID hebt gebruikt, ook je gegevens zullen verwijderen.",
        info4: "Om het verwijderen van je eduID volledig te voltooien, moet je nadat je account is verwijderd je browser afsluiten.",
        deleteAccount: "Verwijder mijn eduID",
        deleteAccountConfirmation: "Weet je zeker dat je je eduID wilt verwijderen?",
        deleteAccountSure: "Je account voor alle eeuwigheid verwijderen?",
        deleteAccountWarning: "Er is geen manier om deze actie ongedaan te maken.",
        proceed: "Als je wilt doorgaan, typ dan je volledige naam zoals bekend in je eduID account ter bevestiging.",
        name: "Volledige naam",
        namePlaceholder: "Je volledige naam zoals gebruikt in je profiel"
    },
    dataActivity: {
        title: "Gebruikte diensten",
        info: "Elke dienst waarvoor je eduID gebruikt ontvangt bepaalde gegevens (attributen) vanuit jouw eduID account. Denk hierbij aan je naam, e-mailadres of aan een pseudoniem waarmee de dienst jou uniek kan identificeren.",
        explanation: "Diensten waarop je bent ingelogd via eduID.",
        noServices: "Je bent nog niet ingelogd geweest op een dienst via eduID.",
        name: "Naam",
        add: "Nieuwe instelling koppelen",
        access: "Heeft toegang tot je data",
        details: {
            login: "Logingegevens",
            delete: "Verwijder logingegevens",
            first: "Eerste login",
            eduID: "Uniek eduID nummer",
            homePage: "Home pagina",
            deleteDisclaimer: "Als je deze logingegevens verwijdert, verwijdert eduID deze informatie uit je eduID account. Je hebt nog een account bij de dienst zelf. Als je dat wilt laten verwijderen, doe dat dan rechtstreeks bij de dienst.",
            access: "Toegangsrechten",
            details: "Accountgegevens",
            consent: "Datum toestemming",
            expires: "Vervaldatum",
            revoke: "Intrekken"
        },
        deleteService: "Verwijder dienst",
        deleteServiceConfirmation: "Weet je zeker dat je het unieke gepseudonimiseerde eduID voor {{name}} wilt verwijderen? <br/> <br/> Deze dienst herkent je wellicht niet meer de volgende keer dat je inlogt en je persoonlijke gegevens bij deze dienst zijn daardoor mogelijk niet meer toegankelijk.",
        deleteTokenConfirmation: "Weet je zeker dat de API access beveiligingssleutel voor {{name}} wilt intrekken?",
        deleteToken: "Beveiligingssleutel intrekken",
        deleted: "eduID verwijderd",
        tokenDeleted: "Beveiligingssleutels verwijderd"
    },
    institution: {
        title: "Gekoppelde instelling",
        info: "Deze instelling is op {{date}} om {{hours}}: {{minutes}} gekoppeld aan jouw eduID.",
        name: "Naam van de instelling",
        eppn: "Identifier bij de instelling",
        displayName: "Weergavenaam",
        affiliations: "Betrekking(en) bij de instelling",
        expires: "Koppeling verloopt op",
        expiresValue: "{{date}}",
        delete: "Verwijder koppeling",
        cancel: "Annuleren",
        deleted: "De koppeling met instelling {{name}} is verwijderd",
        back: "/instellingen",
        deleteInstitution: "Verwijder koppeling",
        deleteInstitutionConfirmation: "Weet je zeker dat je de koppeling met deze instelling wilt verwijderen?<br/> <br/>Sommige diensten vereisen dat je een koppeling hebt met een onderwijsinstelling. Je wordt mogelijk gevraagd een instelling te koppelen als je één van die diensten gebruikt."
    },
    credential: {
        title: "Bewerk beveiligingssleutel",
        info: "Je hebt deze key toegevoegd op {{date}} om {{hours}}: {{minutes}}",
        name: "Naam",
        cancel: "Annuleren",
        update: "Bewaar",
        deleted: "Je key {{name}} is verwijderd",
        updated: "Je key {{name}} is bewaard",
        back: "/weauthn",
        deleteCredential: "Verwijder key",
        deleteCredentialConfirmation: "Weet je zeker dat je de beveiligingssleutel {{name}} wilt verwijderen? De beveiligingssleutel wordt verwijderd uit je eduID account, maar wordt niet verwijderd uit je browser en / of van je YubiKey-apparaat."
    },
    password: {
        setTitle: "Wachtwoord instellen",
        updateTitle: "Wijzig wachtwoord",
        resetTitle: "Stel je wachtwoord opnieuw in",
        currentPassword: "Huidig wachtwoord",
        newPassword: "Nieuw wachtwoord",
        confirmPassword: "Bevestig nieuw wachtwoord",
        setUpdate: "Wachtwoord instellen",
        updateUpdate: "Opslaan",
        cancel: "Annuleren",
        set: "Je wachtwoord is ingesteld",
        reset: "Je wachtwoord is opnieuw ingesteld",
        updated: "Je wachtwoord is aangepast",
        back: "/security",
        passwordDisclaimer: "Kies een wachtwoord van tenminste 8 karakters lang met minimaal een hoofdletter en een cijfer. Een langer wachtwoord van minimaal 15 karakters mag ook.",
        invalidCurrentPassword: "Je huidige wachtwoord is niet correct.",
        passwordResetHashExpired: "De link om je wachtwoord opnieuw in te stellen is verlopen. ",
        forgotPassword: "Help! Ik ben mijn huidige wachtwoord vergeten",
        passwordResetSendAgain: "Stuur een e-mail om mijn wachtwoord opnieuw in te stellen.",
        forgotPasswordConfirmation: "Wachtwoord vergeten? Druk hieronder op 'Bevestigen' om direct een e-mail te ontvangen waarmee je je huidige wachtwoord opnieuw kunt instellen.",
        outstandingEmailReset: "Openstaand wijzigingsverzoek voor e-mail",
        outstandingEmailResetConfirmation: "Je hebt een openstaande bevestigingslink voor een nieuwe e-mail. Deze link is niet langer geldig als je dit verzoek voor een vergeten wachtwoord bevestigt.",
        flash: {
            passwordLink: "Een e-mail is verstuurd naar {{name}} om je wachtwoord opnieuw in te stellen."
        }

    },
    webauthn: {
        setTitle: "Beveiligingssleutel toevoegen",
        updateTitle: "Beveiligingssleutel toevoegen",
        publicKeys: "Je publieke keys",
        noPublicKeys: "Je hebt nog geen keys toegevoegd.",
        nameRequired: "Voordat je een beveiligingssleutel kan toevoegen, moet je deze eerst een naam geven.",
        revoke: "Intrekken",
        addDevice: "Voeg dit apparaat toe",
        info: "Een beveiligingssleutel is een los of ingebouwd apparaat dat strikt persoonlijk van jou is, waarmee je kunt bewijzen dat jij de rechtmatige gebruiker van je eduID-account bent. Nadat je een beveiligingssleutel hebt toegevoegd aan je eduID-account kun je deze gebruiken om mee in te loggen, naast een magische link of wachtwoord.",
        back: "/security",
        setUpdate: "Start",
        updateUpdate: "Voeg deze beveiligingssleutel toe",
        credentialName: "Naam van beveiligingssleutel",
        credentialNamePlaceholder: "bijv. mijn gele Yubikey",
        test: "Test",
        testInfo: "Druk op de <strong>test</strong>knop om een 1 van je beveiligingssleutels te testen. Je wordt doorgestuurd naar het eduID loginscherm.",
        testFlash: "Je hebt met succes de beveligingssleutel getest voor inloggen."
    },
    rememberMe: {
        updated: "Dit apparaat wordt niet langer onthouden",
        forgetMeTitle: "Onthoud dit apparaat",
        info: "Dit apparaat wordt onthouden. Je bent hierdoor automatisch ingelogd met eduID.",
        cancel: "Annuleren",
        update: "Vergeet dit apparaat",
        forgetMeConfirmation: "Weet je zeker dat je dit apparaat niet langer wilt onthouden?",
        forgetMe: "Vergeet dit apparaat"
    },
    footer: {
        privacy: "Privacybeleid",
        terms: "Voorwaarden",
        help: "Help",
        poweredBy: "Aangeboden door"
    },
    modal: {
        cancel: "Annuleren",
        confirm: "Bevestigen"
    },
    format: {
        creationDate: "{{date}} om {{hours}}:{{minutes}}"
    },
    getApp: {
        header: "Download de eduID app",
        info: "Download en installeer <a href=\"https://eduid.nl/help\" target=\"_blank\">de eduID app</a> (uitgegeven door SURF) op je mobiele apparaat.",
        google: "https://play.google.com/apps/testing/nl.eduid",
        apple: "https://testflight.apple.com/join/Ogk1TxKu",
        after: "Als je de eduID app op je telefoon hebt gedownload, kom dan hier terug en klik op volgende.",
        back: "Terug",
        next: "Volgende"
    },
    sms: {
        header: "Controleer je telefoon",
        info: "Voer de zescijferige code in die we naar je telefoon hebben gestuurd om door te gaan.",
        codeIncorrect: "De code is onjuist",
        maxAttemptsPre: "Maximum aantal pogingen bereikt. Klik",
        maxAttemptsPost: "om opnieuw je telefoonnummer in te voeren en een nieuwe code te ontvangen",
        maxAttemptsPostNoReEnter: "om een nieuwe code aan te vragen",
        here: " hier "

    },
    enrollApp: {
        header: "Voltooi de installatie in de eduID app",
        scan: "Scan deze QR-code met je eduID app",
        timeOut: "Sessie timeout",
        timeOutInfoFirst: "Je sessie is verlopen. Klik op deze ",
        timeOutInfoLast: " om het opnieuw te proberen.",
        timeOutInfoLink: "link",
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
        header: "Voeg een recovery telefoonnummer",
        info: "Je telefoonnummer wordt gebruikt voor veiligheidsdoeleinden, zoals u helpen om weer toegang te krijgen tot uw account als u uw app ooit kwijtraakt.",
        text: "We sturen je een code om je nummer te verifiëren.",
        verify: "Verifieer dit telefoonnummer",
        placeHolder: "+31 612345678",
        phoneIncorrect: "Verificatie code is onjuist"
    },
    congrats: {
        header: "Succes",
        info: "Je kunt nu de eduID app gebruiken om snel in te loggen bij diensten waarvoor je moet inloggen met je eduID.",
        next: "Afronden"
    },
    deactivate: {
        titleDelete: "Deactiveer je eduID app",
        info: "Je kunt je eduID app deactiveren als je deze app opnieuw wilt installeren of als je een nieuw apparaat hebt.",
        recoveryCode: "Backup recovery code",
        recoveryCodeInfo: "Vul de <strong>backup recovery code</strong> in van de eduID app installatie.",
        verificationCode: "SMS verificatie code",
        codeIncorrect: "Verkeerde backup recovery code",
        next: "Volgende",
        deactivateApp: "Deactiveer",
        sendSms: "Druk op volgende om een verificatiecode-sms naar je geregistreerde telefoonnummer te sturen.",
        maxAttempts: "Maximum aantal pogingen bereikt. Neem contact op met <a href=\"mailto:help@eduid.nl\">help@eduid.nl</a> voor hulp."
    }

};
