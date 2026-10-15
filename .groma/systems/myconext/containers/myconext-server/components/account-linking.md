---
type: C4 Component
title: Account linking
status: stable
groma:
  id: account-linking
  parent: myconext-server
  code:
    - scanner: java
      file: myconext-server/src/main/java/myconext/api/AccountLinkerController.java
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/OpenConextOidcUserService.java
      symbol: OpenConextOidcUserService
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/AppAwareAuthorizationRequestResolver.java
      symbol: AppAwareAuthorizationRequestResolver
    - scanner: java
      file: myconext-server/src/main/java/myconext/security/RemoteUser.java
      symbol: RemoteUser
    - scanner: java
      file: myconext-server/src/main/java/myconext/config/CreateFromInstitutionProperties.java
      symbol: CreateFromInstitutionProperties
    - scanner: java
      file: myconext-server/src/main/java/myconext/util/CreateFromInstitutionReturnUrlSupport.java
      symbol: CreateFromInstitutionReturnUrlSupport
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/CreateInstitutionEduID.java
      symbol: CreateInstitutionEduID
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/RequestInstitutionEduID.java
      symbol: RequestInstitutionEduID
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/RequestInstitutionEduIDRepository.java
      symbol: RequestInstitutionEduIDRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/MobileLinkAccountRequest.java
      symbol: MobileLinkAccountRequest
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/MobileLinkAccountRequestRepository.java
      symbol: MobileLinkAccountRequestRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/ExternalUser.java
      symbol: ExternalUser
    - scanner: java
      file: myconext-server/src/main/java/myconext/repository/ExternalUserRepository.java
      symbol: ExternalUserRepository
    - scanner: java
      file: myconext-server/src/main/java/myconext/model/AuthorizationURL.java
      symbol: AuthorizationURL
  group: Account
description: Connects an eduID to an institution or verification identity.
---

Starts the OpenID Connect login used to link an institution account, including the create-from-institution flow and the mobile linking request. It also begins iDIN and eHerkenning checks and sends the browser to the verification service. The verified attributes themselves are applied by identity verification.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [myconext-server/src/main/java/myconext/api/AccountLinkerController.java](../../../../../../myconext-server/src/main/java/myconext/api/AccountLinkerController.java) | [surfconext-connect](../../../../../externals/surfconext-connect.md) | Starts institution login | OIDC |
| [myconext-server/src/main/java/myconext/api/AccountLinkerController.java](../../../../../../myconext-server/src/main/java/myconext/api/AccountLinkerController.java) | [idin-verify](../../../../../externals/idin-verify.md) | Starts an identity check | HTTPS |
