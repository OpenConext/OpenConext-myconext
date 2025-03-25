const en = {
    code: "EN",
    name: "English",
    languages: {
        language: "Language",
        en: "English",
        nl: "Dutch",
    },
    landing: {
        header: {
            title: "ServiceDesk",
            subTitle: "Hello {{name}}",
            login: "Log in",
            sup: "EduID ServiceDesk is by invitation only.",
        },
        works: "How does it work?",
        adminFunction: "Employee",
        studentFunction: "Student",
        info: [
            //Arrays of titles and info blocks and if a function is an admin function
            ["Security", "<p>EduID ServiceDesk is a secure platform designed to validate the identity of students who have applied for an identity check. This service ensures compliance with formal identification standards while providing a user-friendly process for employees and students.</p>",
                true],
            ["Compliance", "<p>Built to adhere to formal identification regulations and ensure accuracy in identity verification. Reduces errors through a structured and step-by-step validation process</p>", true],
            ["Authentication", "<p>Employees initiate the verification process using a unique code sent to the student via email. The system ensures that only students with valid application codes can proceed with the identity check.</p><br/>", false],
        ],
        footer: "<p>The eduID ServiceDesk simplifies the identity verification process, ensuring security and efficiency.</p>" +
            "<p>Do you want to know more? <a href='https://support.surfconext.nl/servicedesk-en'>Read more</a>.</p>",
    },
    header: {
        title: "ServiceDesk eduID",
        links: {
            logout: "Logout"
        }
    },
    serviceDesk: {
        member: "ServiceDesk member"
    },
    tabs: {
        home: "Home",
        verify: "Identity check"
    },
    verification: {
        header: "Enter the verification code",
        proceed: "Continue",
        error: "No user found for verification code {{code}}",
        info: "The person generated this code in eduID.",
        flash: "Verification code found for user {{name}}"
    },
    control: {
        header: "Verification code ",
        restart: "Start again",
        proceed: "Ready",
        error: "An unexpected error has occurred. Please contact <a href='mailto:servicedesk@surf.nl?subject=Reference {{reference}}'>SURF ServiceDesk</a>",
        info: "Check this person’s ID",
        validDocuments: "Valid documents: passports, EEA ID cards, Dutch driving licenses and Dutch residence permits",
        inValidDocuments: "Copies, public transport tickets and student cards are not valid documents",
        isValid: "Is everything correct?",
        validations: {
            photo: "The person <span>looks like the photo</span> in the document",
            valid: "The document is <span>still valid</span>",
            lastName: "The last name in the document: <span>{{lastName}}</span>",
            firstName: "The first names in the document: <span>{{firstName}}</span>",
            dayOfBirth: "The date of birth in the document: <span>{{dayOfBirth}}</span> ",
        },
        invalidDate: "The day of birth has an invalid format, first select the correct date by pressing the calendar button at the right",
        validDate: "The day of birth is now valid and can be confirmed",
        idDocument: "Fill in the <span>last 6 characters</span> of the ID document",
    },
    forbidden: {
        info: "Unfortunately you do not have access to the ServiceDesk.",
        logoutInfo: "After logout, you MUST close your browser to complete the logout process.",
        logout: "Logout"
    },
    confirmation: {
        header: "The identity of the person has been confirmed",
        info: "From now on, the person will see a check mark in front of the personal data in eduID",
        restart: "Back to home"
    },
    footer: {
        terms: "Terms of Use",
        termsLink: "https://support.surfconext.nl/terms-en",
        privacy: "Privacy policy",
        privacyLink: "https://support.surfconext.nl/privacy-en",
        surfLink: "https://surf.nl",
        select_locale: "Select your preferred language"
    },

}

export default en;
