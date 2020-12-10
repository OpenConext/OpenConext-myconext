# Changelog
All notable changes to this project will be documented in this file.

## [Unreleased]

## [4.0.4] 2020-12-10
### Fixes/improvement
- Fix security issue in account linking

## [4.0.3] - 2020-10-29
### Fixes/improvement
- Feature toggle for webauthn was nog honored in IdP
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
