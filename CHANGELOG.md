# Changelog
All notable changes to this project will be documented in this file.

## [8.1.0] july 2025
- Add Prometheus monitoring endpoint
- Add CAPTCHA to new account registrations
- Allow name edits for remotely created "Ongevalideerd" (Unvalidated) accounts
- Add eduPersonAssurance attribute
- Extend scoped affiliations to include finalized enrollments
- Improve logging
- Include service overview in deprovisioning emails
- Fix: Using the back button after recovery code is shown causes the code to be lost
- Fix: ACR-linked institution still offers option to use a different one
- Make Spryng SMS route configurable
- Add bulk API for Studielink pseudonym creation

## [8.0.6] July 2025
- Bugfix for validated name ordering if multiple sources are available

## [8.0.4] May 2025
- Add the code login flow instead of magic link: Changed magic link login flow into a code flow. End users will receive, instead of a magic link, a code in their email, which they have to fill in in the UI. This is to prevent UX issues, for example where users open the magic link in "another browser" which resulted in a lot of unhappy user flows. The mechanics for magic link is still in this release. #onetimepassword

## [7.5.12] May 2025
- Add new feature: the ServiceDesk. This feature allows end users to verifiy their myconext (eduID) account using a ServiceDesk, which they can physically visit or video call. This is an alternative method of identify verification, for users that cannot use their Institute account, iDIN (Dutch Banks), or eIDAS (European ID) to verify their myconext (eduID).
- Textual changes to improve UX and understandability (and start using Localicious for good text synchronisation)
- Improvement to the .well-known files and apple-app-site-association files, to improve the working with the myconext (eduID) app.
- Improved logging (for SMS features and email features)
- Bump a lot of dependancies to their newer stable version.

## [7.4.10] April 2025
- Security Patch! Strongly advices for all parties using the SMS functionality

## [7.4.0] 
- Activates e-mail nudges. This includes nudges for the use of a personal e-mail (and not the school or institute e-mail), two warning for account removal (1 month and 1 week in advance), inactivity (to prevent losing access to the account), and a nudge to start using the app.
- Updated to the latest passkey library. Now passkey users with all different versions of their webbrowers should be able to use their passkey with myconext.
- Fixed a user experience flaw, involving the MFA TIQR secret. The fix should make it easier for users to unlink and link a new MFA.
- The new build includes the libopensaml upgraden to 3.3.1.

## [7.3.8]
- UX improvements, including improved texts, show affiliation with new verification, show app nudges less often.
- Improved and fixed names and validated names derivation. Including always ask a user which validated name they want to prefer.
- New API endpoint Invite
- General improvements
- Migration to Java 21
- Updated dependencies

## [7.3.5]
- UX improvements on the inlog screen, the security screen and the delete your eduID screen
- Extend iDIN api with logos of banks
- Prevent (accidential) spaces at the end of a name
- Prevent creating multiple accounts with the same e-mail address, due to cApITaL LeTTEr use. 
- General improvements, update dependencies

## [7.3.4]
- Bug fix for backwards compatibility

## [7.3.3]
- Bug fixes and improvements 

## [7.3.2]
- Updated the email-eduid-exists endpoint to also includes the eduIDValue, in case the user already exists.

## [7.3.1]
- Fixed a small issue with the ID validation using Signicat services

## [7.3.0]
- Add support for ID validation using Signicat services
- Extend API for remote creation
- Update the wau eduID identifiers for services are stored

## [7.2.10]
- If the app sends only a givenname to /mobile/api/sp/update (old app) update the self-asserted chosenname field
- If the app sends only the chosen name (new app) update the self-asserted chosenname field.
- Is the app sends both the givenname as the chosenname field, produce an error the formal-givenname can't be updated this way

## [7.2.9]
- Make chosenname optional in mobile API

## [7.2.8]
- Fix Tiqr-cookie
- Fix redirect after logout

## [7.2.6]
- Improve Tiqr-cookie security
- escape html in email
- Remove 'Stay signed in'
- Login screen consistency

## [7.2.5]
- Allow multiple accountlinkt for one instituition
- Update translations and app-links
- Update dependencies

## [7.2.3]
- Bugfix for adding affiliations

## [7.2.2]
- Update typo's, image and translations
- Do not allow for POST binding with SSO
- Short time-to-life for registration cookie

## [7.2.1]
- Bugfix for nudge-screen
- Bugfix for chosen name migration

## [7.2.0]
- Replace Spring-SAML with openconext-saml-java
- New flow and screens for account linking

## [7.1.3]
-  MDC bugfix for logging
- Update depencencies

## [7.1.2]
- Enable mobile API
- Hide email buttons in mobile view
- Default to magic link for app-authentications
- Remove stay signed in question

## [7.0.14]
- Bugfix for MFA op SSO

## [7.0.13]
- Enable SingleSignOn for MFA authentications

## [7.0.12]
- Bugfix for account-linking update

## [7.0.11]
- Bugfix for removing MFA registrations
- Update image on app-nudge screen

## [7.0.10]
- Bugfix for Attribute Aggregation not storing new eduID identifiers.

## [7.0.9]
- Create an eduID based upon a institution login https://my.domain/myconext/create-from-institution
- Repair account-link expiration timing
- Repair forgot-password links if requested multiple times

## [7.0.7]
- Allow replacement of recovery tokens
- Send an email when logging in from a new location, including geo-location
- Prevent the usage of known disposable email providers
- Allow removing the password
- Fix logging out of account-gui
- Require MFA (app) login by sending an AuthnContextClassref

## [7.0.0]
- Add support for authenticating with a [tiqr](https://tiqr.org) app.

## [6.0.2]
- Move health and info endpoint to /internal
- Update dependencies

## [6.0.0]
- Migrated to JDK11

## [5.2.5]
### Fixes
- Improve eduPersonAffiliation and eduPersonScopedAffiliation

## [5.2.3]
### New
- Improve usability of the verification code
- Sent the same eduID identifier to institutions belonging to the same organisation
- Register trusted devices, so we can inform users of new logins later
- Add an option to use a non-institution account for validating a name

## [5.1.7]
### Fixes
- Update links to privacy and terms
- Show email of the eduID instead of the linked account, if the account is already linked to an eduid

## [5.1.6]
### Fixes
- Remove spaces from one time verification code
- Update dependencies

## [5.1.4]
### Fixes
- Update dependencies
- Log token deletion
- Use eduID if no UID is present in a token.
- Prevent string manipulation in login-page
### New
- Show to user with which eduID account institutional account is linked
- Log invalid tokens in API requests
- Log usage of deleted token

## [5.1.2] 2021-04-19
### Fixes
- Fix problem with diacritics in names

## [5.1.1] 2021-04-14
### New
- API for linked accounts info
### Fixes/improvement
- Improved lookup of email and eduid in backend
- Verification mail code case insensitive
- Focus on custom checkbox, select with _space_
- Update translations

## [5.0.3] 2021-03-10
### Fixes
- Apply feature toggle OINCNG token
- Update translations
- Handle magic link without magic

## [5.0.2] 2021-02-22
### Fixes/improvement
- Fix FIDO2 login on Apple devices

## [5.0.1] 2021-02-15
### New
- Allow the user to change the email address
- Only allow an institution account to be linked to one myconext account
- Redesign of landing page
- Allow downloading of personal data

### Fixes/improvement
- Fix FIDO2 login on Apple devices
- Fix session lost if opening the magic-link in an other browser

## [4.0.4] 2020-12-10
### Fixes/improvement
- Fix security issue in account linking

## [4.0.3] - 2020-10-29
### Fixes/improvement
- Feature toggle for webauthn was not honored in IdP
- Minor textual changes

## [4.0.1] - 2020-10-27
### Fixes/improvement
- Minor textual changes

## [4.0.0] - 2020-10-20
### New
- Warn a user when using an institutional email adres
- Add allowlist feature (default disabled) to only allow sign up from known email-domains
- Log all user-events in standarised json format
- Direct user to known email-providers after registration
- Add ability to test webauthn/fido2 authentication
- Improve translations
### Fixes/improvement
- Respond with 'NoAuthnContext' status if an unknown ACR is requested
- Fix/add forgotten password routine
- Fix users being 'stuck' in registration flow-

## [3.0.17] - 2020-10-05
### Fixes/improvement
- Fix error after stepup authentication in ALA flow

## [3.0.13] - 2020-09-07
### New
- Use the last linked account for selecting validated names
### Fixes/improvement
- After deleting all links, request a new link for acr validated-names
- Show the service name in the current language
- Security updates for dependencies

## [3.0.10] - 2020-08-07
### New
- Add JSON logging
- Add WebAuthn (feature toggle)
- Add Account Linking
- Use [AuthenticationContextClassReference](https://wiki.surfnet.nl/x/qgQiAg) for a SP request account linking
### Fixes/improvement
- Filter error warnings

## [2.0.13] - 2020-04-24
### Fixes/improvement
- Allow multiple SP's for the eduID IDP
- Update path-matching in redirects

## [2.0.11] - 2020-04-15
### Fixes/improvement
- Bugfixes

## [2.0.10] - 2020-03-27
### New
- Add oneGini migration
- Set correct shacHomeOrg values
### Fixes/improvement
- Update text and translations
- Update layout and images
- Change identifier in nameID
- Imporove responsive design

## [2.0.4] - 2020-03-19
### New
- Warn if migration email-address is in use
### Fixes/improvement
- Fix open redirect
- Fix possible loop on logout
- Update translations
- Change loglevel for unknown emails
- Enlarge clickable area for menu in SP

## [2.0.1] - 2020-03-13
### Fixes/improvement
- Fix translations
- Update layouts
