const nl = {
    code: 'NL',
    name: 'Nederlands',
    languages: {
        language: 'Language',
        en: 'Engels',
        nl: 'Nederlands',
    },
    landing: {
        header: {
            title: 'eduID Dashboard ',
            subTitle: 'Hallo {{name}}',
            login: 'Log in',
            sup: 'eduID Dashboard is alleen op uitnodiging beschikbaar.',
        },
        works: 'Hoe werkt het?',
        adminFunction: 'Medewerker',
        studentFunction: 'Student',
        info: [
            //Matrices met titels en infoblokken en of een functie een beheerdersfunctie is
            [
                'Beveiliging',
                '<p>eduID Dashboard is een beveiligd platform ontworpen om de identiteit te valideren van studenten die een identiteitscontrole hebben aangevraagd. Deze dienst waarborgt naleving van formele identificatienormen en biedt tegelijkertijd een gebruiksvriendelijk proces voor medewerkers en studenten. </p>',
                true,
            ],
            [
                'Compliance',
                '<p>Gebouwd om te voldoen aan de formele identificatievoorschriften en nauwkeurigheid bij identiteitsverificatie te garanderen. Vermindert fouten door een gestructureerd en stapsgewijs validatieproces</p>',
                true,
            ],
            [
                'Authenticatie',
                '<p>Medewerkers initiëren het verificatieproces met behulp van een unieke code die via e-mail naar de student wordt verzonden. Het systeem zorgt ervoor dat alleen studenten met geldige aanmeldingscodes door kunnen gaan met de identiteitscontrole.</p><br/> ',
                false,
            ],
        ],
        footer:
            '<p>De eduID Dashboard vereenvoudigt het identiteitsverificatieproces en zorgt voor veiligheid en efficiëntie.</p>' +
            "<p>Wil je meer weten? <a href='https://support.surfconext.nl/servicedesk-nl'>Lees verder</a>.</p>",
    },
    header: {
        title: 'eduID Dashboard',
        links: {
            logout: 'Logout',
        },
    },
    serviceDesk: {
        member: 'Dashboard lid',
    },
    navigation: {
        overview: 'Overzicht',
        users: 'Gebruikers',
        identityCheck: 'Identiteitscontrole',
    },
    overview: {
        accountsOverTime: 'Accounts in de tijd',
        total: 'Totaal aantal accounts',
        added: 'Nieuwe accounts',
        services: 'Accounts per dienst',
        servicesSubtitle: 'Diensten waarbij de eduID accounts zijn ingelogd',
        searchServices: 'Zoek diensten…',
        clearSearch: 'Zoekopdracht wissen',
        showMore: 'Toon meer (+%{count})',
        showLess: 'Toon minder',
        noData: 'Geen gegevens beschikbaar',
        loadError: 'Het overzicht kon niet worden geladen',
    },
    users: {
        title: 'Gebruikers',
        description:
            'Doorzoek eduID accounts met een actieve link of invite rol voor jouw instelling.',
        search: 'Zoek op naam, e-mailadres of id…',
        mfaFilter: 'MFA methoden',
        allUsers: 'Alle gebruikers',
        blockedFilter: 'Blokkade',
        blocked: 'Geblokkeerd',
        notBlocked: 'Niet geblokkeerd',
        columns: {
            name: 'Naam',
            email: 'E-mailadres',
            mfa: 'MFA methode',
            affiliations: 'Affiliation(s)',
            id: 'ID',
        },
        mfa: {
            app: 'Mobiele app',
            passkey: 'Passkey',
            password: 'Wachtwoord',
        },
        affiliation: {
            student: 'Student',
            employee: 'Medewerker',
            staff: 'Medewerker',
            affiliate: 'Affiliate',
            member: 'Lid',
            faculty: 'Docent',
            alum: 'Alumnus',
        },
        blockedUser: 'Geblokkeerd',
        previous: 'Vorige',
        next: 'Volgende',
        morePages: 'Meer pagina’s',
        summary: '{{from}} t/m {{to}} van de {{total}} gebruikers',
        empty: 'Geen gebruikers gevonden',
        error: 'De gebruikers konden niet worden opgehaald',
    },
    userDetail: {
        back: 'Terug naar gebruikers',
        notFound: 'Deze gebruiker is niet gevonden',
        block: 'Blokkeren',
        verifiedWith: 'Geverifieerd met {{issuer}}',
        givenName: 'Voornaam',
        familyName: 'Achternaam',
        chosenName: 'Roepnaam',
        dateOfBirth: 'Geboortedatum',
        id: 'ID',
        eppn: 'ePPN',
        mfa: 'MFA methode',
        noMfa: 'Geen',
        reset: 'Resetten',
        loginCode: 'Stuur een inlogcode naar',
        change: 'Wijzigen',
        verified: 'Geverifieerd',
        blocked: 'Geblokkeerd',
    },
    activity: {
        title: 'Activiteit',
        filter: 'Filter op type',
        allTypes: 'Alle types',
        empty: 'Nog geen activiteit',
        showMore: 'Toon meer',
        types: {
            account: 'Account',
            security: 'Beveiliging',
            institution: 'Instelling',
            service: 'Dienst',
        },
        events: {
            ACCOUNT_CREATED: 'Account aangemaakt via {{institution}}',
            NAME_UPDATED: 'Naam gewijzigd naar {{name}}',
            LANGUAGE_CHANGED: 'Taal gewijzigd naar {{language}}',
            EMAIL_CHANGED: 'E-mailadres gewijzigd naar {{email}}',
            PASSWORD_ADDED: 'Wachtwoord toegevoegd',
            PASSWORD_UPDATED: 'Wachtwoord gewijzigd',
            PASSWORD_DELETED: 'Wachtwoord verwijderd',
            PASSKEY_ADDED: 'Passkey {{name}} toegevoegd',
            PASSKEY_RENAMED: 'Passkey hernoemd naar {{name}}',
            PASSKEY_DELETED: 'Passkey {{name}} verwijderd',
            APP_ACTIVATED: {
                default: 'eduID-app geactiveerd',
                phone: 'eduID-app geactiveerd met een telefoonnummer',
                recoveryCode: 'eduID-app geactiveerd met een herstelcode',
            },
            APP_DEACTIVATED: 'eduID-app gedeactiveerd',
            RECOVERY_METHOD_CHANGED: {
                default: 'Herstelmethode gewijzigd',
                phone: 'Herstelmethode gewijzigd naar een telefoonnummer',
                recoveryCode: 'Herstelmethode gewijzigd naar een herstelcode',
            },
            INSTITUTION_LINKED: 'Instelling {{institution}} gekoppeld',
            INSTITUTION_REMOVED: 'Instelling {{institution}} verwijderd',
            PREFERRED_INSTITUTION_CHANGED:
                'Voorkeursinstelling gewijzigd naar {{institution}}',
            IDENTITY_VERIFIED: 'Identiteit bevestigd door {{institution}}',
            IDENTITY_REMOVED:
                'Bevestigde identiteit van {{institution}} verwijderd',
            CONTROL_CODE_CREATED:
                'Controlecode voor de servicedesk aangevraagd',
            CONTROL_CODE_DELETED: 'Controlecode voor de servicedesk verwijderd',
            SERVICE_REMOVED: 'Inloggegevens van {{service}} verwijderd',
            TOKENS_REMOVED: 'Toegang van een dienst ingetrokken',
            SERVICE_FIRST_LOGIN: 'Eerste keer ingelogd bij {{service}}',
            SERVICE_LAST_LOGIN: 'Laatst ingelogd bij {{service}}',
        },
    },
    issuers: {
        idin: 'iDIN',
        eherkenning: 'eIDAS',
        studielink: 'Studielink',
        serviceDesk: 'Servicedesk',
        servicedesk: 'Servicedesk',
    },
    blockUser: {
        title: 'Gebruiker blokkeren?',
        question:
            'Weet je zeker dat je de toegang van het eduID van {{name}} bij {{institution}} wilt blokkeren?',
        info: {
            institution:
                'Het eduID kan daarna niet meer inloggen op diensten van {{institution}}.',
            active: 'Het eduID zelf blijft actief en bruikbaar voor diensten van andere instellingen.',
            indefinite:
                'De blokkade geldt voor onbepaalde tijd; je kunt deze later altijd weer opheffen.',
            notice: '{{name}} krijgt geen automatisch bericht over deze blokkade.',
        },
        cancel: 'Annuleren',
        confirm: 'Blokkeren',
        error: 'De gebruiker kon niet worden geblokkeerd',
    },
    identityCheck: {
        title: 'Identiteitscontrole',
        codeTitle: 'Vul de controlecode in',
    },
    info: {
        title: 'Verifieer iemands identiteit',
        subTitle:
            'Een eduID gebruiker heeft een afspraak gemaakt om zijn identiteit te laten verifiëren.',
        verify1: 'Vraag naar <strong>een eduID verificatie code</strong>',
        verify2:
            'Deze code heeft de gebruiker eerder gegenereerd in de eduID applicatie.',
        verify3:
            'Heeft de gebruiker géén code, vraag de gebruiker deze dan alsnog te maken door het verificatieprocess in eduID te starten.',
    },
    verification: {
        header: 'Voer de eduID verificatie code in',
        proceed: 'Ga door',
        error: 'Geen gebruiker gevonden voor de verificatiecode {{code}}',
        info: 'Deze code heeft de persoon gegenereerd in eduID',
        flash: 'Verificatie code gevonden voor gebruiker {{name}}',
    },
    control: {
        forCode: 'Voor code',
        restart: 'Start opnieuw',
        proceed: 'Bevestigen',
        error: "Er is een onverwachte fout opgetreden. Neem dan contact op met <a href='mailto:servicedesk@surf.nl>?subject=Referentie {{reference}}'>eduID Dashboard</a>",
        info: 'Controleer het identiteitsbewijs van deze persoon',
        validDocuments:
            'Geldige documenten: paspoorten, EER ID-kaarten, Nederlandse rijbewijzen en Nederlandse verblijfsvergunningen',
        inValidDocuments:
            'Kopieën, OV-kaarten en studentenkaarten zijn geen geldige documenten',
        isValid: 'Klopt dit allemaal?',
        validations: {
            photo: 'De persoon <span>lijkt op de foto</span> in het document',
            valid: 'Het document is <span>nog geldig</span>',
            lastName:
                'De achternaam in het document: <span>{{lastName}}</span>',
            firstName:
                'De voornamen in het document: <span>{{firstName}}</span>',
            dayOfBirth:
                'De geboortedatum in het document: <span>{{dayOfBirth}}</span>',
        },
        invalidDate:
            'De geboortedatum heeft een ongeldig formaat, selecteer eerst de juiste geboortedatum door op de kalendar knop rechts te drukken',
        validDate: 'De geboortedatum is nu correct en kan worden bevestigd',
        idDocument:
            'Vul de <span>laatste 6 karakters</span> van het document in',
    },
    forbidden: {
        info: 'Helaas heb je geen toegang tot het eduID Dashboard. Als je uitlogt, dan kan je het opnieuw proberen met een andere account',
        logout: 'Uitloggen',
    },
    confirmation: {
        header: 'De identiteit van de persoon is bevestigd',
        info: 'De persoon ziet vanaf nu in Mijn eduID een vinkje voor de persoonsgegevens.',
        viewEduId: 'eduID bekijken',
        restart: 'Nieuwe controle starten',
    },
    footer: {
        terms: 'Gebruiksvoorwaarden',
        termsLink: 'https://support.surfconext.nl/terms-nl',
        privacy: 'Privacybeleid',
        privacyLink: 'https://support.surfconext.nl/privacy-nl',
        surfLink: 'https://surf.nl',
        select_locale: 'Kies jouw taal',
    },
};

export default nl;
