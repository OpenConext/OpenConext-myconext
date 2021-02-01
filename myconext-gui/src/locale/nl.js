import I18n from "i18n-js";

I18n.translations.nl = {
    sidebar: {
        home: "Home",
        personalInfo: "Personal info",
        dataActivity: "Data & activity",
        security: "Security",
        account: "Account"
    },
    start: {
        hi: "Hi {{name}}!",
        manage: "Manage your personal info, your privacy, and the security of your eduID account."
    },
    header: {
        title: "eduID",
        logout: "Uitloggen"
    },
    landing: {
        logoutTitle: "Je bent uitgelogd",
        logoutStatus: "Om het uitlogproces te voltooien, moet je de browser nu afsluiten.",
        deleteTitle: "Je eduID is verwijderd",
        deleteStatus: "Om het verwijderingsproces te voltooien, moet je je browser nu afsluiten."
    },
    notFound: {
        title: "Oeps...",
        title2: "Er is iets fout gegaan (404)."
    },
    profile: {
        title: "Persoonlijke informatie",
        info: "Wanneer je eduID gebruikt om in te loggen op andere websites, moet een deel van je persoonlijke informatie worden gedeeld. Sommige services vereisen dat je persoonlijke gegevens worden gevalideerd door een derde partij.",
        basic: "Basic information",
        email: "E-mail",
        name: "Naam",
        validated: "Validated information",
        firstAndLastName: "Voor- en achternaam",
        firstAndLastNameInfo: "Je voor- en achternaam zijn nog niet geverifieerd door een vertrouwde partij",
        verify: "Verifïeer",
        student: "Bewijs van studie",
        studentInfo: "je hebt nog niet bewezen dat je in Nederland een studie volgt.",
        prove: "Bewijs",
        trusted: "Link naar vertrouwde partij",
        trustedInfo: "Je eduID-account is nog niet gekoppeld aan een vertrouwde partij",
        link: "Koppel",
        institution: "Instelling",
        affiliations: "Affiliation(s)",
        expires: "Koppeling verloopt",
        expiresValue: "{{date}}",
        verifiedAt: "Geverifïeerd door <strong>{{name}}</strong> op {{date}}",
        addInstitutionConfirmation: "Als je doorgaat word je gevraagd in te loggen via de onderwijsinstelling die je wilt koppelen. Selecteer eerst welke instelling je wilt koppelen en log daarna in.<br/> <br/>Nadat je succesvol bent ingelogd kom je hier weer terug.",
        proceed: "Doorgaan",
        addInstitution: "Instelling toevoegen"
    },
    eppnAlreadyLinked: {
        header: "Account niet gekoppeld!",
        info: "Je eduID kon niet worden gekoppeld, omdat het account waarmee je bent ingelogd, al aan een ander eduID-account is gekoppeld.",
        proceed: "Je kan het nogmaals met een andere instelling proberen.",
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
        updated: "Een e-mail is verzonden naar {{email}}",
        confirmed: "Je e-mail is gewijzigd naar {{email}}",
        back: "/personal",
        emailEquality: "Je nieuwe e-mailadres is gelijk aan je huidige e-mailadres",
        duplicateEmail: "Dit e-mailadres is al in gebruik."
    },
    security: {
        title: "Beveiliging",
        subTitle: "We bieden verschillende methoden om in te loggen met je eduID.",
        secondSubTitle: "Loginmethoden",
        usePassword: "Wachtwoord",
        usePublicKey: "WebAuthn",
        notSet: "Niet ingesteld",
        notSupported: "Not supported",
        useMagicLink: "Stuur magische link naar",
        rememberMe: "Ingelogd blijven",
        rememberMetrue: "Ja",
        rememberMefalse: "Nee",
        securityKey: "Security key {{nbr}}",
        test: "Test",
        addSecurityKey: "Security key toevoegen",
        addSecurityKeyInfo: "Je kan biometrieken (TouchID, FaceID of Windows Hello) of hardware security tokens (Yubikey).",
        settings: "Inlog instellngen",
        rememberMeInfo: "<strong> Uw apparaat wordt momenteel onthouden. Je wordt automatisch ingelogd op eduD </strong>",
        noRememberMeInfo: "Als u inlogt met uw eduID, kunt u ervoor kiezen om <strong> ingelogd te blijven </strong>. Dit onthoudt uw login op het apparaat dat u op dat moment gebruikt.",
        forgetMe: "Vergeet me"

    },
    home: {
        home: "Home",
        welcome: "Welkom {{name}}",
        "data-activity": "Data & activiteit",
        personal: "Personal info",
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
        titleDelete: "Verijder je eduID account",
        info: "Op deze pagna kan je je account beheren.",
        created: "Aangemaakt op",
        delete: "Verijder mijn account",
        cancel: "Annuleer",
        deleteInfo: "Ga voorzichtig te werk, want je verliest de unieke eduID-ID's die momenteel aan je e-mailadres zijn gekoppeld.",
        data: "Download je data",
        personalInfo: "Klik op de onderstaande knop om al je persoonlijke gegevens te downloaden.",
        deleteTitle: "Je eduID verwijderen",
        info1: "Je kunt je eduID verwijderen wanneer je maar wilt.",
        info2: "Let op, je verliest het unieke eduID-nummer dat momenteel aan je e-mailadres is gekoppeld. Wanneer je je opnieuw registreert voor eduID met hetzelfde e-mailadres, krijg je een nieuw eduID-nummer. Sommige diensten gebruiken dit unieke nummer om je te identificeren, dus voor die diensten word je dan gezien als een nieuwe gebruiker. ",
        info3: "Houd er rekening mee dat het verwijderen van je eduID niet betekent dat alle diensten die je met je eduID hebt gebruikt, ook je gegevens zullen verwijderen.",
        info4: "Om het verwijderen van je eduID volledig te voltooien, moet je nadat je account is verwijderd je browser afsluiten.",
        deleteAccount: "Verwijder mijn eduID",
        deleteAccountConfirmation: "Weet je zeker dat je je eduID wilt verwijderen?",
        deleteAccountSure: "Je account voor alle eeuwigheid verwijderen?",
        deleteAccountWarning: "Er is geen manier om deze actie ongedaan te maken.",
        proceed: "Als je wilt doorgaan, typ dan je volledige naam in ter bevestiging.",
        name: "Volledige naam",
        namePlaceholder: "Je volledige naam zoals gebruikt in uw profiel"
    },
    dataActivity: {
        title: "Gebruikte diensten",
        info: "Elke dienst waarvoor je eduID gebruikt ontvangt bepaalde gegevens (attributen) vanuit jouw eduID-account. Denk hierbij aan je naam, e-mailadres of aan een pseudoniem waarmee de dienst jou uniek kan identificeren.",
        explanation: "Diensten waarop je ben ingelogd via eduID.",
        noServices: "Je bent nog niet ingelogd geweest op een dienst via eduID.",
        name: "Naam",
        add: "Nieuwe instelling koppelen",
        access: "Heeft toegang tot je data",
        details: {
            login: "Login gegevens",
            delete: "Verijder login gegevens",
            first: "Eerste login",
            eduID: "Unieke eduID",
            homePage: "Home pagina",
            deleteDisclaimer: "Als je deze inloggegevens verwijdert, verwijdert eduID deze informatie uit je eduID-account. Je hebt nog een account bij de dienst zelf. Als je dat wilt laten verwijderen, doe dat dan bij de dienst.",
            access: "Toegangsrechten",
            details: "Account gegevens",
            consent: "Consent datum",
            expires: "Vervaldatum",
            revoke: "Intrekken"
        },
        deleteService: "Verwijder dienst",
        deleteServiceConfirmation: "Weet je zeker dat je je unieke gepseudonimiseerde eduID voor {{name}} wilt verwijderen? <br/> <br/> Deze dienst herkent je wellicht niet meer de volgende keer dat je inlogt en je persoonlijke gegevens bij deze dienst worden daardoor mogelijkerwijs verwijderd.",
        deleteTokenConfirmation: "Weet je zeker dat het API access token voor {{name}} wilt intrekken?",
        deleteToken: "Token intrekken",
        deleted: "eduID verwijderd",
        tokenDeleted: "Tokens verwijderd"
    },
    institution: {
        title: "Gekoppelde instelling",
        info: "Deze instelling is op {{date}} om {{hours}}: {{minutes}} gekoppeld aan je eduID.",
        name: "Naam van de instelling",
        eppn: "Identifier bij de instelling",
        displayName: "Weergavenaam",
        affiliations: "Betrekking(en) bij de instelling",
        expires: "Koppeling verloopt op",
        expiresValue: "{{date}}",
        delete: "Verwijder Koppeling",
        cancel: "Annuleren",
        deleted: "De koppeling met instelling {{name}} is verwijderd",
        back: "/instellingen",
        deleteInstitution: "Verwijder koppeling",
        deleteInstitutionConfirmation: "Weet je zeker dat je de koppeling met deze instelling wilt verwijderen?<br/> <br/>Sommige diensten vereisen dat je een koppeling hebt met een onderwijsinstelling. Je wordt mogelijk gevraagd een instelling te koppelen als je één van die diensten gebruikt."
    },
    credential: {
        title: "Bewerk security key",
        info: "Je hebt deze key toegevoegd op {{date}} om {{hours}}: {{minutes}}",
        name: "Naam",
        cancel: "Annuleren",
        update: "Bewaar",
        deleted: "Je key {{name}} is verwijderd",
        updated: "Je key {{name}} is bewaard",
        back: "/weauthn",
        deleteCredential: "Verwijder key",
        deleteCredentialConfirmation: "Weet je zeker dat je de key {{name}} wilt verwijderen? De key wordt verwijderd uit je eduID-account, maar wordt niet verwijderd uit je browser en / of van je YubiKey-apparaat."
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
        flash: {
            passwordLink: "Een e-mail is verstuurd naar {{name}} om je wachtwoord opnieuw in te stellen."
        }

    },
    webauthn: {
        setTitle: "WebAuthn instellen",
        updateTitle: "WebAuthn instellen",
        publicKeys: "Je publieke keys",
        noPublicKeys: "Je hebt nog geen keys toegevoegd.",
        nameRequired: "Voordat je een key kan toevoegen, moet je deze eerst een naam geven.",
        revoke: "Revoke",
        addDevice: "Voeg dit apparaat toe",
        info: "Public Key Cryptography and Web Authentication (ook bekend als WebAuthn) stelt eduID in staat om u te authenticeren met behulp van public key cryptografie in plaats van een magische link of wachtwoord.",
        back: "/security",
        setUpdate: "Start",
        updateUpdate: "Voeg dit apparaat toe",
        credentialName: "Device naam",
        credentialNamePlaceholder: "e.g. mijn gele YubiKey",
        test: "Test",
        testInfo: "Druk op de <strong>test</strong>knop om een 1 van je WebAuthn-sleutels te testen. Je wordt doorgestuurd naar de eduID-identiteitsserver.",
        testFlash: "Je hebt met succes je WebAuthn-sleutel getest voor authenticatie"
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
    migration: {
        header: "Je eduID is aangemaakt!",
        info: "Je Onegini-account is succesvol gemigreerd.",
        info2: "Vanaf nu moet je eduID gebruiken om in te loggen bij diensten waar je voorheen Onegini gebruikte.",
        info3: "Tip! je eduID heeft standaard geen wachtwoord nodig (we sturen een magische link naar je e-mail om in te loggen), maar als je wilt, kan je toch een wachtwoord instellen op het ",
        securityLink: " tabblad Beveiliging.",
        link: "Naar mijn account gegevens"
    },
    migrationError: {
        header: "Let op: eduID met zelfde e-mailadres bestaat al",
        info: "Je hebt al een eduID met hetzelfde e-mailadres als je Onegini-account. Je hebt daarom nu twee keuzes:",
        sub1: "Doorgaan met migreren van je bestaande Onegini-account naar een nieuw eduID. Dit betekent:",
        sub1Inner1: "Teamlidmaatschappen in SURFconext Teams en autorisaties binnen diensten van je bestaande Onegini-account gaan mee naar je nieuwe eduID.",
        sub1Inner2: "Teamlidmaatschappen en autorisaties voor diensten van je bestaande eduID gaan verloren.",
        sub2: "Migratie afbreken en doorgaan met je bestaande eduID. Dit betekent:",
        sub2Inner1: "Je blijft je bestaande eduID gebruiken.",
        sub2Inner2: "Je bestaande Onegini-account kun je per 1 juli 2020 niet meer gebruiken. Teamlidmaatschappen in SURFconext Teams en autorisaties binnen diensten van dit Onegini-account gaan verloren.",
        abortMigration: "Migratie afbreken ",
        continueMigration: "Doorgaan met migreren",
        abort: "afbreken",
        continue: "doorgaan",
        help: "Hulp nodig? Stuur een e-mail naar <a href=\"mailto:support@surfconext.nl\">support@surfconext.nl</a>."
    },
    format: {
        creationDate: "{{date}} om {{hours}}:{{minutes}}"
    }
};
