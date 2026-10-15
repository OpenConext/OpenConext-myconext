---
type: C4 Component
title: Account API
status: stable
groma:
  id: account-api
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/UserController.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/EnvironmentController.java
      symbol: EnvironmentController
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/HasUserRepository.java
      symbol: HasUserRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/CreateAccount.java
      symbol: CreateAccount
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UpdateEmailRequest.java
      symbol: UpdateEmailRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UpdateUserNameRequest.java
      symbol: UpdateUserNameRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UpdateUserSecurityRequest.java
      symbol: UpdateUserSecurityRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UpdateCredential.java
      symbol: UpdateCredential
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UpdateLinkedAccountRequest.java
      symbol: UpdateLinkedAccountRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/DeleteService.java
      symbol: DeleteService
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/DeleteServiceTokens.java
      symbol: DeleteServiceTokens
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/LanguageChangeRequest.java
      symbol: LanguageChangeRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/UserResponse.java
      symbol: UserResponse
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/CreateEduIDResponse.java
      symbol: CreateEduIDResponse
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/PasswordResetHash.java
      symbol: PasswordResetHash
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/PasswordResetHashRepository.java
      symbol: PasswordResetHashRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ChangeEmailHash.java
      symbol: ChangeEmailHash
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/ChangeEmailHashRepository.java
      symbol: ChangeEmailHashRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/validation/PasswordStrength.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/LongPasswordAwareBCryptPasswordEncoder.java
      symbol: LongPasswordAwareBCryptPasswordEncoder
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/PasswordTooLongException.java
      symbol: PasswordTooLongException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/WeakPasswordException.java
      symbol: WeakPasswordException
    - scanner: java
      file: myconext-server/src/main/java/myconext/exceptions/DuplicateUserEmailException.java
      symbol: DuplicateUserEmailException
    - scanner: java
      file: myconext-server/src/main/java/myconext/webauthn/UserCredentialRepository.java
      symbol: UserCredentialRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/Challenge.java
      symbol: Challenge
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/ChallengeRepository.java
      symbol: ChallengeRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ClientAuthenticationRequest.java
      symbol: ClientAuthenticationRequest
  group: Account
description: Registration, profile, and security API used by the web applications.
---

Creates an eduID, checks one-time codes, and updates name, email, password, passkeys, login preference, and connected-service tokens. My eduID also asks it for the in-person verification code. Outbound account mail and the optional registration captcha are invoked from here.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/api/UserController.java](../../../../../../myconext-server/src/main/java/myconext/api/UserController.java) | [myconext-server/src/main/java/myconext/mail/MailBox.java](../../../../../../myconext-server/src/main/java/myconext/mail/MailBox.java) | Sends account mail | Java |
| [myconext-server/src/main/java/myconext/api/UserController.java](../../../../../../myconext-server/src/main/java/myconext/api/UserController.java) | [myconext-server/src/main/java/myconext/captcha/CaptchaVerifier.java](../../../../../../myconext-server/src/main/java/myconext/captcha/CaptchaVerifier.java) | Guards registration | Java |
