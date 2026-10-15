---
type: C4 Component
title: Guest IdP login
status: stable
groma:
  id: guest-idp-login
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java
      symbol: GuestIdpAuthenticationRequestFilter
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/LoginController.java
      symbol: LoginController
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/SamlAuthenticationCallBack.java
      symbol: SamlAuthenticationCallBack
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/SamlAuthenticationRequest.java
      symbol: SamlAuthenticationRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/ACR.java
      symbol: ACR
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/IdentityProviderMetaData.java
      symbol: IdentityProviderMetaData
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/AuthenticationRepository.java
      symbol: AuthenticationRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/AuthenticationRequestRepository.java
      symbol: AuthenticationRequestRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/OneTimeLoginCode.java
      symbol: OneTimeLoginCode
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/VerifyOneTimeLoginCode.java
      symbol: VerifyOneTimeLoginCode
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/InvalidOneTimeLoginCodeException.java
      symbol: InvalidOneTimeLoginCodeException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/ExpiredAuthenticationException.java
      symbol: ExpiredAuthenticationException
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/UserAuthentication.java
      symbol: UserAuthentication
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/UserAuthenticationToken.java
      symbol: UserAuthenticationToken
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/VerificationCodeGenerator.java
      symbol: VerificationCodeGenerator
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/CookieResolver.java
      symbol: CookieResolver
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/CookieValueEncoder.java
      symbol: CookieValueEncoder
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/LocalDevelopmentAuthenticationFilter.java
      symbol: LocalDevelopmentAuthenticationFilter
    - scanner: java
      file: myconext-server/src/main/java/myconext/crypto/HashGenerator.java
      symbol: HashGenerator
    - scanner: java
      file: myconext-server/src/main/java/myconext/crypto/KeyGenerator.java
      symbol: KeyGenerator
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/StepUpStatus.java
      symbol: StepUpStatus
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/LoginStatus.java
      symbol: LoginStatus
  group: Sign-in
description: Completes SAML sign-in for the eduID identity provider.
---

Receives the authentication request from a service provider and finishes it with a signed assertion. The same path covers an existing session, a magic link, a password, a passkey, and a completed eduID-app login, including the extra check when the magic link is opened on another device. It also serves the login routes and stores the authentication request. A local-development filter sits on this path for localhost testing.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java](../../../../../../myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java) | [surfconext-engine](../../../../../externals/surfconext-engine.md) | Returns the assertion | SAML |
| [myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java](../../../../../../myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java) | [myconext-server/src/main/java/myconext/geo/GeoLocation.java](../../../../../../myconext-server/src/main/java/myconext/geo/GeoLocation.java) | Locates the browser | Java |
| [myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java](../../../../../../myconext-server/src/main/java/myconext/security/GuestIdpAuthenticationRequestFilter.java) | [account-gui/src/main.js](../../../../../../account-gui/src/main.js) | Shows the login screen | Browser |
