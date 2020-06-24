import I18n from "i18n-js";

I18n.translations.nl = {
  login: {
    header: "Login met eduID op",
    header2: "Aanvraag",
    header3: "van je eduID",
    trust: "Vertrouw deze computer",
    email: "Je e-mailadres",
    emailPlaceholder: "bijv. naam@gmail.com",
    familyName: "Achternaam",
    givenName: "Voornaam",
    familyNamePlaceholder: "bijv. Berners-Lee",
    givenNamePlaceholder: "bijv. Tim",
    usePassword: "Gebruik wachtwoord",
    useMagicLink: "Gebruik magische link",
    sendMagicLink: "Verstuur magische link",
    useWebAuth: "Gebruik webAuthn",
    requestEduId: "Vraag je eduID aan",
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
    emailInUse: "E-mailadres is al in gebruik.",
    emailNotFound: "E-mailadres niet gevonden.",
    emailOrPasswordIncorrect: "E-mailadres of wachtwoord is niet juist",
    passwordDisclaimer: "Je wachtwoord moet minimaal 15 karakters lang zijn, of minimaal 8 als het een hoofdletter en een getal bevat.",
    noGuestAccount: "Heb je nog geen <strong>eduID?</strong>",
    requestGuestAccount: "Vraag een eduID aan",
    alreadyGuestAccount: "Heb je al een <strong>eduID?</strong>",
    noPasswordNeeded: "Geen wachtwoord nodig. We e-mailen je een magische link waarmee je direct kunt inloggen.",
    noPasswordNeededWebAuthn: "Geen wachtwoord nodig. Gebruik je Yubikey of vingerafdruk om direct in te loggen.",
    usePasswordLink: "Gebruik toch een wachtwoord",
    usePasswordLinkInfo: "als je deze hebt ingesteld.",
    useWebAuthnLink: "Of gebruik WebAuthn",
    useWebAuthnLinkInfo: " als je public keys hebt toegoevoegd.",
    whatis: "Wat is eduID?",
    agreeWithTerms: "<span>Ik ga akkoord met <a href='https://eduid.nl/voorwaarden/' target='_blank'>de voorwaarden.</a> En ik begrijp <a href='https://eduid.nl/privacyverklaring/' target='_blank'>de privacyverklaring</a>.</span>"
  },
  magicLink: {
    header: "Bijna klaar!",
    header2: "Open nu je mailbox",
    info1: "We hebben een e-mail gestuurd naar",
    info2: "Deze bevat de magische link waarmee je kunt inloggen.",
    wrongEmail: "Is het bovenstaande e-mailadres niet correct?",
    wrongEmail2: "Begin opnieuw."
  },
  confirm: {
    header: "Gelukt!",
    thanks: "Je eduID is geactiveerd. Ga door naar je bestemming."
  },
  confirmStepup: {
    header: "Gelukt!",
    conditionMet: "Je hebt aan alle voorwaarden voldaan.",
    proceed: "Ga naar {{name}}"
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
  session: {
    title: "Je sessie is verlopen.",
    info: "Je moet de magische link vanuit de e-mail openen in dezelfde browsersessie als waar je de magische link hebt aangevraagd. <br/> <br/> Ga terug naar de service waar je naar toe ging en vraag een nieuwe magische link aan."
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
    info: "Inschakelen Public Key Cryptography en Web Authentication (WebAuthn)",
    browserPrompt: "Je browser vraagt ​je om een ​​van je beveiligingssleutels of vingerafdruk te registreren bij je account."
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
