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
        menu: 'Menu',
        home: 'Home',
        identity: 'Identiteit',
        institutions: 'Instellingen',
        security: 'Beveiliging',
        activity: 'Activiteit',
    },
    account: {
        download: {
            title: 'Download',
            info: 'De download van je persoonlijke gegevens van je eduID-account bevat alle informatie die we over je hebben. Het bevat ook technische sleutels en referenties.',
            cancel: 'Nee, terug',
            action: 'Download',
        },
        delete: {
            title: 'Je eduID verwijderen?',
            warning:
                'Dit kun je niet ongedaan maken. Je verliest je unieke eduID-nummer: een nieuwe eduID met hetzelfde e-mailadres krijgt een nieuw nummer, en sommige websites zien je dan als nieuwe gebruiker. Websites die aan je eduID gekoppeld zijn, verwijderen je gegevens niet automatisch.',
            confirm: "Typ '{{keyword}}' om te bevestigen.",
            keyword: 'verwijder',
            cancel: 'Annuleren',
            action: 'Account verwijderen',
        },
    },
    userMenu: {
        downloadData: 'Accountgegevens downloaden',
        deleteAccount: 'Account verwijderen',
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
        title: 'Activiteit',
        lead: 'Hier bekijk je waar je bent ingelogd met je eduID.',
        services: {
            title: 'Applicaties waarop je bent ingelogd via eduID',
            empty: 'Je hebt eduID nog niet gebruikt om in te loggen bij een applicatie.',
            loggedIn: 'Ingelogd {{time}}',
            access: 'Heeft toegang tot je eduID-gegevens.',
            accessGiven: 'Toegang gegeven op',
            accessExpires: 'Toegang verloopt op',
            uniqueEduId: 'Uniek eduID',
            firstLogin: 'Eerste login',
            lastLogin: 'Laatste login',
            homePage: 'Homepagina',
            deleteLogin: 'Verwijder logingegevens',
            revoke: 'Toegang intrekken',
            deleteDialog: {
                title: 'Verwijder logingegevens',
                question:
                    'Weet je zeker dat je de koppeling met applicatie {{name}} wilt verwijderen?',
                info: 'Deze applicatie herkent je wellicht niet meer de volgende keer dat je inlogt en je persoonlijke gegevens bij deze applicatie zijn daardoor mogelijk niet meer toegankelijk. Je hebt nog een account bij de applicatie zelf. Als je dat wilt laten verwijderen, doe dat dan rechtstreeks bij de applicatie.',
                action: 'Ja, verwijder',
            },
            revokeDialog: {
                title: 'Toegang intrekken',
                question:
                    'Weet je zeker dat je de toegang voor {{name}} wilt intrekken?',
                info: 'De applicatie heeft dan geen toegang meer tot je gegevens namens jou.',
                action: 'Ja, trek in',
            },
            cancel: 'Annuleren',
        },
        showMore: 'Toon meer',
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
    identity: {
        title: 'Jouw identiteit',
        lead: 'Bekijk en wijzig de gegevens die websites en apps van je zien als je met eduID inlogt.',
        edit: 'Wijzig',
        view: 'Bekijk',
        chosenName: 'Roepnaam',
        dateOfBirth: 'Geboortedatum',
        verifiedBy: 'Bevestigd door {{name}} op {{date}}',
        details: {
            verifiedBy: 'Bevestigd door',
            firstName: 'Voornaam',
            lastName: 'Achternaam',
            receivedOn: 'Ontvangen op',
            validUntil: 'Geldig tot',
        },
        editChosenName: {
            title: 'Wijzig je roepnaam',
        },
        issuers: {
            idin: 'iDIN',
            eherkenning: 'eIDAS',
        },
        unverified: {
            title: 'Nog niet bevestigd',
            description:
                'Sommige apps vereisen dat je bevestigt wie je bent bij een vertrouwde partij.',
            button: 'Bevestig je identiteit',
        },
        name: {
            label: 'Naam',
            edit: 'Wijzig',
        },
        editName: {
            title: 'Wijzig je naam',
            givenName: 'Voornaam',
            familyName: 'Achternaam',
            save: 'Opslaan',
            cancel: 'Annuleren',
        },
        verify: {
            title: 'Bevestig je identiteit',
            institutionQuestion:
                'Heb je een account bij een onderwijs- of onderzoeksinstelling?',
            institution: 'Gebruik je instellingsaccount',
            otherMethods:
                'Geen instellingsaccount? Kies dan een van de volgende methoden',
            bank: 'Gebruik je bank',
            european: 'Gebruik een Europees ID',
            bankTitle: 'Gebruik je bank',
            bankDescription:
                'Met iDIN controleren we via de bank wie je bent. Je maakt geen geld over.',
            bankMore: 'Meer over iDIN',
            bankMissing: 'Staat je bank er niet tussen?',
            noOtherMethod:
                'Heb je geen Europees ID of bank die iDIN ondersteunt?',
            serviceDesk: 'Bevestig bij een servicedesk',
            otherMethod: 'Gebruik een andere manier',
            bankLoading: 'Banken laden...',
        },
        serviceDesk: {
            formTitle: 'Bevestig wie je bent',
            formInfo:
                'Vul eerst de gegevens van je identiteitsbewijs in, daarna krijg je een controlecode die je laat zien bij een eduID-servicedesk.',
            validIdsTitle: 'Geldige identiteitsbewijzen',
            validIds:
                'Paspoort, ID-kaart, Nederlands rijbewijs, EEA*-kaart of Nederlandse verblijfsvergunning. Niet geldig: kopieën, studentenkaarten of OV-kaarten.',
            eeaNote:
                '*EEA: alle EU-lidstaten + Liechtenstein, Noorwegen en IJsland.',
            continue: 'Doorgaan',
            lastName: 'Achternaam',
            firstName: 'Voornamen',
            dateOfBirth: 'Geboortedatum',
            day: 'Geboortedag',
            month: 'Maand',
            year: 'Jaar',
            select: 'Selecteer...',
            codeTitle: 'Je controlecode',
            codeInfo: {
                one: 'Je krijgt ook een e-mail met deze code. De code is nog {{count}} dag geldig en is bedoeld voor:',
                other: 'Je krijgt ook een e-mail met deze code. De code is nog {{count}} dagen geldig en is bedoeld voor:',
            },
            typo: 'Typfout gemaakt?',
            typoLink: 'Pas gegevens aan',
            todo: 'Wat moet je nu doen?',
            todoDetails:
                'Maak een afspraak met een eduID Service Desk (digitaal of op locatie). Laat tijdens je afspraak je controlecode en identiteitsbewijs zien.',
            locations: 'Bekijk eduID Service Desks',
            locationsUrl: 'https://eduID.nl/servicedesk',
            back: 'Terug naar overzicht',
            rethink:
                'Wil je toch op een andere manier bewijzen wie je bent? Verwijder dan eerst je controlecode.',
            delete: 'Verwijder controlecode',
            errorTitle: 'Fout opgetreden',
            errorInfo:
                'De servicedesk is momenteel niet bereikbaar. Neem voor ondersteuning contact op met',
            bannerTitle: 'Wachten op de servicedesk',
            banner: 'Bevestig je identiteit bij een eduID Service Desk. Dit doe je door je identiteitsbewijs en controlecode te laten zien.',
            showCode: 'Toon code',
        },
    },
    institutions: {
        title: 'Instellingen',
        lead: 'Koppel onderwijs- of onderzoeksinstellingen om toegang te krijgen tot gerelateerde diensten.',
        role: 'Rol: {{roles}}',
        add: 'Instelling koppelen',
        learnMore: 'Lees meer',
        learnMoreUrl: 'https://eduid.nl/help/',
        empty: {
            title: 'Geen gekoppelde instellingen',
        },
        linked: {
            title: 'Je hebt bevestigd wie je bent',
            intro: 'De volgende gegevens zijn gekoppeld aan je eduID:',
            alsoLinked:
                'Hiermee is ook de volgende instelling aan je eduID gekoppeld:',
            choose: 'We hebben nieuwe gegevens ontvangen. Kies van welke instelling je je naam wilt gebruiken:',
            new: 'Nieuw',
            current: 'Huidig',
            dateOfBirth: 'Geboortedatum',
            continue: 'Doorgaan',
        },
        details: {
            firstName: 'Voornaam',
            lastName: 'Achternaam',
            receivedOn: 'Ontvangen op',
            validUntil: 'Geldig tot',
        },
        roles: {
            student: 'student',
            employee: 'medewerker',
            staff: 'personeel',
            faculty: 'docent',
            member: 'lid',
            affiliate: 'extern',
            alum: 'alumnus',
        },
        delete: {
            button: 'Instelling verwijderen',
            title: 'Instelling verwijderen?',
            question:
                'Weet je zeker dat je de koppeling met deze instelling wilt verwijderen?',
            info: "Sommige diensten vereisen dat je eduID is gekoppeld aan een instelling. Gebruik je later zo'n dienst? Dan kan het zijn dat je opnieuw moet koppelen.",
            cancel: 'Annuleren',
            action: 'Verwijderen',
        },
    },
    app: {
        get: {
            title: 'Activeer je eduID-app',
            info: 'Download de eduID-app (van SURF) in een van de app-stores:',
            qr: 'QR-code om de eduID-app te installeren',
            google: 'https://play.google.com/store/apps/details?id=nl.eduid',
            apple: 'https://apps.apple.com/us/app/eduid/id1600756434',
            after: 'Heb je de app al geïnstalleerd? Ga dan door.',
            cancel: 'Annuleren',
            next: 'Ga door',
        },
        enroll: {
            scanTitle: 'Voltooi de installatie in de eduID-app',
            scanInfo: 'Scan deze QR-code met je eduID-app.',
            qr: 'QR-code om de eduID-app te koppelen',
            open: 'Open de app op dit apparaat',
            finishTitle: 'Voltooi de installatie in de eduID-app',
            finishInfo:
                'Volg de instructies in de app om de installatie te voltooien.',
            timeoutTitle: 'Sessie verlopen',
            timeoutInfo:
                'Je sessie is verlopen. Sluit dit venster en probeer het opnieuw.',
            existingTitle: 'Installatie niet afgerond',
            existingInfo:
                'Je bent al begonnen met het koppelen van de eduID-app, maar de app is nog niet actief. Ga door om een herstelmethode in te stellen en af te ronden.',
            resume: 'Installatie afronden',
            close: 'Sluiten',
        },
        recovery: {
            title: 'Een herstelmethode instellen',
            info: 'Als je de eduID-app niet meer kunt gebruiken, bijvoorbeeld bij verlies van je mobiele telefoon, kun je met de herstelmethode de eduID-app opnieuw registreren.',
            back: 'Terug',
            phone: {
                option: 'Registreer hersteltelefoonnummer',
                optionInfo: 'Je ontvangt een sms met een verificatiecode.',
                title: 'Voeg een hersteltelefoonnummer toe',
                info: 'Je telefoonnummer wordt gebruikt om weer toegang te krijgen tot je account als je de app ooit kwijtraakt. We sturen je een code om je nummer te verifiëren.',
                label: 'Telefoonnummer',
                placeholder: '+31 612345678',
                incorrect: 'Telefoonnummer is onjuist',
                rateLimited:
                    'Je hebt te vaak een onjuiste verificatiecode ingevoerd. Wacht even voordat je het opnieuw probeert.',
                send: 'Stuur code',
            },
            sms: {
                title: 'Controleer je telefoon',
                info: 'Voer de zescijferige code in die we naar je telefoon hebben gestuurd om door te gaan.',
                label: 'Verificatiecode',
                incorrect: 'De code is onjuist',
                maxAttempts:
                    'Maximum aantal pogingen bereikt. Gebruik een ander telefoonnummer of vraag een nieuwe code aan.',
                resend: 'Nieuwe code',
                confirm: 'Bevestigen',
            },
            backup: {
                option: 'Registreer herstelcode',
                optionInfo: 'Bewaar deze code op een veilige plek.',
                title: 'Bewaar de code ergens veilig',
                info: 'Deze code is nu actief en je eduID-app is klaar om te gebruiken.',
                copy: 'Kopieer de code',
                copied: 'Gekopieerd',
                next: 'Mijn code is veilig. Doorgaan',
            },
            congrats: {
                title: 'Succes',
                info: 'Je kunt nu de eduID-app gebruiken om snel in te loggen bij applicaties die eduID gebruiken.',
                next: 'Verder',
            },
        },
        stepUp: {
            pushTitle: 'Controleer je eduID app',
            pushInfo:
                'We hebben een pushmelding naar je app gestuurd om te controleren dat jij het bent.',
            scanTitle: 'Scan deze QR-code met je eduID app',
            qr: 'QR-code om te bevestigen met de eduID app',
            noNotification: 'Geen melding?',
            qrLink: 'Maak een QR-code',
            qrPostfix: 'en scan deze.',
            offline: 'Toont je telefoon een code?',
            offlineLink: 'Code invoeren',
            codeLabel: 'Code uit je eduID app',
            wrongCode: 'De code is niet juist.',
            open: 'Open de app op dit apparaat',
            lost: 'Je app kwijt?',
            lostLink: 'Lees hoe je een nieuwe moet registreren',
            lostUrl: 'https://eduid.nl/help',
            timeoutTitle: 'Sessie verlopen',
            timeoutInfo:
                'Je sessie is verlopen. Sluit dit venster en probeer het opnieuw.',
            suspended: 'De controle van je eduID app is mislukt.',
            suspendedMinutes: {
                one: 'Je moet {{count}} minuut wachten voordat je het opnieuw kan proberen.',
                other: 'Je moet {{count}} minuten wachten voordat je het opnieuw kan proberen.',
            },
            notSuspended: 'Je kan het opnieuw proberen.',
            cancel: 'Annuleren',
        },
        deactivate: {
            title: 'Deactiveer je eduID-app',
            info: 'Je kunt je eduID-app deactiveren als je de app opnieuw wilt installeren of als je een nieuw apparaat hebt.',
            sendInfo:
                'Ga door om een verificatiecode te ontvangen. Die heb je nodig om de app te deactiveren.',
            codeInfo: 'Vul de verificatiecode in die je hebt ontvangen.',
            recoveryInfo:
                'Vul de herstelcode in die je tijdens de installatie van de eduID-app hebt bewaard.',
            codeLabel: 'Verificatiecode',
            recoveryLabel: 'Herstelcode',
            wrongCode: 'Verkeerde code. Probeer het opnieuw.',
            maxAttempts:
                'Maximum aantal pogingen bereikt. Neem contact op met help@eduid.nl voor hulp.',
            sendError:
                'We konden geen verificatiecode versturen. Probeer het later opnieuw.',
            cancel: 'Annuleren',
            next: 'Ga door',
            action: 'Deactiveren',
        },
    },
    security: {
        title: 'Beveiliging',
        lead: 'Beheer je inlog- en herstelmethoden om je eduID veilig te houden.',
        change: 'Wijzigen',
        single: {
            title: 'Zorg dat je altijd toegang houdt',
            description:
                'Je hebt nu één manier om in te loggen. Raak je die kwijt, dan verlies je ook toegang tot de diensten die je met je eduID gebruikt. Stel hieronder een extra inlogmethode in.',
        },
        active: 'Actieve inlogmethoden',
        recommended: 'Aanbevolen inlogmethode',
        other: 'Andere inlogmethoden',
        email: {
            title: 'Stuur een inlogcode',
            description: 'Naar {{email}}',
        },
        password: {
            title: 'Wachtwoord',
            description: 'Toegevoegd op {{date}}',
            add: 'Wachtwoord toevoegen',
            flow: {
                cancel: 'Annuleren',
                confirm: 'Bevestigen',
                addTitle: 'Wachtwoord toevoegen',
                updateTitle: 'Wachtwoord wijzigen of verwijderen',
                addInfo:
                    'We sturen je een e-mail met een verificatiecode. Nadat je de code invult, kun je een wachtwoord toevoegen.',
                updateInfo:
                    'We sturen je een e-mail met een verificatiecode. Nadat je de code invult, kun je je wachtwoord wijzigen of verwijderen.',
                outstandingTitle: 'Openstaande e-mailwijziging',
                outstandingInfo:
                    'Je hebt recent een code ontvangen om een nieuw e-mailadres te bevestigen. Die code is niet langer geldig. Je ontvangt een nieuwe code om dit wachtwoordverzoek te bevestigen.',
                codeTitle: 'Voer verificatiecode in',
                codeSentTo: 'Voer de code in die is verstuurd naar',
                codeLabel: 'Verificatiecode',
                codeError: 'Verkeerde code. Probeer het opnieuw.',
                resendPrompt: 'Geen e-mail ontvangen? Kijk in je spammap, of',
                resend: 'stuur een nieuwe code',
                resent: 'We hebben je een nieuwe code gestuurd.',
                newTitle: 'Nieuw wachtwoord instellen',
                changeTitle: 'Wachtwoord wijzigen',
                disclaimer:
                    'Kies een wachtwoord van minimaal 8 karakters, met een maximum van 128 karakters.',
                newPassword: 'Nieuw wachtwoord',
                confirmPassword: 'Bevestig nieuw wachtwoord',
                mismatch: 'De wachtwoorden komen niet overeen.',
                tooLong:
                    'Je wachtwoord is te lang. Gebruik maximaal 128 karakters.',
                expired: 'De code om je wachtwoord in te stellen is verlopen.',
                expiredAction: 'Stuur een nieuwe code',
                save: 'Wachtwoord opslaan',
                delete: 'Wachtwoord verwijderen',
                deleteTitle: 'Wachtwoord verwijderen?',
                deleteInfo:
                    'Weet je zeker dat je je wachtwoord wilt verwijderen? Het is hierna niet meer mogelijk om in te loggen met dit wachtwoord.',
                deleteAction: 'Verwijderen',
                back: 'Terug',
            },
        },
        passkey: {
            title: 'Passkey',
            description: '{{name}}, toegevoegd op {{date}}',
            add: 'Passkey toevoegen',
            flow: {
                addTitle: 'Passkey toevoegen',
                addInfo:
                    'Een passkey is een los of ingebouwd apparaat dat strikt persoonlijk van jou is, waarmee je kunt bewijzen dat jij de eigenaar van je eduID bent. Nadat je een passkey hebt toegevoegd, kun je deze gebruiken om mee in te loggen. Ook kun je nog steeds inloggen met een code via e-mail of wachtwoord.',
                name: 'Naam van passkey',
                namePlaceholder: 'bijv. mijn gele Yubikey',
                start: 'Start',
                cancel: 'Annuleren',
                editTitle: 'Passkey bewerken',
                added: 'Je hebt deze passkey toegevoegd op {{date}}',
                update: 'Bewaar',
                test: 'Test',
                testSuccess:
                    'Je hebt je passkey met succes getest voor inloggen.',
                testFailed:
                    'Het testen van je passkey is mislukt. Probeer het opnieuw.',
                delete: 'Passkey verwijderen',
                deleteTitle: 'Passkey verwijderen?',
                deleteInfo:
                    'Weet je zeker dat je de passkey {{name}} wilt verwijderen? De passkey wordt verwijderd uit je eduID, maar wordt niet verwijderd uit je browser en/of van je YubiKey-apparaat.',
                deleteAction: 'Verwijderen',
                back: 'Terug',
            },
        },
        app: {
            title: 'Bevestig met je eduID-app',
            description: 'Ontvang een melding of scan een QR-code',
            lastLogin: 'Laatste inlog',
            device: 'Apparaat',
            devices: {
                APNS: 'iPhone',
                FCM: 'Android',
                GCM: 'Android',
            },
            deactivate: 'Je eduID-app deactiveren',
            download: 'Download de eduID-app',
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
        securityApp: 'eduID-app',
        activity: 'Activiteit',
        account: 'Account',
        accountDelete: 'Account verwijderen',
        updateEmail: 'E-mailadres bevestigen',
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
