import I18n from "i18n-js";

I18n.translations.nl = {
    header: {
        title: "eduID",
        logout: "Logout"
    },
    landing: {
        info: "Online samenwerken in het onderwijs",
        login: "Enter",
        logoutStatus: "Je bent succesvol uitgelogd. Om het uitlogproces te voltooien, moet je je browser sluiten",
        deletionStatus: "Je hebt succesvol je account verwijderd. Om het proces te voltooien, moet je je browser sluiten"
    },
    notFound: {
        main: "404 - Not Found"
    },
    profile: {
        title: "Persoonlijke informatie",
        info: "Basisinformatie zoals je naam en e-mailadres.",
        email: "Email",
        schacHomeOrganization: "Instellings ID",
        name: "Naam",
        profile: "Profiel"
    },
    edit: {
        title: "Aanpassen profiel gegevens",
        info: "Voer je volledige naam in",
        givenName: "Je voornaam",
        familyName: "Je achternaam",
        update: "Verstuur",
        cancel: "Annuleer",
        updated: "Je profiel is bijgewerkt.",
        back: "/profile"
    },
    security: {
        title: "Beveiligingsinstellingen",
        subTitle: "We bieden verschillende methoden om in te loggen op je eduID-account.",
        secondSubTitle: "Inlog methoden",
        usePassword: "Wachtwoord",
        notSet: "Niet ingesteld",
        useMagicLink: "Email magic link naar",
        rememberMe: "Ingelogd blijven",
        rememberMetrue: "Ja",
        rememberMefalse: "Nee",
    },
    home: {
        welcome: "Welkom {{name}}",
        profile: "Profiel",
        security: "Beveiliging",
        account: "Account",
        favorites: "Favorieten",
        settings: "Instellingen",
        links: {
            teams: "Teams",
            teamsHref: "https://teams.{{baseDomain}}",
        }
    },
    account: {
        title: "Je eduID account",
        info: "Je kan ervoor kiezen om je eduID-account te verwijderen",
        deleteAccount: "Verwijder mijn account",
        deleteAccountConfirmation: "Weet je zeker dat je je eduID account wil verwijderen?"
    },
    password: {
        setTitle: "Wachtwoord instellen",
        updateTitle: "Verander wachtwoord",
        currentPassword: "Huidige wachtwoord",
        newPassword: "Nieuwe wachtwoord",
        confirmPassword: "Bevestig nieuwe wachtwoord",
        setUpdate: "Zet wachtwoord",
        updateUpdate: "Verander wachtwoord",
        cancel: "Annuleer",
        set: "Je wachtwoord is gezet",
        updated: "je wachtwoord is aangepast",
        back: "/security",
        passwordDisclaimer: "je wachtwoord moet minimaal 15 karakters zijn of 8 met en dan inclusief een hoofdletter en cijfer.",
        invalidCurrentPassword: "Je huidige wachtwoord is niet correct."
    },
    rememberMe: {
        updated: "Dit apparaat is niet langer onthouden",
        forgetMeTitle: "Onthoud dit apparaat.",
        info: "Je huidige apparaat wordt onthouden. Je bent hierdoor automatisch ingelogt op de eduID Guest IdP.",
        cancel: "Annuleer",
        update: "Vergeet me",
        forgetMeConfirmation: "Weet je zeker dat je dit apparaat niet langer wilt onthouden?",
        forgetMe: "Vergeet dit apparaat"
    },
    footer: {
        privacy: "Privacybeleid | Voorwaarden",
        help: "Help",
        poweredBy: "Aangeboden door"
    },
    modal: {
        cancel: "Annuleren",
        confirm: "Bevestigen"
    },
    migration: {
        header: "Je eduID gastaccount is aangemaakt!",
        info: "je Onegini account is succesvol gemigreerd.",
        info2: "Vanaf nu moet je je eduID gastaccount gebruiken om inte loggene bij services waar je voorheen Onegini gebruikte.",
        info3: "Tip! je eduID gastaccount heeft standaard geen wachtwoord nodig (we sturen een magische link naar je e-mail om in te loggen), maar als je wilt, kan je een wachtwoord instellen op het ",
        securityLink: " tabblad Beveiliging.",
        link: "Naar mijn account gegevens"
    },
    migrationError: {
        header: "Account Migratie Conflict",
        info: "We hebben uw bestaande Onegini-account NIET gemigreerd naar een eduID-gastaccount omdat u al een gebruiker van een gastaccount hebt.",
        question: "Als u uw Onegini-account wilt migreren, klikt u op migreren. Als u wilt doorgaan met uw bestaande eduID-gastaccount, klikt u op doorgaan",
        migrate: "Migreren",
        proceed: "Doorgaan",
        help : "Als je hier vragen over hebt, neem dan contact op met <a href=\"mailto:support@surfconext.nl\"> support@surfconext.nl </a>."
    }
};