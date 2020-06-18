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
    thanks: "Je eduID is geactiveerd. Ga door naar je bestemming.",
    thanksStepup: "Je eduID is geactiveerd en gekoppeld aan een instelling. Ga door naar je bestemming."
  },
  confirmStepup: {
    header: "Gelukt!",
    thanks: "Je eduID is nu gekoppeld aan je thuisinstelling. Ga door naar je bestemming."
  },
  stepup: {
    header: "Bijna daar!",
    info: "Je bent succesvol geverifieerd, maar voor de dienst {{name}} moet je je eduID-account koppelen aan een SURFconext-instelling.",
    proceed: "Klik op de onderstaande link om verder te gaan en selecteer je thuisinstelling en log in.",
    link: "Ga verder naar SURFconext"
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
  }
};
