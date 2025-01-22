const nl = {
    code: "NL",
    name: "Nederlands",
    languages: {
        language: "Language",
        en: "Engels",
        nl: "Nederlands",
    },
    landing: {
        header: {
            title: "ServiceDesk",
            subTitle: "",
            login: "Log in",
            sup: "EduID ServiceDesk is alleen op uitnodiging beschikbaar.",
        },
        works: "Hoe werkt het?",
        adminFunction: "Medewerker",
        studentFunction: "Student",
        info: [
            //Matrices met titels en infoblokken en of een functie een beheerdersfunctie is
            ["Beveiliging", "<p>EduID ServiceDesk is een beveiligd platform ontworpen om de identiteit te valideren van studenten die een identiteitscontrole hebben aangevraagd. Deze dienst waarborgt naleving van formele identificatienormen en biedt tegelijkertijd een gebruiksvriendelijk proces voor medewerkers en studenten. </p>",
                true],
            ["Compliance", "<p>Gebouwd om te voldoen aan de formele identificatievoorschriften en nauwkeurigheid bij identiteitsverificatie te garanderen. Vermindert fouten door een gestructureerd en stapsgewijs validatieproces</p>", true],
            ["Authenticatie", "<p>Medewerkers initiëren het verificatieproces met behulp van een unieke code die via e-mail naar de student wordt verzonden. Het systeem zorgt ervoor dat alleen studenten met geldige aanmeldingscodes door kunnen gaan met de identiteitscontrole.</p><br/> ", false],
        ],
        footer: "<p>De eduID ServiceDesk vereenvoudigt het identiteitsverificatieproces en zorgt voor veiligheid en efficiëntie.</p>" +
            "<p>Wil je meer weten? <a href='https://support.surfconext.nl/servicedesk-nl'>Lees verder</a>.</p>",
    },
    header: {
        title: "EduID ServiceDesk",
        links: {
            logout: "Logout"
        }
    },
    serviceDesk: {
        member: "ServiceDesk lid"
    },
    tabs: {
        home: "Home",
        verify: "Identiteitscontrole"
    },
    verification: {
        header: "Voer de verificatiecode in",
        proceed: "Doorgaan",
        error: "Geen gebruiker gevonden voo de verificatiecode {{code}}",
        info: "Deze code heeft de persoon gegenereerd in eduID"
    },
    control: {
        header: "Identiteitscontrole voor de code : ",
        restart: "Opnieuw beginnen",
        proceed: "Doorgaan",
        error: "Er is een onverwachte fout opgetreden. Neem dan contact op met <a href='mailto:servicedesk@surf.nl>?subject=Referentie {{reference}}'>SURF ServiceDesk</a>",
        info: "Controleer het identiteitsbewijs van deze persoon",
        validDocuments: "Geldige documenten: paspoorten, EER ID-kaarten, Nederlandse rijbewijzen en Nederlandse verblijfsvergunningen",
        inValidDocuments: "Kopieën, OV-kaarten en studentenkaarten zijn geen geldige documenten",
        isValid: "Klopt dit allemaal?",
        validations: {
            photo: "De persoon <span>lijkt op de foto</span> in het document",
            valid: "Het document is <span>nog geldig</span>",
            lastName: "De achternaam in het document: <span>{{lastName}}</span>",
            firstName: "De voornamen in het document: <span>{{firstName}}</span>",
            dayOfBirth: "De geboortedatum in het document: <span>{{dayOfBirth}}</span>",
        },
        invalidDate: "De geboortedatum wordt niet herkent, selecteer de juiste datum",
        validDate: "De geboortedatum is gecorrigeerd",
        idDocument: "Vul de <span>laatste 6 karakters</span> van het document in",
    },
    confirmation: {
        header: "De identiteit van de persoon is bevestigd",
        info: "De persoon ziet vanaf nu in eduID een vinkje voor de persoonsgegevens",
        restart: "Terug naar home"
    },
    footer: {
        terms: "Gebruiksvoorwaarden",
        termsLink: "https://support.surfconext.nl/terms-nl",
        privacy: "Privacybeleid",
        privacyLink: "https://support.surfconext.nl/privacy-nl",
        surfLink: "https://surf.nl",
    },

}

export default nl;
