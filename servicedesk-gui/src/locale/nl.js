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
            sup: "EduID ServiceDesk is by invitation only.",
        },
        works: "How does it work?",
        info: [
            //Arrays of titles and info blocks and if a function is an admin function
            "SURF TODO.",
        ],
        footer: "<p>SURFconext ServiceDesk offers user validation for SURFconext-connected applications.</p>" +
            "<p>Do you want to know more? <a href='https://support.surfconext.nl/servicedesk-en'>Read more</a>.</p>",
    },
    header: {
        title: "SURFconext ServiceDesk",
        links: {
            logout: "Logout"
        }
    },
    serviceDesk: {
        member: "ServiceDesk member"
    },
    tabs: {
        home: "Home",
        verify: "Identity control"
    },
    verification: {
        header: "Enter the verification code",
        proceed: "Continue",
        error: "No user found for verification code {{code}}",
        info: "Deze code heeft de persoon gegenereerd in eduID"
    },
    control: {
        header: "Identity control for code: ",
        restart: "Start again",
        proceed: "Continue",
        error: "An unexpected error has occurred. Please contact <a href='mailto:servicedesk@surf.nl>?subject=Reference {{reference}}'>SURF ServiceDesk</a>",
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
